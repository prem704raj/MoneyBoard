package com.premraj.moneyboard.feature

import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.feature.transactions.TransactionListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var accountRepo: FakeAccountRepository
    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var viewModel: TransactionListViewModel

    private val testMonth = YearMonth.of(2026, 9)

    private val checkingAccount = Account(
        id = "acc-1",
        name = "Checking",
        type = AccountType.BANK,
        openingBalance = Money(5000_00L, "INR"),
        includeInNetWorth = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    private val groceriesCategory = Category(
        id = "cat-groceries",
        name = "Groceries",
        iconKey = "food",
        type = CategoryType.EXPENSE,
        group = CategoryGroup.FOOD,
        sortOrder = 1,
        systemCategory = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    private val salaryCategory = Category(
        id = "cat-salary",
        name = "Salary",
        iconKey = "work",
        type = CategoryType.INCOME,
        group = CategoryGroup.INCOME,
        sortOrder = 1,
        systemCategory = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    @Before
    fun setUp() {
        transactionRepo = FakeTransactionRepository()
        accountRepo = FakeAccountRepository().apply {
            accountsFlow.value = listOf(checkingAccount)
        }
        categoryRepo = FakeCategoryRepository().apply {
            categoriesFlow.value = listOf(groceriesCategory, salaryCategory)
        }

        viewModel = TransactionListViewModel(
            transactionRepository = transactionRepo,
            accountRepository = accountRepo,
            categoryRepository = categoryRepo,
            defaultCurrencyCode = "INR"
        )
        viewModel.selectMonth(testMonth)
    }

    @Test
    fun defaultState_loadsAccountsAndCategories() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertEquals(1, state.accounts.size)
        assertEquals("Checking", state.accounts.first().name)
        assertEquals(2, state.categories.size)
        assertEquals(testMonth, state.selectedMonth)
        assertTrue(state.groups.isEmpty())
    }

    @Test
    fun saveTransaction_createsAndAppearsInListAndSummary() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("150.00"),
            date = LocalDate.of(2026, 9, 15),
            note = "Supermarket visit",
            merchant = "Fresh Mart"
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.groups.size)
        val item = state.groups.first().items.first()
        assertEquals("Groceries", item.categoryName)
        assertEquals("Checking", item.accountName)
        assertEquals("Supermarket visit", item.note)
        assertEquals("Fresh Mart", item.merchant)
        assertEquals(TransactionType.EXPENSE, item.type)
        assertEquals(1, transactionRepo.transactionsFlow.value.size)
    }

    @Test
    fun updateTransaction_editsExistingRecord() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("100.00"),
            date = LocalDate.of(2026, 9, 10),
            note = "Initial note",
            merchant = "Shop A"
        )

        val createdTx = transactionRepo.transactionsFlow.value.first()
        viewModel.openEditSheet(createdTx)
        assertEquals(createdTx.id, viewModel.uiState.value.editingTransaction?.id)

        // Save updated version
        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("120.00"),
            date = LocalDate.of(2026, 9, 10),
            note = "Updated note",
            merchant = "Shop B"
        )

        val updated = transactionRepo.transactionsFlow.value.first()
        assertEquals(120_00L, updated.amount.amountMinor)
        assertEquals("Updated note", updated.note)
        assertEquals("Shop B", updated.merchant)
        assertFalse(viewModel.uiState.value.isAddEditSheetOpen)
        assertNull(viewModel.uiState.value.editingTransaction)
    }

    @Test
    fun filterByType_showsOnlyMatchingTransactions() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = salaryCategory.id,
            type = TransactionType.INCOME,
            amountMajor = BigDecimal("1000.00"),
            date = LocalDate.of(2026, 9, 1),
            note = "Monthly salary",
            merchant = "Employer"
        )

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("200.00"),
            date = LocalDate.of(2026, 9, 2),
            note = "Groceries",
            merchant = "Store"
        )

        assertEquals(2, viewModel.uiState.value.groups.flatMap { it.items }.size)

        // Filter to INCOME
        viewModel.setTypeFilter(TransactionType.INCOME)
        val incomeItems = viewModel.uiState.value.groups.flatMap { it.items }
        assertEquals(1, incomeItems.size)
        assertEquals(TransactionType.INCOME, incomeItems.first().type)

        // Filter to EXPENSE
        viewModel.setTypeFilter(TransactionType.EXPENSE)
        val expenseItems = viewModel.uiState.value.groups.flatMap { it.items }
        assertEquals(1, expenseItems.size)
        assertEquals(TransactionType.EXPENSE, expenseItems.first().type)

        // Clear filter
        viewModel.setTypeFilter(null)
        assertEquals(2, viewModel.uiState.value.groups.flatMap { it.items }.size)
    }

    @Test
    fun searchQuery_matchesMerchantNoteAndCategory() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("50.00"),
            date = LocalDate.of(2026, 9, 5),
            note = "Milk and eggs",
            merchant = "Local Dairy"
        )

        // Search by merchant
        viewModel.setSearchQuery("dairy")
        assertEquals(1, viewModel.uiState.value.groups.flatMap { it.items }.size)

        // Search by note
        viewModel.setSearchQuery("eggs")
        assertEquals(1, viewModel.uiState.value.groups.flatMap { it.items }.size)

        // Search non-matching
        viewModel.setSearchQuery("hardware")
        assertEquals(0, viewModel.uiState.value.groups.flatMap { it.items }.size)

        // Clear query
        viewModel.setSearchQuery("")
        assertEquals(1, viewModel.uiState.value.groups.flatMap { it.items }.size)
    }

    @Test
    fun deleteTransaction_andUndo_restoresTransaction() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveTransaction(
            accountId = checkingAccount.id,
            categoryId = groceriesCategory.id,
            type = TransactionType.EXPENSE,
            amountMajor = BigDecimal("75.00"),
            date = LocalDate.of(2026, 9, 8),
            note = "Dinner",
            merchant = "Bistro"
        )

        val txId = transactionRepo.transactionsFlow.value.first().id
        assertEquals(1, viewModel.uiState.value.groups.flatMap { it.items }.size)

        // Soft delete
        viewModel.deleteTransaction(txId)
        assertEquals(0, viewModel.uiState.value.groups.flatMap { it.items }.size)
        assertEquals(txId, viewModel.uiState.value.lastDeletedTransactionId)
        assertNotNull(transactionRepo.transactionsFlow.value.first().deletedAtEpochMillis)

        // Undo delete
        viewModel.undoDelete()
        assertEquals(1, viewModel.uiState.value.groups.flatMap { it.items }.size)
        assertNull(viewModel.uiState.value.lastDeletedTransactionId)
        assertNull(transactionRepo.transactionsFlow.value.first().deletedAtEpochMillis)
    }

    @Test
    fun monthNavigation_switchesActiveMonth() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(YearMonth.of(2026, 9), viewModel.uiState.value.selectedMonth)

        viewModel.nextMonth()
        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.selectedMonth)

        viewModel.previousMonth()
        assertEquals(YearMonth.of(2026, 9), viewModel.uiState.value.selectedMonth)
    }
}
