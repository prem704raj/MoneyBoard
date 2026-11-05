package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import com.premraj.moneyboard.core.validation.EmojiValidator
import com.premraj.moneyboard.feature.dashboard.components.CategoryIconMapper
import com.premraj.moneyboard.feature.dashboard.components.DonutSliceUi
import com.premraj.moneyboard.feature.dashboard.components.defaultDonutSlices
import java.math.BigDecimal

data class FinanceBoardUiModel(
    val salaryText: String,
    val totalAllocationText: String,
    val expenseRows: List<ExpenseRowUi>,
    val salaryVsExpenses: List<SummaryLineUi>,
    val savings: List<SummaryLineUi>,
    val living: List<SummaryLineUi>,
    val annual: List<SummaryLineUi>,
    val chartLines: List<ChartLineUi>,
    val donutSlices: List<DonutSliceUi>,
    val remainingText: String,
    val savingsRateText: String,
    val totalSavingsText: String,
    val actualSpendingText: String,
    val annualBalanceText: String,
    val actualTotalAllocationMinor: Long = 0L,
    val planTotalAllocationMinor: Long = 0L,
    val mode: DashboardMode = DashboardMode.PLAN
)

data class ChartLineUi(
    val label: String,
    val amountMinor: Long,
    val amountText: String
)

fun isPresetEssential(categoryId: String, title: String = ""): Boolean {
    val cat = categoryId.lowercase().trim()
    val t = title.lowercase().trim()
    return when {
        cat == SeedIds.EXPENSE_RENT || t.contains("rent") -> true
        cat == SeedIds.EXPENSE_GROCERIES || t.contains("groceries") -> true
        cat == SeedIds.EXPENSE_GAS || t.contains("gas") -> true
        cat == SeedIds.EXPENSE_HOUSE_HELP || t.contains("maid") -> true
        cat == SeedIds.EXPENSE_DINING || t.contains("online food") || t.contains("dining") -> true
        cat == SeedIds.EXPENSE_ELECTRICITY || t.contains("electricity") -> true
        cat == SeedIds.EXPENSE_WIFI || t.contains("wi-fi") || t.contains("wifi") -> true
        cat == SeedIds.EXPENSE_MOBILE || t.contains("mobile") -> true
        cat == SeedIds.EXPENSE_CAB || t.contains("cab") || t.contains("rapido") -> true
        cat == SeedIds.EXPENSE_LAUNDRY || t.contains("laundry") -> true
        cat == SeedIds.EXPENSE_SHOPPING || t.contains("shopping") -> true
        cat == SeedIds.EXPENSE_GYM || (t.contains("gym") && !t.contains("diet")) -> true
        else -> false
    }
}

