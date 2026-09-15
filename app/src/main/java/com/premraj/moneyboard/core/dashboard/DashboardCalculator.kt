package com.premraj.moneyboard.core.dashboard

import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardCategoryLine
import com.premraj.moneyboard.core.domain.model.DashboardMetrics
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth

object DashboardCalculator {

    fun calculate(
        month: YearMonth,
        currencyCode: String,
        incomePlans: List<MonthlyIncomePlan>,
        categoryPlans: List<MonthlyCategoryPlan>,
        transactions: List<FinanceTransaction>,
        categories: List<Category>
    ): MonthlyDashboardSnapshot {

        val categoryMap = categories.associateBy(Category::id)

        val planIncome = incomePlans.filter {
            it.month == month && it.plannedAmount.currencyCode == currencyCode
        }

        val plans = categoryPlans.filter {
            it.month == month && it.plannedAmount.currencyCode == currencyCode
        }

        val tx = transactions.filter {
            YearMonth.from(it.accountingDate) == month &&
                it.amount.currencyCode == currencyCode &&
                it.deletedAtEpochMillis == null
        }

        val plannedLines = plans.map { plan ->
            val category = requireNotNull(categoryMap[plan.categoryId]) {
                "Missing category ${plan.categoryId}"
            }

            DashboardCategoryLine(
                categoryId = category.id,
                name = category.name,
                iconKey = category.iconKey,
                type = category.type,
                amount = plan.plannedAmount,
                sortOrder = category.sortOrder
            )
        }.sortedWith(compareBy(DashboardCategoryLine::sortOrder, DashboardCategoryLine::name))

        val actualLines = tx
            .filter {
                it.type == TransactionType.EXPENSE ||
                    it.type == TransactionType.SAVING ||
                    it.type == TransactionType.INVESTMENT
            }
            .groupBy(FinanceTransaction::categoryId)
            .map { (categoryId, items) ->
                val category = requireNotNull(categoryMap[categoryId]) {
                    "Missing category $categoryId"
                }

                DashboardCategoryLine(
                    categoryId = category.id,
                    name = category.name,
                    iconKey = category.iconKey,
                    type = category.type,
                    amount = Money.sum(items.map(FinanceTransaction::amount), currencyCode),
                    sortOrder = category.sortOrder
                )
            }
            .sortedWith(compareBy(DashboardCategoryLine::sortOrder, DashboardCategoryLine::name))

        val plannedIncome = Money.sum(
            planIncome.map(MonthlyIncomePlan::plannedAmount),
            currencyCode
        )

        val actualIncome = Money.sum(
            tx.filter { it.type == TransactionType.INCOME }
                .map(FinanceTransaction::amount),
            currencyCode
        )

        return MonthlyDashboardSnapshot(
            month = month,
            currencyCode = currencyCode,
            planned = metrics(plannedIncome, plannedLines, currencyCode),
            actual = metrics(actualIncome, actualLines, currencyCode),
            plannedCategoryLines = plannedLines,
            actualCategoryLines = actualLines,
            hasPlan = planIncome.isNotEmpty() || plans.isNotEmpty(),
            hasActualActivity = tx.isNotEmpty()
        )
    }

    private fun metrics(
        income: Money,
        lines: List<DashboardCategoryLine>,
        currencyCode: String
    ): DashboardMetrics {
        fun sum(type: CategoryType) = Money.sum(
            lines.filter { it.type == type }.map(DashboardCategoryLine::amount),
            currencyCode
        )

        val expenses = sum(CategoryType.EXPENSE)
        val savings = sum(CategoryType.SAVING)
        val investments = sum(CategoryType.INVESTMENT)
        val allocation = expenses + savings + investments
        val remaining = income - allocation
        val saved = savings + investments

        return DashboardMetrics(
            income = income,
            expenses = expenses,
            savings = savings,
            investments = investments,
            totalAllocation = allocation,
            remaining = remaining,
            savingsRateBasisPoints = basisPoints(saved.amountMinor, income.amountMinor)
        )
    }

    private fun basisPoints(
        numerator: Long,
        denominator: Long
    ): Int? {
        if (denominator <= 0L) return null

        return BigDecimal.valueOf(numerator)
            .multiply(BigDecimal.valueOf(10_000L))
            .divide(BigDecimal.valueOf(denominator), 0, RoundingMode.HALF_UP)
            .intValueExact()
    }
}
