package com.premraj.moneyboard.feature

import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.feature.accounts.AccountsViewModel
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
import java.time.Instant
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var accountRepo: FakeAccountRepository
    private lateinit var transactionRepo: FakeTransactionRepository
    private lateinit var viewModel: AccountsViewModel

    private val bankAccount = Account(
        id = "acc-bank",
        name = "HDFC Bank",
        type = AccountType.BANK,
        openingBalance = Money(100000_00L, "INR"),
        includeInNetWorth = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    private val investmentAccount = Account(
        id = "acc-invest",
        name = "Zerodha",
        type = AccountType.INVESTMENT,
        openingBalance = Money(50000_00L, "INR"),
        includeInNetWorth = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    private val excludedAccount = Account(
        id = "acc-excluded",
        name = "Office Reimbursement",
        type = AccountType.WALLET,
        openingBalance = Money(20000_00L, "INR"),
        includeInNetWorth = false,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    @Before
    fun setUp() {
        accountRepo = FakeAccountRepository().apply {
            accountsFlow.value = listOf(bankAccount, investmentAccount, excludedAccount)
        }
        transactionRepo = FakeTransactionRepository()

        viewModel = AccountsViewModel(
            accountRepository = accountRepo,
            transactionRepository = transactionRepo,
            defaultCurrencyCode = "INR"
        )
    }

    @Test
    fun defaultState_computesBalancesAndNetWorthCorrectly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertEquals(3, state.accounts.size)

        // Net worth should be bank (100k) + invest (50k) = 150k. Excluded (20k) is omitted.
        val bankUi = state.accounts.first { it.id == "acc-bank" }
        assertEquals(100000_00L, bankUi.currentBalanceMinor)
        assertTrue(bankUi.isPositive)

        assertTrue(state.summary.isNetWorthPositive)
    }

    @Test
    fun transactions_adjustAccountCurrentBalance() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Add income transaction of 20,000 to bank account
        transactionRepo.transactionsFlow.value = listOf(
            FinanceTransaction(
                id = "tx-1",
                accountId = "acc-bank",
                categoryId = "cat-salary",
                type = TransactionType.INCOME,
                amount = Money(20000_00L, "INR"),
                accountingDate = LocalDate.of(2026, 9, 1),
                occurredAt = Instant.now(),
                note = "Salary",
                merchant = null,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L,
                deletedAtEpochMillis = null
            ),
            FinanceTransaction(
                id = "tx-2",
                accountId = "acc-bank",
                categoryId = "cat-rent",
                type = TransactionType.EXPENSE,
                amount = Money(25000_00L, "INR"),
                accountingDate = LocalDate.of(2026, 9, 2),
                occurredAt = Instant.now(),
                note = "Rent",
                merchant = null,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L,
                deletedAtEpochMillis = null
            )
        )

        val state = viewModel.uiState.value
        val bankUi = state.accounts.first { it.id == "acc-bank" }
        // 100,000 + 20,000 - 25,000 = 95,000
        assertEquals(95000_00L, bankUi.currentBalanceMinor)
    }

    @Test
    fun createAccount_addsNewAccount() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.createAccount(
            name = "Cash in Hand",
            type = AccountType.CASH,
            openingBalanceMajor = BigDecimal("3000.00"),
            includeInNetWorth = true
        )

        val state = viewModel.uiState.value
        assertEquals(4, state.accounts.size)
        val cash = state.accounts.first { it.name == "Cash in Hand" }
        assertEquals(AccountType.CASH, cash.type)
        assertEquals(3000_00L, cash.currentBalanceMinor)
    }

    @Test
    fun archiveAccount_removesFromActiveList() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val target = viewModel.uiState.value.accounts.first { it.id == "acc-bank" }
        viewModel.promptArchiveAccount(target)
        assertEquals(target.id, viewModel.uiState.value.accountToArchive?.id)

        viewModel.confirmArchiveAccount()
        assertNull(viewModel.uiState.value.accountToArchive)

        val state = viewModel.uiState.value
        assertEquals(2, state.accounts.size)
        assertTrue(state.accounts.none { it.id == "acc-bank" })
    }

    @Test
    fun dialogState_openAndClose() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertFalse(viewModel.uiState.value.isAddAccountDialogOpen)
        viewModel.openAddAccountDialog()
        assertTrue(viewModel.uiState.value.isAddAccountDialogOpen)
        viewModel.closeAddAccountDialog()
        assertFalse(viewModel.uiState.value.isAddAccountDialogOpen)
    }
}
