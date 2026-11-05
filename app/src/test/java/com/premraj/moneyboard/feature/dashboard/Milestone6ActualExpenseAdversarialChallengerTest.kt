package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.CreateCategoryCommand
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicInteger

/**
 * Empirical Adversarial Challenger Test Suite for Milestone 6:
 * Actual Expense Reset Logic & Zero-Amount Invariant Verification.
 */
class Milestone6ActualExpenseAdversarialChallengerTest {

    private object SeedIds {
        const val CASH_ACCOUNT = "acc.cash"
        const val EXPENSE_OTHER = "exp.other"
        const val EXPENSE_GROCERIES = "exp.groceries"
        const val SAVING_EMERGENCY = "sav.emergency"
        const val INVESTMENT_SIP = "inv.sip"
    }

    /**
     * Fake TransactionRepository tracking every call to create, update, and softDelete.
     */
    private class FakeTransactionRepository : TransactionRepository {
        val transactionsMap = mutableMapOf<String, FinanceTransaction>()
        val createCalls = mutableListOf<CreateTransactionCommand>()
        val updateCalls = mutableListOf<FinanceTransaction>()
        val softDeleteCalls = mutableListOf<String>()

        private val idCounter = AtomicInteger(1)

        override fun observeTransactions(month: YearMonth): Flow<List<FinanceTransaction>> {
            val list = transactionsMap.values.filter {
                YearMonth.from(it.accountingDate) == month && it.deletedAtEpochMillis == null
            }
            return flowOf(list)
        }

        override fun observeTotal(month: YearMonth, type: TransactionType, currencyCode: String): Flow<Money> {
            val totalMinor = transactionsMap.values
                .filter { YearMonth.from(it.accountingDate) == month && it.type == type && it.deletedAtEpochMillis == null }
                .sumOf { it.amount.amountMinor }
            return flowOf(Money(totalMinor, currencyCode))
        }

        override fun observeCategoryTotals(
            month: YearMonth,
            type: TransactionType,
            currencyCode: String
        ): Flow<List<CategoryTotalRow>> {
            return flowOf(emptyList())
        }

        override suspend fun getTransaction(transactionId: String): FinanceTransaction? {
            return transactionsMap[transactionId]?.takeIf { it.deletedAtEpochMillis == null }
        }

        override suspend fun createTransaction(command: CreateTransactionCommand): FinanceTransaction {
            createCalls.add(command)
            val tx = FinanceTransaction(
                id = "tx-${idCounter.getAndIncrement()}",
                accountId = command.accountId,
                categoryId = command.categoryId,
                type = command.type,
                amount = command.amount,
                accountingDate = command.accountingDate,
                occurredAt = command.occurredAt,
                note = command.note,
                merchant = command.merchant,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L,
                deletedAtEpochMillis = null
            )
            transactionsMap[tx.id] = tx
            return tx
        }

        override suspend fun updateTransaction(transaction: FinanceTransaction): FinanceTransaction {
            updateCalls.add(transaction)
            transactionsMap[transaction.id] = transaction
            return transaction
        }

        override suspend fun softDeleteTransaction(transactionId: String) {
            softDeleteCalls.add(transactionId)
            val existing = transactionsMap[transactionId]
                ?: error("Transaction $transactionId not found")
            transactionsMap[transactionId] = existing.copy(deletedAtEpochMillis = 2000L)
        }
    }

    /**
     * Fake CategoryRepository providing test categories with types.
     */
    private class FakeCategoryRepository : CategoryRepository {
        val categories = mutableMapOf<String, Category>()

