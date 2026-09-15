package com.premraj.moneyboard.core.designsystem

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = MoneyBoardColors.Navy900,
    onPrimary = Color.White,
    secondary = MoneyBoardColors.Blue600,
    onSecondary = Color.White,
    tertiary = MoneyBoardColors.Green700,
    background = MoneyBoardColors.BackgroundLight,
    onBackground = MoneyBoardColors.TextPrimary,
    surface = MoneyBoardColors.SurfaceLight,
    onSurface = MoneyBoardColors.TextPrimary,
    surfaceVariant = MoneyBoardColors.SalarySurface,
    onSurfaceVariant = MoneyBoardColors.TextSecondary,
    error = MoneyBoardColors.Red600
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9BCBFF),
    onPrimary = Color(0xFF002E51),
    secondary = Color(0xFFA8C9EA),
    onSecondary = Color(0xFF0C314D),
    tertiary = Color(0xFF82D6B7),
    background = MoneyBoardColors.DarkBackground,
    onBackground = MoneyBoardColors.DarkTextPrimary,
    surface = MoneyBoardColors.DarkSurface,
    onSurface = MoneyBoardColors.DarkTextPrimary,
    surfaceVariant = MoneyBoardColors.DarkSurfaceVariant,
    onSurfaceVariant = MoneyBoardColors.DarkTextSecondary,
    error = Color(0xFFFFB4AB)
)

@Composable
fun MoneyBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window

            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = !darkTheme
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MoneyBoardTypography,
        content = content
    )
}
