package com.premraj.moneyboard.core.domain.repository

import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

data class SetIncomePlanCommand(
    val id: String? = null,
    val month: YearMonth,
    val name: String,
    val plannedAmount: Money,
    val expectedDate: LocalDate? = null,
    val sortOrder: Int = 100
) {
    init {
        require(name.isNotBlank())
        require(name.length <= 60)
        require(plannedAmount.amountMinor > 0L)
        require(expectedDate == null || YearMonth.from(expectedDate) == month)
    }
}

data class SetCategoryPlanCommand(
    val month: YearMonth,
    val categoryId: String,
    val plannedAmount: Money
) {
    init {
        require(categoryId.isNotBlank())
        require(plannedAmount.amountMinor >= 0L)
    }
}

interface MonthlyPlanRepository {
    fun observeIncomePlans(month: YearMonth): Flow<List<MonthlyIncomePlan>>
    fun observeCategoryPlans(month: YearMonth): Flow<List<MonthlyCategoryPlan>>

    suspend fun setIncomePlan(
        command: SetIncomePlanCommand
    ): MonthlyIncomePlan

    suspend fun deleteIncomePlan(id: String)

    suspend fun setCategoryPlan(
        command: SetCategoryPlanCommand
    ): MonthlyCategoryPlan?

    suspend fun hasAnyPlan(month: YearMonth): Boolean
}
