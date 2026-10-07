package com.premraj.moneyboard

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.premraj.moneyboard.core.designsystem.MoneyBoardTheme
import com.premraj.moneyboard.feature.dashboard.DashboardViewModel
import com.premraj.moneyboard.feature.dashboard.FinanceBoardScreen

@Composable
fun MoneyBoardApp(
    appContainer: com.premraj.moneyboard.di.AppContainer
) {
    MoneyBoardTheme {
        val dashboardViewModel: DashboardViewModel = viewModel(
            factory = DashboardViewModel.factory(
                appContainer.observeMonthlyDashboardUseCase
            )
        )

        FinanceBoardScreen(
            viewModel = dashboardViewModel
        )
    }
}
