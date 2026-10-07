package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardCategoryLine
import com.premraj.moneyboard.core.domain.model.DashboardMetrics
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import java.util.Locale

/**
 * Maps domain [MonthlyDashboardSnapshot] into UI-ready models.
 * All monetary formatting goes through [MoneyFormatter].
 * No hard-coded currency symbols.
 */
object DashboardUiMapper {

    fun map(
        snapshot: MonthlyDashboardSnapshot,
        mode: DashboardMode,
        locale: Locale = Locale.getDefault()
    ): FinanceBoardUiModel {
        val metrics = when (mode) {
            DashboardMode.PLAN -> snapshot.planned
            DashboardMode.ACTUAL -> snapshot.actual
        }
        val lines = when (mode) {
            DashboardMode.PLAN -> snapshot.plannedCategoryLines
            DashboardMode.ACTUAL -> snapshot.actualCategoryLines
        }

        val expenseRows = lines
            .filter { it.type == CategoryType.EXPENSE }
            .mapIndexed { index, line ->
                ExpenseRowUi(
                    number = index + 1,
                    title = line.name,
                    amountText = MoneyFormatter.formatCompact(line.amount, locale),
                    icon = categoryIcon(line.iconKey)
                )
            }

        val savingsLines = lines
            .filter { it.type == CategoryType.SAVING || it.type == CategoryType.INVESTMENT }
            .map { line ->
                SummaryLineUi(
                    label = line.name,
                    value = MoneyFormatter.formatCompact(line.amount, locale)
                )
            }

        val saved = metrics.savings + metrics.investments
        val savingsTotal = savingsLines + SummaryLineUi(
            label = "Total",
            value = MoneyFormatter.formatCompact(saved, locale)
        )

        val living = metrics.totalAllocation - saved
        val livingLines = listOf(
            SummaryLineUi(
                "Total Allocation",
                MoneyFormatter.formatCompact(metrics.totalAllocation, locale)
            ),
            SummaryLineUi(
                "Less Savings / Investments",
                MoneyFormatter.formatCompact(saved, locale)
            ),
            SummaryLineUi(
                if (mode == DashboardMode.PLAN) "Planned Living / Family / Travel" else "Actual Living / Family / Travel",
                MoneyFormatter.formatCompact(living, locale)
            )
        )

        val annualMetrics = metrics.annualized()
        val annualLines = listOf(
            SummaryLineUi(
                "Projected Allocation",
                MoneyFormatter.formatCompact(annualMetrics.totalAllocation, locale)
            ),
            SummaryLineUi(
                "Projected Income",
                MoneyFormatter.formatCompact(annualMetrics.income, locale)
            ),
            SummaryLineUi(
                "Projected Balance",
                MoneyFormatter.formatCompact(annualMetrics.remaining, locale)
            )
        )

        val salaryVsExpenses = listOf(
            SummaryLineUi(
                if (mode == DashboardMode.PLAN) "Planned Income" else "Actual Income",
                MoneyFormatter.formatCompact(metrics.income, locale)
            ),
            SummaryLineUi(
                "Total Allocation",
                MoneyFormatter.formatCompact(metrics.totalAllocation, locale)
            ),
            SummaryLineUi(
                "Remaining",
                MoneyFormatter.formatCompact(metrics.remaining, locale)
            )
        )

        val chartLines = lines.map { line ->
            ChartLineUi(
                label = line.name,
                amountMinor = line.amount.amountMinor,
                amountText = MoneyFormatter.formatCompact(line.amount, locale)
            )
        }

        return FinanceBoardUiModel(
            salaryText = MoneyFormatter.formatCompact(metrics.income, locale),
            totalAllocationText = MoneyFormatter.formatCompact(
                metrics.totalAllocation,
                locale
            ),
            expenseRows = expenseRows,
            salaryVsExpenses = salaryVsExpenses,
            savings = if (savingsLines.isEmpty()) emptyList() else savingsTotal,
            living = livingLines,
            annual = annualLines,
            chartLines = chartLines,
            remainingText = MoneyFormatter.formatCompact(metrics.remaining, locale),
            savingsRateText = MoneyFormatter.formatSavingsRate(
                metrics.savingsRateBasisPoints
            )
        )
    }
}
