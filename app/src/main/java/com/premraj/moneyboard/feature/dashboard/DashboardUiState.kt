package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import java.time.YearMonth

data class DashboardUiState(
    val selectedMonth: YearMonth,
    val mode: DashboardMode = DashboardMode.PLAN,
    val snapshot: MonthlyDashboardSnapshot? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
