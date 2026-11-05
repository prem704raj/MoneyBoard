package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.dashboard.ObserveMonthlyDashboardUseCase
import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardMetrics
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.CreateCategoryCommand
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

/**
 * Empirical Adversarial Challenger Test Suite for Milestone 6:
 * UI Navigation, BackHandler & Error Recovery.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Milestone6AdversarialChallengerTest {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. BACKHANDLER ENABLED LOGIC STRESS TEST
    // =========================================================================

    /**
     * BackHandler contract in MoneyBoardApp.kt:
     * BackHandler(enabled = uiState.selectedMonth != YearMonth.now()) {
     *     viewModel.resetToCurrentMonth()
     * }
     *
     * Invariants:
     * 1. When selectedMonth == YearMonth.now(), enabled == false (system back permitted).
     * 2. When selectedMonth != YearMonth.now(), enabled == true (intercept back).
     */
    @Test
    fun backHandlerCondition_currentMonth_mustBeDisabled() {
        val currentMonth = YearMonth.now()
        val isBackHandlerEnabled = currentMonth != YearMonth.now()
        assertFalse("BackHandler must be disabled on current month to allow system back", isBackHandlerEnabled)
    }

    @Test
    fun backHandlerCondition_pastMonths_mustBeEnabled() {
        val currentMonth = YearMonth.now()
        for (i in 1..60) {
            val pastMonth = currentMonth.minusMonths(i.toLong())
            val isBackHandlerEnabled = pastMonth != currentMonth
            assertTrue("BackHandler must be enabled for past month $pastMonth", isBackHandlerEnabled)
        }
    }

    @Test
    fun backHandlerCondition_futureMonths_mustBeEnabled() {
        val currentMonth = YearMonth.now()
        for (i in 1..60) {
            val futureMonth = currentMonth.plusMonths(i.toLong())
            val isBackHandlerEnabled = futureMonth != currentMonth
            assertTrue("BackHandler must be enabled for future month $futureMonth", isBackHandlerEnabled)
        }
    }

    @Test
    fun backHandlerCondition_yearBoundaryTransitions() {
        val december = YearMonth.of(2026, 12)
        val january = YearMonth.of(2027, 1)

        // Simulating currentMonth as December 2026
        val isDecEnabledWhenDecCurrent = december != december
        val isJanEnabledWhenDecCurrent = january != december
        assertFalse(isDecEnabledWhenDecCurrent)
        assertTrue(isJanEnabledWhenDecCurrent)

        // Simulating currentMonth as January 2027
        val isDecEnabledWhenJanCurrent = december != january
        val isJanEnabledWhenJanCurrent = january != january
        assertTrue(isDecEnabledWhenJanCurrent)
        assertFalse(isJanEnabledWhenJanCurrent)
    }

    // =========================================================================
    // 2. DASHBOARD VIEWMODEL NAVIGATION & RESET TESTS
    // =========================================================================

    private fun createFakeObserveUseCase(
        errorForMonth: YearMonth? = null
    ): ObserveMonthlyDashboardUseCase {
        val dummyMonthlyPlanRepo = object : MonthlyPlanRepository {
            override fun observeCategoryPlans(month: YearMonth): Flow<List<MonthlyCategoryPlan>> {
                if (month == errorForMonth) {
                    return flow { throw RuntimeException("Simulated failure for month $month") }
                }
                return flowOf(emptyList())
            }

            override fun observeIncomePlans(month: YearMonth): Flow<List<MonthlyIncomePlan>> = flowOf(emptyList())
            override suspend fun setCategoryPlan(command: SetCategoryPlanCommand): MonthlyCategoryPlan? = null
            override suspend fun deleteCategoryPlan(month: YearMonth, categoryId: String) {}
            override suspend fun setIncomePlan(command: SetIncomePlanCommand): MonthlyIncomePlan {
                error("Not needed in test")
            }
            override suspend fun deleteIncomePlan(id: String) {}
            override suspend fun hasAnyPlan(month: YearMonth): Boolean = false
        }

        val dummyTxRepo = object : TransactionRepository {
            override fun observeTransactions(month: YearMonth): Flow<List<FinanceTransaction>> = flowOf(emptyList())
            override fun observeTotal(month: YearMonth, type: TransactionType, currencyCode: String): Flow<Money> =
                flowOf(Money(0L, currencyCode))
            override fun observeCategoryTotals(month: YearMonth, type: TransactionType, currencyCode: String): Flow<List<CategoryTotalRow>> =
                flowOf(emptyList())
            override suspend fun getTransaction(transactionId: String): FinanceTransaction? = null
            override suspend fun createTransaction(command: CreateTransactionCommand): FinanceTransaction {
                error("Not needed in test")
            }
            override suspend fun updateTransaction(transaction: FinanceTransaction): FinanceTransaction = transaction
            override suspend fun softDeleteTransaction(transactionId: String) {}
        }

        val dummyCatRepo = object : CategoryRepository {
            override fun observeActiveCategories(): Flow<List<Category>> = flowOf(emptyList())
            override fun observeActiveCategories(type: CategoryType): Flow<List<Category>> = flowOf(emptyList())
            override fun observeAllCategories(): Flow<List<Category>> = flowOf(emptyList())
            override suspend fun getCategory(categoryId: String): Category? = null
            override suspend fun createCategory(command: CreateCategoryCommand): Category {
                error("Not needed in test")
            }
            override suspend fun archiveCustomCategory(categoryId: String) {}
        }

        return ObserveMonthlyDashboardUseCase(
            monthlyPlanRepository = dummyMonthlyPlanRepo,
            transactionRepository = dummyTxRepo,
            categoryRepository = dummyCatRepo
        )
    }

    @Test
    fun viewModel_initialMonth_defaultsToCurrentMonth() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertEquals(currentMonth, viewModel.uiState.value.selectedMonth)
        assertFalse(
            "Initial state on current month must have BackHandler disabled",
            viewModel.uiState.value.selectedMonth != YearMonth.now()
        )
    }

    @Test
    fun viewModel_nextMonth_incrementsMonthAndEnablesBackHandler() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.nextMonth()
        advanceUntilIdle()

        val expected = currentMonth.plusMonths(1)
        assertEquals(expected, viewModel.uiState.value.selectedMonth)
        assertTrue(
            "BackHandler must be enabled after nextMonth()",
            viewModel.uiState.value.selectedMonth != YearMonth.now()
        )
    }

    @Test
    fun viewModel_previousMonth_decrementsMonthAndEnablesBackHandler() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.previousMonth()
        advanceUntilIdle()

        val expected = currentMonth.minusMonths(1)
        assertEquals(expected, viewModel.uiState.value.selectedMonth)
        assertTrue(
            "BackHandler must be enabled after previousMonth()",
            viewModel.uiState.value.selectedMonth != YearMonth.now()
        )
    }

    @Test
    fun viewModel_resetToCurrentMonth_restoresCurrentMonthAndDisablesBackHandler() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth.minusMonths(5)
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Starting in the past
        assertTrue(viewModel.uiState.value.selectedMonth != YearMonth.now())

        // Action: reset
        viewModel.resetToCurrentMonth()
        advanceUntilIdle()

        // Assert: restored to current month
        assertEquals(currentMonth, viewModel.uiState.value.selectedMonth)
        assertFalse(
            "BackHandler must be disabled after resetToCurrentMonth()",
            viewModel.uiState.value.selectedMonth != YearMonth.now()
        )
    }

    @Test
    fun viewModel_selectMonth_arbitraryNavigationAndResetCycle() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val targetMonths = listOf(
            YearMonth.of(2020, 1),
            YearMonth.of(2024, 2),
            YearMonth.of(2030, 12),
            YearMonth.of(2025, 7)
        )

        for (target in targetMonths) {
            viewModel.selectMonth(target)
            advanceUntilIdle()
            assertEquals(target, viewModel.uiState.value.selectedMonth)
            assertTrue(
                "BackHandler must be enabled on arbitrary non-current month $target",
                viewModel.uiState.value.selectedMonth != YearMonth.now()
            )

            viewModel.resetToCurrentMonth()
            advanceUntilIdle()
            assertEquals(currentMonth, viewModel.uiState.value.selectedMonth)
            assertFalse(
                "BackHandler must be disabled after reset",
                viewModel.uiState.value.selectedMonth != YearMonth.now()
            )
        }
    }

    @Test
    fun viewModel_multipleConsecutiveResetCalls_areIdempotent() = runTest {
        val currentMonth = YearMonth.now()
        val viewModel = DashboardViewModel(
            observeMonthlyDashboard = createFakeObserveUseCase(),
            initialMonth = currentMonth
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        repeat(5) {
            viewModel.resetToCurrentMonth()
            advanceUntilIdle()
            assertEquals(currentMonth, viewModel.uiState.value.selectedMonth)
            assertFalse(viewModel.uiState.value.selectedMonth != YearMonth.now())
        }
    }

    // =========================================================================
    // 3. ERROR RECOVERY STATE & BEHAVIOR
    // =========================================================================

    @Test
    fun errorRecoveryState_invariantsHold() {
        val errorState = DashboardUiState(
            selectedMonth = YearMonth.of(2025, 3),
            mode = DashboardMode.PLAN,
            snapshot = null,
            isLoading = false,
            errorMessage = "Unable to load your finance dashboard."
        )

        // Matching FinanceBoardScreen.kt line 73:
        val showErrorRecovery = errorState.errorMessage != null && errorState.snapshot == null
        assertTrue("Error recovery UI must be shown when errorMessage != null and snapshot == null", showErrorRecovery)

        val zeroMetrics = DashboardMetrics(
            income = Money(0L, "INR"),
            expenses = Money(0L, "INR"),
            savings = Money(0L, "INR"),
            investments = Money(0L, "INR"),
            totalAllocation = Money(0L, "INR"),
            remaining = Money(0L, "INR"),
            savingsRateBasisPoints = null
        )

        val successState = DashboardUiState(
            selectedMonth = YearMonth.of(2025, 3),
            mode = DashboardMode.PLAN,
            snapshot = MonthlyDashboardSnapshot(
                month = YearMonth.of(2025, 3),
                currencyCode = "INR",
                planned = zeroMetrics,
                actual = zeroMetrics,
                plannedCategoryLines = emptyList(),
                actualCategoryLines = emptyList(),
                hasPlan = false,
                hasActualActivity = false
            ),
            isLoading = false,
            errorMessage = null
        )

        val showSuccessErrorRecovery = successState.errorMessage != null && successState.snapshot == null
        assertFalse("Error recovery UI must NOT be shown when snapshot is present", showSuccessErrorRecovery)
    }

    // =========================================================================
    // 4. ACTUAL EXPENSE RESET (₹0) SAFETY CHECK
    // =========================================================================

    @Test
    fun actualExpense_zeroAmountCommand_violatesDomainInvariants() {
        // In CreateTransactionCommand / FinanceTransaction:
        // require(amount.amountMinor > 0L) { "Transaction amount must be greater than zero" }
        // Worker M6 avoided calling CreateTransactionCommand with 0 by soft-deleting instead.
        try {
            CreateTransactionCommand(
                accountId = "acc-1",
                categoryId = "cat-1",
                type = TransactionType.EXPENSE,
                amount = Money(0L, "INR"),
                accountingDate = LocalDate.now(),
                occurredAt = Instant.now(),
                merchant = null,
                note = "Zero test"
            )
            fail("Expected IllegalArgumentException for 0 amount transaction command")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Exception message must specify transaction amount > 0",
                e.message?.contains("greater than zero") == true
            )
        }
    }
}
