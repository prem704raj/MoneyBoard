package com.premraj.moneyboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.designsystem.MoneyBoardTheme
import com.premraj.moneyboard.di.AppContainer
import com.premraj.moneyboard.feature.accounts.AccountsScreen
import com.premraj.moneyboard.feature.accounts.AccountsViewModel
import com.premraj.moneyboard.feature.dashboard.DashboardViewModel
import com.premraj.moneyboard.feature.dashboard.FinanceBoardScreen
import com.premraj.moneyboard.feature.planning.PlanningScreen
import com.premraj.moneyboard.feature.planning.PlanningViewModel
import com.premraj.moneyboard.feature.transactions.TransactionListScreen
import com.premraj.moneyboard.feature.transactions.TransactionListViewModel

enum class AppTab(
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Rounded.Dashboard),
    TRANSACTIONS("Ledger", Icons.AutoMirrored.Rounded.ReceiptLong),
    PLANNING("Planning", Icons.Rounded.TrackChanges),
    ACCOUNTS("Accounts", Icons.Rounded.AccountBalance)
}

@Composable
fun MoneyBoardApp(
    appContainer: AppContainer
) {
    MoneyBoardTheme {
        var currentTab by rememberSaveable { mutableStateOf(AppTab.DASHBOARD) }

        val dashboardViewModel: DashboardViewModel = viewModel(
            factory = DashboardViewModel.factory(
                appContainer.observeMonthlyDashboardUseCase
            )
        )

        val transactionListViewModel: TransactionListViewModel = viewModel(
            factory = TransactionListViewModel.factory(
                transactionRepository = appContainer.transactionRepository,
                accountRepository = appContainer.accountRepository,
                categoryRepository = appContainer.categoryRepository
            )
        )

        val planningViewModel: PlanningViewModel = viewModel(
            factory = PlanningViewModel.factory(
                monthlyPlanRepository = appContainer.monthlyPlanRepository,
                categoryRepository = appContainer.categoryRepository
            )
        )

        val accountsViewModel: AccountsViewModel = viewModel(
            factory = AccountsViewModel.factory(
                accountRepository = appContainer.accountRepository,
                transactionRepository = appContainer.transactionRepository
            )
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = MoneyBoardColors.Navy900,
                                indicatorColor = MoneyBoardColors.Navy900,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> FinanceBoardScreen(viewModel = dashboardViewModel)
                    AppTab.TRANSACTIONS -> TransactionListScreen(viewModel = transactionListViewModel)
                    AppTab.PLANNING -> PlanningScreen(viewModel = planningViewModel)
                    AppTab.ACCOUNTS -> AccountsScreen(viewModel = accountsViewModel)
                }
            }
        }
    }
}
