package com.premraj.moneyboard.core.settings

import android.content.Context
import android.content.SharedPreferences
import com.premraj.moneyboard.core.domain.model.DashboardMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readFromPrefs())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun readFromPrefs(): UserSettings {
        val city = prefs.getString(KEY_CITY, "Pune") ?: "Pune"
        val profile = prefs.getString(KEY_PROFILE_TAG, "Single Person") ?: "Single Person"
        val emi = prefs.getString(KEY_EMI_TAG, "No EMI") ?: "No EMI"
        val salary = prefs.getLong(KEY_SALARY, 0L)
        val currency = prefs.getString(KEY_CURRENCY, "INR") ?: "INR"
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
        val modeStr = prefs.getString(KEY_DEFAULT_MODE, DashboardMode.PLAN.name) ?: DashboardMode.PLAN.name
        val mode = runCatching { DashboardMode.valueOf(modeStr) }.getOrDefault(DashboardMode.PLAN)
        val scale = prefs.getFloat(KEY_TEXT_SCALE, 1.0f)

        return UserSettings(
            cityName = city,
            profileTag = profile,
            emiTag = emi,
            inHandSalaryRupees = salary,
            currencyCode = currency,
            themeMode = theme,
            defaultMode = mode,
            textScale = scale
        )
    }

    fun updateCity(city: String) {
        val trimmed = city.trim().ifEmpty { "Pune" }
        prefs.edit().putString(KEY_CITY, trimmed).apply()
        _settings.value = _settings.value.copy(cityName = trimmed)
    }

    fun updateProfile(profileTag: String, emiTag: String) {
        val p = profileTag.trim().ifEmpty { "Single Person" }
        val e = emiTag.trim().ifEmpty { "No EMI" }
        prefs.edit()
            .putString(KEY_PROFILE_TAG, p)
            .putString(KEY_EMI_TAG, e)
            .apply()
        _settings.value = _settings.value.copy(profileTag = p, emiTag = e)
    }

    fun updateSalary(salaryRupees: Long) {
        val s = salaryRupees.coerceAtLeast(0L)
        prefs.edit().putLong(KEY_SALARY, s).apply()
        _settings.value = _settings.value.copy(inHandSalaryRupees = s)
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateDefaultMode(mode: DashboardMode) {
        prefs.edit().putString(KEY_DEFAULT_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(defaultMode = mode)
    }

    fun updateTextScale(scale: Float) {
        val s = scale.coerceIn(0.75f, 1.50f)
        prefs.edit().putFloat(KEY_TEXT_SCALE, s).apply()
        _settings.value = _settings.value.copy(textScale = s)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = UserSettings()
    }

    companion object {
        private const val PREFS_NAME = "moneyboard_user_settings"
        private const val KEY_CITY = "city_name"
        private const val KEY_PROFILE_TAG = "profile_tag"
        private const val KEY_EMI_TAG = "emi_tag"
        private const val KEY_SALARY = "salary_rupees"
        private const val KEY_CURRENCY = "currency_code"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DEFAULT_MODE = "default_mode"
        private const val KEY_TEXT_SCALE = "text_scale"
    }
}

