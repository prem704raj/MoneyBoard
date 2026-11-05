package com.premraj.moneyboard.core.settings

import com.premraj.moneyboard.core.domain.model.DashboardMode

data class UserSettings(
    val cityName: String = "Pune",
    val profileTag: String = "Single Person",
    val emiTag: String = "",
    val inHandSalaryRupees: Long = 70_000L,
    val currencyCode: String = "INR",
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val defaultMode: DashboardMode = DashboardMode.PLAN,
    val textScale: Float = 1.0f
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
