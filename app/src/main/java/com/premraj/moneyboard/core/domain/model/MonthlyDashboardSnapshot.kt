package com.premraj.moneyboard.core.domain.model

import java.time.YearMonth

data class MonthlyDashboardSnapshot(
    val month: YearMonth,
    val currencyCode: String,
    val planned: DashboardMetrics,
    val actual: DashboardMetrics,
    val plannedCategoryLines: List<DashboardCategoryLine>,
    val actualCategoryLines: List<DashboardCategoryLine>,
    val hasPlan: Boolean,
    val hasActualActivity: Boolean
)
