package com.premraj.moneyboard.feature.dashboard

data class FinanceBoardUiModel(
    val salaryText: String,
    val totalAllocationText: String,
    val expenseRows: List<ExpenseRowUi>,
    val salaryVsExpenses: List<SummaryLineUi>,
    val savings: List<SummaryLineUi>,
    val living: List<SummaryLineUi>,
    val annual: List<SummaryLineUi>,
    val chartLines: List<ChartLineUi>,
    val remainingText: String,
    val savingsRateText: String
)

data class ChartLineUi(
    val label: String,
    val amountMinor: Long,
    val amountText: String
)
