package com.premraj.moneyboard.feature.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlanningViewModel(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val categoryRepository: CategoryRepository,
    private val defaultCurrencyCode: String = "INR",
    private val locale: Locale = Locale.forLanguageTag("en-IN")
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val isAddIncomeDialogOpen = MutableStateFlow(false)
    private val editingIncomePlan = MutableStateFlow<MonthlyIncomePlan?>(null)
    private val editingCategoryPlan = MutableStateFlow<CategoryPlanItemUi?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val incomePlansFlow = selectedMonth.flatMapLatest { month ->
        monthlyPlanRepository.observeIncomePlans(month)
    }

    private val categoryPlansFlow = selectedMonth.flatMapLatest { month ->
        monthlyPlanRepository.observeCategoryPlans(month)
    }

    val uiState: StateFlow<PlanningUiState> = combine(
        selectedMonth,
        incomePlansFlow,
        categoryPlansFlow,
        categoryRepository.observeActiveCategories(),
        combine(
            isAddIncomeDialogOpen,
            editingIncomePlan,
            editingCategoryPlan,
            errorMessage
        ) { addIncome, editIncome, editCategory, error ->
            DialogState(addIncome, editIncome, editCategory, error)
        }
    ) { month, incomePlans, categoryPlans, categories, dialogState ->
        val planMap = categoryPlans.associateBy { it.categoryId }

        // Map income plans
        var totalIncomeMinor = 0L
        val incomeItems = incomePlans.map { plan ->
            totalIncomeMinor += plan.plannedAmount.amountMinor
            IncomePlanItemUi(
                id = plan.id,
                name = plan.name,
                amountText = MoneyFormatter.format(plan.plannedAmount, locale),
                amountMinor = plan.plannedAmount.amountMinor,
                raw = plan
            )
        }

        // Map category plans
        var totalAllocatedMinor = 0L

        fun mapCategory(cat: Category): CategoryPlanItemUi {
            val plan = planMap[cat.id]
            val amountMinor = plan?.plannedAmount?.amountMinor ?: 0L
            totalAllocatedMinor += amountMinor
            val money = Money(amountMinor, defaultCurrencyCode)
            return CategoryPlanItemUi(
                categoryId = cat.id,
                categoryName = cat.name,
                iconKey = cat.iconKey,
                type = cat.type,
                plannedAmountText = if (amountMinor > 0) MoneyFormatter.format(money, locale) else "—",
                plannedAmountMinor = amountMinor,
                hasBudget = amountMinor > 0
            )
        }

        val expenseItems = categories
            .filter { it.type == CategoryType.EXPENSE }
            .map(::mapCategory)
            .sortedWith(compareByDescending<CategoryPlanItemUi> { it.plannedAmountMinor }.thenBy { it.categoryName })

        val savingItems = categories
            .filter { it.type == CategoryType.SAVING }
            .map(::mapCategory)
            .sortedWith(compareByDescending<CategoryPlanItemUi> { it.plannedAmountMinor }.thenBy { it.categoryName })

        val investmentItems = categories
            .filter { it.type == CategoryType.INVESTMENT }
            .map(::mapCategory)
            .sortedWith(compareByDescending<CategoryPlanItemUi> { it.plannedAmountMinor }.thenBy { it.categoryName })

        val remainingMinor = totalIncomeMinor - totalAllocatedMinor

        val summary = PlanningSummaryUi(
            totalIncomeText = MoneyFormatter.format(Money(totalIncomeMinor, defaultCurrencyCode), locale),
            totalAllocatedText = MoneyFormatter.format(Money(totalAllocatedMinor, defaultCurrencyCode), locale),
            remainingBalanceText = MoneyFormatter.format(Money(remainingMinor, defaultCurrencyCode), locale),
            isPositive = remainingMinor >= 0
        )

        PlanningUiState(
            isLoading = false,
            selectedMonth = month,
            incomePlans = incomeItems,
            expensePlans = expenseItems,
            savingPlans = savingItems,
            investmentPlans = investmentItems,
            allCategories = categories,
            summary = summary,
            isAddIncomeDialogOpen = dialogState.isAddIncomeOpen,
            editingIncomePlan = dialogState.editingIncome,
            editingCategoryPlan = dialogState.editingCategory,
            errorMessage = dialogState.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PlanningUiState()
    )

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun openAddIncomeDialog() {
        editingIncomePlan.value = null
        isAddIncomeDialogOpen.value = true
    }

    fun openEditIncomeDialog(plan: MonthlyIncomePlan) {
        editingIncomePlan.value = plan
        isAddIncomeDialogOpen.value = true
    }

    fun closeIncomeDialog() {
        isAddIncomeDialogOpen.value = false
        editingIncomePlan.value = null
    }

    fun openEditCategoryPlanDialog(plan: CategoryPlanItemUi) {
        editingCategoryPlan.value = plan
    }

    fun closeCategoryPlanDialog() {
        editingCategoryPlan.value = null
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun saveIncomePlan(name: String, amountMajor: BigDecimal) {
        viewModelScope.launch {
            try {
                val money = Money.fromMajor(amountMajor, defaultCurrencyCode)
                val editing = editingIncomePlan.value
                monthlyPlanRepository.setIncomePlan(
                    SetIncomePlanCommand(
                        id = editing?.id,
                        month = selectedMonth.value,
                        name = name.trim(),
                        plannedAmount = money
                    )
                )
                closeIncomeDialog()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to save income target"
            }
        }
    }

    fun deleteIncomePlan(id: String) {
        viewModelScope.launch {
            try {
                monthlyPlanRepository.deleteIncomePlan(id)
                closeIncomeDialog()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to delete income target"
            }
        }
    }

    fun saveCategoryPlan(categoryId: String, amountMajor: BigDecimal) {
        viewModelScope.launch {
            try {
                val money = Money.fromMajor(amountMajor, defaultCurrencyCode)
                monthlyPlanRepository.setCategoryPlan(
                    SetCategoryPlanCommand(
                        month = selectedMonth.value,
                        categoryId = categoryId,
                        plannedAmount = money
                    )
                )
                closeCategoryPlanDialog()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to set category budget"
            }
        }
    }

    private data class DialogState(
        val isAddIncomeOpen: Boolean,
        val editingIncome: MonthlyIncomePlan?,
        val editingCategory: CategoryPlanItemUi?,
        val error: String?
    )

    companion object {
        fun factory(
            monthlyPlanRepository: MonthlyPlanRepository,
            categoryRepository: CategoryRepository,
            defaultCurrencyCode: String = "INR"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlanningViewModel(
                    monthlyPlanRepository = monthlyPlanRepository,
                    categoryRepository = categoryRepository,
                    defaultCurrencyCode = defaultCurrencyCode
                ) as T
            }
        }
    }
}