        override fun observeActiveCategories(): Flow<List<Category>> = flowOf(categories.values.toList())
        override fun observeActiveCategories(type: CategoryType): Flow<List<Category>> =
            flowOf(categories.values.filter { it.type == type })
        override fun observeAllCategories(): Flow<List<Category>> = flowOf(categories.values.toList())
        override suspend fun getCategory(categoryId: String): Category? = categories[categoryId]
        override suspend fun createCategory(command: CreateCategoryCommand): Category {
            val cat = Category(
                id = "cat-${command.name.lowercase()}",
                name = command.name,
                iconKey = command.iconKey,
                type = command.type,
                group = command.group,
                sortOrder = command.sortOrder,
                systemCategory = false,
                archived = false,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
            categories[cat.id] = cat
            return cat
        }
        override suspend fun archiveCustomCategory(categoryId: String) {
            categories[categoryId]?.let {
                categories[categoryId] = it.copy(archived = true)
            }
        }
    }

    /**
     * Test harness faithfully mirroring MoneyBoardApp.kt updateActualExpense and clearActualExpensesForCategory.
     */
    private class ActualExpenseHarness(
        val transactionRepository: FakeTransactionRepository,
        val categoryRepository: FakeCategoryRepository
    ) {
        var selectedMonth: YearMonth = YearMonth.now()
        val toastMessages = mutableListOf<String>()
        val errorMessages = mutableListOf<String>()

        suspend fun updateActualExpense(categoryId: String, newRupees: Long) {
            try {
                val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                val transactions = transactionRepository.observeTransactions(selectedMonth).first()
                val existing = transactions.firstOrNull { it.categoryId == validCatId }
                if (newRupees == 0L) {
                    if (existing != null) {
                        transactionRepository.softDeleteTransaction(existing.id)
                    }
                    toastMessages.add("Actual expense reset to ₹0")
                    return
                }
                val category = categoryRepository.getCategory(validCatId)
                val txType = when (category?.type) {
                    CategoryType.SAVING -> TransactionType.SAVING
                    CategoryType.INVESTMENT -> TransactionType.INVESTMENT
                    else -> TransactionType.EXPENSE
                }
                val newMoney = Money(amountMinor = Math.multiplyExact(newRupees, 100L), currencyCode = "INR")

                if (existing != null) {
                    transactionRepository.updateTransaction(
                        existing.copy(amount = newMoney)
                    )
                } else {
                    transactionRepository.createTransaction(
                        CreateTransactionCommand(
                            accountId = SeedIds.CASH_ACCOUNT,
                            categoryId = validCatId,
                            type = txType,
                            amount = newMoney,
                            accountingDate = if (selectedMonth == YearMonth.now()) LocalDate.now() else selectedMonth.atDay(1),
                            occurredAt = Instant.now(),
                            merchant = null,
                            note = "Updated via actual dashboard"
                        )
                    )
                }
                toastMessages.add("Actual expense updated: ₹$newRupees")
            } catch (e: Exception) {
                errorMessages.add(e.message ?: "Unknown error")
                toastMessages.add("Failed to record expense: ${e.message}")
            }
        }

        suspend fun clearActualExpensesForCategory(categoryId: String) {
            try {
                val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                val transactions = transactionRepository.observeTransactions(selectedMonth).first()
                val matching = transactions.filter { it.categoryId == validCatId }
                matching.forEach { tx ->
                    transactionRepository.softDeleteTransaction(tx.id)
                }
                toastMessages.add("Actual expenses cleared")
            } catch (e: Exception) {
                errorMessages.add(e.message ?: "Unknown error")
                toastMessages.add("Failed to clear: ${e.message}")
            }
        }
    }

    private lateinit var txRepo: FakeTransactionRepository
    private lateinit var catRepo: FakeCategoryRepository
    private lateinit var harness: ActualExpenseHarness