fun MonthlyDashboardSnapshot.toFinanceBoardUi(
    mode: DashboardMode
): FinanceBoardUiModel {
    val metrics = if (mode == DashboardMode.PLAN) planned else actual
    val lines = if (mode == DashboardMode.PLAN) plannedCategoryLines else actualCategoryLines
    val plannedMap = plannedCategoryLines.associateBy { it.categoryId }
    val actualMap = actualCategoryLines.associateBy { it.categoryId }

    val allocationRows = lines.mapIndexed { index, line ->
        val rawIcon = line.iconKey.substringBefore("|").trim()
        val badge = categoryBadge(rawIcon)
        val isEmoji = EmojiValidator.isValidEmoji(rawIcon)
        val presetDrawable = CategoryIconMapper.getPresetDrawable(line.categoryId, rawIcon)
        val isEssential = when {
            line.iconKey.contains("|essential") -> true
            line.iconKey.contains("|other") -> false
            else -> isPresetEssential(line.categoryId, line.name)
        }
        val actualMinor = actualMap[line.categoryId]?.amount?.amountMinor ?: 0L
        val planMinor = plannedMap[line.categoryId]?.amount?.amountMinor ?: line.amount.amountMinor

        ExpenseRowUi(
            categoryId = line.categoryId,
            number = index + 1,
            title = line.name,
            amountText = formatRupees(line.amount),
            amountMinor = line.amount.amountMinor,
            actualAmountMinor = actualMinor,
            actualAmountText = formatRupees(Money(actualMinor, line.amount.currencyCode)),
            plannedAmountMinor = planMinor,
            plannedAmountText = formatRupees(Money(planMinor, line.amount.currencyCode)),
            icon = badge.icon,
            iconDrawableRes = presetDrawable,
            iconEmoji = if (isEmoji) rawIcon else null,
            badgeColor = badge.backgroundColor,
            badgeIconTint = badge.iconTint,
            isCustom = isEmoji || presetDrawable == null,
            isEssential = isEssential
        )
    }

    val savingsLines = lines
        .filter {
            it.type == CategoryType.SAVING || it.type == CategoryType.INVESTMENT
        }
        .map {
            SummaryLineUi(
                label = it.name,
                value = formatMoney(it.amount)
            )
        }

    val totalSaved = metrics.savings + metrics.investments
    val actualSpending = metrics.expenses
    val annualExpenses = metrics.totalAllocation * 12
    val annualSalary = metrics.income * 12
    val annualBalance = annualSalary - annualExpenses

    val chartLines = lines
        .filter { it.amount.amountMinor > 0L }
        .sortedByDescending { it.amount.amountMinor }
        .map {
            ChartLineUi(
                label = it.name,
                amountMinor = it.amount.amountMinor,
                amountText = formatMoney(it.amount)
            )
        }

    return FinanceBoardUiModel(
        salaryText = "${formatMoney(metrics.income)} / month",
        totalAllocationText = formatMoney(metrics.totalAllocation),
        expenseRows = if (allocationRows.isNotEmpty()) {
            allocationRows
        } else if (mode == DashboardMode.ACTUAL) {
            emptyList()
        } else {
            DashboardPreviewData.expenses
        },

        salaryVsExpenses = listOf(
            SummaryLineUi("In-hand Salary", formatMoney(metrics.income)),
            SummaryLineUi("Total Expenses", formatMoney(metrics.totalAllocation))
        ),

        savings = if (savingsLines.isNotEmpty()) {
            savingsLines
        } else if (mode == DashboardMode.ACTUAL) {
            emptyList()
        } else {
            DashboardPreviewData.savings
        },

        living = listOf(
            SummaryLineUi("Total Expenses", formatMoney(metrics.totalAllocation)),
            SummaryLineUi("Less Savings/Investments", "(${formatMoney(totalSaved)})")
        ),

        annual = listOf(
            SummaryLineUi(
                label = "Total Allocation",
                value = formatMoney(annualExpenses),
                subLabel = "${formatMoney(metrics.totalAllocation)} × 12"
            ),
            SummaryLineUi(
                label = "Salary",
                value = formatMoney(annualSalary),
                subLabel = "${formatMoney(metrics.income)} × 12"
            )
        ),

        chartLines = chartLines,
        donutSlices = defaultDonutSlices(),
        remainingText = "${formatMoney(metrics.remaining)} / month",
        savingsRateText = formatBasisPoints(metrics.savingsRateBasisPoints),
        totalSavingsText = "${formatMoney(totalSaved)} / month",
        actualSpendingText = "${formatMoney(actualSpending)} / month",
        annualBalanceText = formatMoney(annualBalance),
        actualTotalAllocationMinor = actual.totalAllocation.amountMinor,
        planTotalAllocationMinor = planned.totalAllocation.amountMinor,
        mode = mode
    )
}

/**
 * Universal Indian numbering system formatter (e.g. 69,350; 8,32,200; 1,00,000).
 * Free from JVM/Android locale fragmentation.
 */
fun formatIndianRupees(amount: Long): String {
    val isNegative = amount < 0
    val absAmount = kotlin.math.abs(amount)
    val s = absAmount.toString()
    if (s.length <= 3) {
        return (if (isNegative) "-" else "") + s
    }
    val last3 = s.takeLast(3)
    val remaining = s.dropLast(3)
    val sb = StringBuilder()
    var count = 0
    for (i in remaining.length - 1 downTo 0) {
        sb.append(remaining[i])
        count++
        if (count % 2 == 0 && i > 0) {
            sb.append(',')
        }
    }
    val formatted = sb.reverse().toString() + "," + last3
    return (if (isNegative) "-" else "") + formatted
}

fun formatMoney(money: Money): String {
    return "₹" + formatIndianRupees(money.amountMinor / 100L)
}

fun formatRupees(money: Money): String {
    return formatIndianRupees(money.amountMinor / 100L)
}

private fun formatBasisPoints(basisPoints: Int?): String {
    if (basisPoints == null) return "—"
    val percent = BigDecimal.valueOf(basisPoints.toLong())
        .movePointLeft(2)
    return "$percent%"
}
