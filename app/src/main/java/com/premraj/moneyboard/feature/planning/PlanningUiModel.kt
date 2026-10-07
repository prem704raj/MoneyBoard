package com.premraj.moneyboard.feature.planning

import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import java.time.YearMonth

data class IncomePlanItemUi(
    val id: String,
    val name: String,
    val amountText: String,
    val amountMinor: Long,
    val raw: MonthlyIncomePlan
)

data class CategoryPlanItemUi(
    val categoryId: String,
    val categoryName: String,
    val iconKey: String,
    val type: CategoryType,
    val plannedAmountText: String,
    val plannedAmountMinor: Long,
    val hasBudget: Boolean
)

data class PlanningSummaryUi(
    val totalIncomeText: String,
    val totalAllocatedText: String,
    val remainingBalanceText: String,
    val isPositive: Boolean
)

data class PlanningUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val incomePlans: List<IncomePlanItemUi> = emptyList(),
    val expensePlans: List<CategoryPlanItemUi> = emptyList(),
    val savingPlans: List<CategoryPlanItemUi> = emptyList(),
    val investmentPlans: List<CategoryPlanItemUi> = emptyList(),
    val allCategories: List<Category> = emptyList(),
    val summary: PlanningSummaryUi = PlanningSummaryUi("₹0", "₹0", "₹0", true),
    val editingIncomePlan: MonthlyIncomePlan? = null,
    val isAddIncomeDialogOpen: Boolean = false,
    val editingCategoryPlan: CategoryPlanItemUi? = null,
    val errorMessage: String? = null
)