    @Before
    fun setUp() {
        txRepo = FakeTransactionRepository()
        catRepo = FakeCategoryRepository()
        harness = ActualExpenseHarness(txRepo, catRepo)

        // Seed some categories
        catRepo.categories[SeedIds.EXPENSE_GROCERIES] = Category(
            id = SeedIds.EXPENSE_GROCERIES,
            name = "Groceries",
            iconKey = "ic_groceries",
            type = CategoryType.EXPENSE,
            group = CategoryGroup.LIFESTYLE,
            sortOrder = 1,
            systemCategory = true,
            archived = false,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        catRepo.categories[SeedIds.SAVING_EMERGENCY] = Category(
            id = SeedIds.SAVING_EMERGENCY,
            name = "Emergency Fund",
            iconKey = "ic_emergency",
            type = CategoryType.SAVING,
            group = CategoryGroup.SAVINGS,
            sortOrder = 2,
            systemCategory = true,
            archived = false,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        catRepo.categories[SeedIds.INVESTMENT_SIP] = Category(
            id = SeedIds.INVESTMENT_SIP,
            name = "Mutual Fund SIP",
            iconKey = "ic_sip",
            type = CategoryType.INVESTMENT,
            group = CategoryGroup.INVESTMENTS,
            sortOrder = 3,
            systemCategory = true,
            archived = false,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
    }

    // =========================================================================
    // 1. DOMAIN INVARIANT VERIFICATIONS: amountMinor > 0L REQUIRED
    // =========================================================================

    @Test
    fun domainInvariant_createTransactionCommand_withZeroAmount_throwsIllegalArgumentException() {
        try {
            CreateTransactionCommand(
                accountId = "acc-1",
                categoryId = "cat-1",
                type = TransactionType.EXPENSE,
                amount = Money(0L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
            fail("Expected IllegalArgumentException when amountMinor is 0L")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Exception message must mention greater than zero",
                e.message?.contains("greater than zero") == true
            )
        }
    }

    @Test
    fun domainInvariant_createTransactionCommand_withNegativeAmount_throwsIllegalArgumentException() {
        try {
            CreateTransactionCommand(
                accountId = "acc-1",
                categoryId = "cat-1",
                type = TransactionType.EXPENSE,
                amount = Money(-100L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
            fail("Expected IllegalArgumentException when amountMinor is negative")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Exception message must mention greater than zero",
                e.message?.contains("greater than zero") == true
            )
        }
    }

    @Test
    fun domainInvariant_financeTransaction_withZeroAmount_throwsIllegalArgumentException() {
        try {
            FinanceTransaction(
                id = "tx-1",
                accountId = "acc-1",
                categoryId = "cat-1",
                type = TransactionType.EXPENSE,
                amount = Money(0L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now(),
                note = null,
                merchant = null,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L,
                deletedAtEpochMillis = null
            )
            fail("Expected IllegalArgumentException when FinanceTransaction amountMinor is 0L")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Exception message must mention greater than zero",
                e.message?.contains("greater than zero") == true
            )
        }
    }

    // =========================================================================
    // 2. ACTUAL EXPENSE RESET WHEN newRupees == 0L AND TRANSACTION EXISTS
    // =========================================================================

    @Test
    fun updateActualExpense_whenRupeesZeroAndTransactionExists_softDeletesTransaction() = runTest {
        // Arrange: create an existing transaction for Groceries
        val existingTx = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(250000L, "INR"), // ₹2,500
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )
        txRepo.createCalls.clear()

        // Act: updateActualExpense to 0
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 0L)

        // Assert:
        // 1. Existing transaction was soft-deleted
        assertEquals(1, txRepo.softDeleteCalls.size)
        assertEquals(existingTx.id, txRepo.softDeleteCalls.first())
        assertNotNull(txRepo.transactionsMap[existingTx.id]?.deletedAtEpochMillis)

        // 2. Neither createTransaction nor updateTransaction was called with 0 amount
        assertEquals(0, txRepo.createCalls.size)
        assertEquals(0, txRepo.updateCalls.size)

        // 3. User received confirmation toast and zero errors
        assertTrue(harness.toastMessages.contains("Actual expense reset to ₹0"))
        assertTrue(harness.errorMessages.isEmpty())
    }

    // =========================================================================
    // 3. ACTUAL EXPENSE RESET WHEN newRupees == 0L AND NO TRANSACTION EXISTS
    // =========================================================================

    @Test
    fun updateActualExpense_whenRupeesZeroAndNoTransactionExists_returnsGracefullyWithToast() = runTest {
        // Arrange: empty transaction repository for this month

        // Act: updateActualExpense to 0
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 0L)

        // Assert:
        // 1. softDeleteTransaction was NOT called
        assertEquals(0, txRepo.softDeleteCalls.size)

        // 2. Neither createTransaction nor updateTransaction was called
        assertEquals(0, txRepo.createCalls.size)
        assertEquals(0, txRepo.updateCalls.size)

        // 3. Graceful toast shown and no error caught
        assertTrue(harness.toastMessages.contains("Actual expense reset to ₹0"))
        assertTrue(harness.errorMessages.isEmpty())
    }

    // =========================================================================
    // 4. ACTUAL EXPENSE UPDATE WHEN newRupees > 0L AND TRANSACTION EXISTS
    // =========================================================================

    @Test
    fun updateActualExpense_whenRupeesPositiveAndTransactionExists_updatesTransaction() = runTest {
        // Arrange: existing transaction with ₹1,000
        val existingTx = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(100000L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )
        txRepo.createCalls.clear()

        // Act: update to ₹3,500
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 3500L)

        // Assert:
        assertEquals(0, txRepo.softDeleteCalls.size)
        assertEquals(0, txRepo.createCalls.size)
        assertEquals(1, txRepo.updateCalls.size)

        val updated = txRepo.updateCalls.first()
        assertEquals(existingTx.id, updated.id)
        assertEquals(350000L, updated.amount.amountMinor)
        assertEquals("INR", updated.amount.currencyCode)
        assertTrue(harness.toastMessages.contains("Actual expense updated: ₹3500"))
        assertTrue(harness.errorMessages.isEmpty())
    }

    // =========================================================================
    // 5. ACTUAL EXPENSE UPDATE WHEN newRupees > 0L AND NO TRANSACTION EXISTS
    // =========================================================================

    @Test
    fun updateActualExpense_whenRupeesPositiveAndNoTransactionExists_createsNewTransaction() = runTest {
        // Act: update to ₹1,500
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 1500L)

        // Assert:
        assertEquals(0, txRepo.softDeleteCalls.size)
        assertEquals(0, txRepo.updateCalls.size)
        assertEquals(1, txRepo.createCalls.size)

        val created = txRepo.createCalls.first()
        assertEquals(SeedIds.EXPENSE_GROCERIES, created.categoryId)
        assertEquals(TransactionType.EXPENSE, created.type)
        assertEquals(150000L, created.amount.amountMinor)
        assertEquals(SeedIds.CASH_ACCOUNT, created.accountId)
        assertTrue(harness.toastMessages.contains("Actual expense updated: ₹1500"))
        assertTrue(harness.errorMessages.isEmpty())
    }

