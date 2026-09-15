package com.premraj.moneyboard

import androidx.compose.runtime.Composable
import com.premraj.moneyboard.core.designsystem.MoneyBoardTheme
import com.premraj.moneyboard.feature.dashboard.FinanceBoardScreen

@Composable
fun MoneyBoardApp() {
    MoneyBoardTheme {
        FinanceBoardScreen()
    }
}
