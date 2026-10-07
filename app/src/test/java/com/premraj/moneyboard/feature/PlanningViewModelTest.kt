package com.premraj.moneyboard.feature

import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.feature.planning.PlanningViewModel
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
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class PlanningViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var monthlyPlanRepo: FakeMonthlyPlanRepository
    private lateinit var categoryRepo: FakeCategoryRepository
    private lateinit var viewModel: PlanningViewModel

    private val testMonth = YearMonth.of(2026, 9)

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

    private val emergencyCategory = Category(
        id = "cat-emergency",
        name = "Emergency Fund",
        iconKey = "saving",
        type = CategoryType.SAVING,
        group = CategoryGroup.SAVINGS,
        sortOrder = 2,
        systemCategory = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    private val sipCategory = Category(
        id = "cat-sip",
        name = "SIP Mutual Funds",
        iconKey = "invest",
        type = CategoryType.INVESTMENT,
        group = CategoryGroup.INVESTMENTS,
        sortOrder = 3,
        systemCategory = true,
        archived = false,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    @Before
    fun setUp() {
        monthlyPlanRepo = FakeMonthlyPlanRepository()
        categoryRepo = FakeCategoryRepository().apply {
            categoriesFlow.value = listOf(groceriesCategory, emergencyCategory, sipCategory)
        }

        viewModel = PlanningViewModel(
            monthlyPlanRepository = monthlyPlanRepo,
            categoryRepository = categoryRepo,
            defaultCurrencyCode = "INR"
        )
        viewModel.selectMonth(testMonth)
    }

    @Test
    fun defaultState_categorizesSectionsCorrectly() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        val state = viewModel.uiState.value
        assertEquals(1, state.expensePlans.size)
        assertEquals("Groceries", state.expensePlans.first().categoryName)
        assertEquals(1, state.savingPlans.size)
        assertEquals("Emergency Fund", state.savingPlans.first().categoryName)
        assertEquals(1, state.investmentPlans.size)
        assertEquals("SIP Mutual Funds", state.investmentPlans.first().categoryName)
        assertTrue(state.incomePlans.isEmpty())
        assertEquals(0L, state.expensePlans.first().plannedAmountMinor)
    }

    @Test
    fun saveIncomePlan_updatesIncomeAndRemainingBalance() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveIncomePlan("Primary Job", BigDecimal("50000.00"))

        val state = viewModel.uiState.value
        assertEquals(1, state.incomePlans.size)
        assertEquals("Primary Job", state.incomePlans.first().name)
        assertEquals(50000_00L, state.incomePlans.first().amountMinor)
        assertTrue(state.summary.isPositive)
    }

    @Test
    fun deleteIncomePlan_removesTargetAndUpdatesSummary() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.saveIncomePlan("Freelance", BigDecimal("10000.00"))
        assertEquals(1, viewModel.uiState.value.incomePlans.size)

        val planId = viewModel.uiState.value.incomePlans.first().id
        viewModel.deleteIncomePlan(planId)

        assertEquals(0, viewModel.uiState.value.incomePlans.size)
    }

    @Test
    fun saveCategoryPlan_allocatesBudgetToCategory() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // Add 50,000 income
        viewModel.saveIncomePlan("Job", BigDecimal("50000.00"))

        // Budget 15,000 for groceries
        viewModel.saveCategoryPlan(groceriesCategory.id, BigDecimal("15000.00"))

        val state = viewModel.uiState.value
        val expensePlan = state.expensePlans.first { it.categoryId == groceriesCategory.id }
        assertEquals(15000_00L, expensePlan.plannedAmountMinor)
        assertTrue(expensePlan.hasBudget)

        // Clear budget by setting 0
        viewModel.saveCategoryPlan(groceriesCategory.id, BigDecimal.ZERO)
        val clearedState = viewModel.uiState.value
        val clearedPlan = clearedState.expensePlans.first { it.categoryId == groceriesCategory.id }
        assertEquals(0L, clearedPlan.plannedAmountMinor)
        assertFalse(clearedPlan.hasBudget)
    }

    @Test
    fun dialogState_openAndCloseFlow() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        assertFalse(viewModel.uiState.value.isAddIncomeDialogOpen)
        viewModel.openAddIncomeDialog()
        assertTrue(viewModel.uiState.value.isAddIncomeDialogOpen)
        viewModel.closeIncomeDialog()
        assertFalse(viewModel.uiState.value.isAddIncomeDialogOpen)

        val catItem = viewModel.uiState.value.expensePlans.first()
        viewModel.openEditCategoryPlanDialog(catItem)
        assertEquals(catItem.categoryId, viewModel.uiState.value.editingCategoryPlan?.categoryId)
        viewModel.closeCategoryPlanDialog()
        assertNull(viewModel.uiState.value.editingCategoryPlan)
    }
}