    // =========================================================================
    // 6. CLEAR ACTUAL EXPENSES (Multiple transactions per category)
    // =========================================================================

    @Test
    fun clearActualExpensesForCategory_softDeletesAllMatchingTransactions() = runTest {
        // Arrange: 3 transactions in the month - 2 groceries, 1 sip
        val g1 = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(50000L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )
        val g2 = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(70000L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )
        val s1 = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.INVESTMENT_SIP,
                type = TransactionType.INVESTMENT,
                amount = Money(1000000L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )

        // Act: clear groceries
        harness.clearActualExpensesForCategory(SeedIds.EXPENSE_GROCERIES)

        // Assert:
        assertEquals(2, txRepo.softDeleteCalls.size)
        assertTrue(txRepo.softDeleteCalls.contains(g1.id))
        assertTrue(txRepo.softDeleteCalls.contains(g2.id))
        assertFalse(txRepo.softDeleteCalls.contains(s1.id))
        assertNull(txRepo.transactionsMap[s1.id]?.deletedAtEpochMillis)
        assertTrue(harness.toastMessages.contains("Actual expenses cleared"))
    }

    // =========================================================================
    // 7. CATEGORY TYPE MAPPINGS (SAVING / INVESTMENT / EXPENSE)
    // =========================================================================

    @Test
    fun updateActualExpense_mapsCategoryTypesCorrectly() = runTest {
        // Saving category
        harness.updateActualExpense(SeedIds.SAVING_EMERGENCY, 2000L)
        assertEquals(TransactionType.SAVING, txRepo.createCalls.last().type)

        // Investment category
        harness.updateActualExpense(SeedIds.INVESTMENT_SIP, 5000L)
        assertEquals(TransactionType.INVESTMENT, txRepo.createCalls.last().type)

        // Standard expense category
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 1200L)
        assertEquals(TransactionType.EXPENSE, txRepo.createCalls.last().type)

        // Blank category ID falls back to EXPENSE_OTHER and TransactionType.EXPENSE
        harness.updateActualExpense("", 500L)
        val lastCommand = txRepo.createCalls.last()
        assertEquals(SeedIds.EXPENSE_OTHER, lastCommand.categoryId)
        assertEquals(TransactionType.EXPENSE, lastCommand.type)
    }

    // =========================================================================
    // 8. PAST & FUTURE MONTH DATE RESOLUTION
    // =========================================================================

    @Test
    fun updateActualExpense_accountingDate_currentMonthVsPastMonth() = runTest {
        // Current month uses today's date
        harness.selectedMonth = YearMonth.now()
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 1000L)
        assertEquals(LocalDate.now(), txRepo.createCalls.last().accountingDate)

        // Past month uses month.atDay(1)
        val pastMonth = YearMonth.now().minusMonths(3)
        harness.selectedMonth = pastMonth
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 1000L)
        assertEquals(pastMonth.atDay(1), txRepo.createCalls.last().accountingDate)
    }

    // =========================================================================
    // 9. SEQUENTIAL RESET CYCLES (Idempotence & State Consistency)
    // =========================================================================

    @Test
    fun updateActualExpense_repeatedZeroResets_areIdempotentAndSafe() = runTest {
        // Create initial transaction
        val tx = txRepo.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(200000L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now()
            )
        )

        // 1st reset: soft-deletes the transaction
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 0L)
        assertEquals(1, txRepo.softDeleteCalls.size)
        assertEquals(tx.id, txRepo.softDeleteCalls.first())

        // 2nd reset immediately after: no active transactions exist now
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 0L)
        // Soft delete calls count should NOT increase
        assertEquals(1, txRepo.softDeleteCalls.size)
        // Zero errors caught
        assertTrue(harness.errorMessages.isEmpty())

        // 3rd reset: still safe
        harness.updateActualExpense(SeedIds.EXPENSE_GROCERIES, 0L)
        assertEquals(1, txRepo.softDeleteCalls.size)
        assertTrue(harness.errorMessages.isEmpty())
    }

    // =========================================================================
    // 10. EDIT EXPENSE DIALOG INPUT SANITIZATION & PARSING
    // =========================================================================

    @Test
    fun editExpenseDialog_inputParsing_handlesZeroAndEmptyGracefully() {
        // Simulating EditExpenseDialog logic:
        // val initialCleanAmount = expense.amountText.replace(",", "").replace("₹", "").trim()
        // onSaveAmount = (amountText.toLongOrNull() ?: 0L).coerceAtLeast(0L)

        val inputsToExpected = listOf(
            "0" to 0L,
            "" to 0L,
            "   " to 0L,
            "000" to 0L,
            "₹ 0" to 0L,
            "1,500" to 1500L,
            "5000" to 5000L
        )

        for ((input, expected) in inputsToExpected) {
            val cleaned = input.replace(",", "").replace("₹", "").trim()
            val parsed = (cleaned.toLongOrNull() ?: 0L).coerceAtLeast(0L)
            assertEquals("Failed for input '$input'", expected, parsed)
        }
    }
}
