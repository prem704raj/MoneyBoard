package com.premraj.moneyboard.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.components.CategoryDonutCard
import com.premraj.moneyboard.feature.dashboard.components.ExpenseLedger
import com.premraj.moneyboard.feature.dashboard.components.FinanceHeroHeader
import com.premraj.moneyboard.feature.dashboard.components.FinancialHealthCard
import com.premraj.moneyboard.feature.dashboard.components.SummaryCardsGrid
import com.premraj.moneyboard.feature.dashboard.components.TotalAllocationBanner
import com.premraj.moneyboard.feature.dashboard.components.VerificationSummaryCard
import com.premraj.moneyboard.feature.dashboard.components.calculateFinancialHealth
import java.time.YearMonth

@Composable
fun FinanceBoardScreen(
    uiState: DashboardUiState = DashboardUiState(selectedMonth = YearMonth.now()),
    userSettings: UserSettings = UserSettings(),
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onResetToCurrentMonth: () -> Unit = {},
    onModeSelected: (DashboardMode) -> Unit = {},
    onExportPdf: () -> Unit = {},
    onExportImage: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onExpenseClick: (ExpenseRowUi) -> Unit = {},
    onAddExpenseClick: () -> Unit = {},
    onLogTransaction: () -> Unit = {},
    onEditSalary: () -> Unit = {},
    onEditCity: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading && uiState.snapshot == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MoneyBoardColors.Blue700
                    )
                }
            }

            uiState.errorMessage != null && uiState.snapshot == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onResetToCurrentMonth
                        ) {
                            Text(text = "Return to Current Month")
                        }
                    }
                }
            }

            else -> {
                val board = uiState.snapshot?.toFinanceBoardUi(uiState.mode)
                    ?: FinanceBoardUiModel(
                        salaryText = "₹${userSettings.inHandSalaryRupees} / month",
                        totalAllocationText = "₹69,350",
                        expenseRows = DashboardPreviewData.expenses,
                        salaryVsExpenses = DashboardPreviewData.salaryVsExpenses,
                        savings = DashboardPreviewData.savings,
                        living = DashboardPreviewData.living,
                        annual = DashboardPreviewData.annual,
                        chartLines = emptyList(),
                        donutSlices = com.premraj.moneyboard.feature.dashboard.components.defaultDonutSlices(),
                        remainingText = "₹650 / month",
                        savingsRateText = "20.2%",
                        totalSavingsText = "₹14,000 / month",
                        actualSpendingText = "₹55,350 / month",
                        annualBalanceText = "₹7,800"
                    )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Skyline, In-hand salary card, Month navigation, and Action buttons
                    item {
                        FinanceHeroHeader(
                            board = board,
                            salaryText = board.salaryText.replace("/ month", "").trim() + " / month",
                            cityName = userSettings.cityName,
                            profileTag = userSettings.profileTag,
                            emiTag = userSettings.emiTag,
                            selectedMonth = uiState.selectedMonth,
                            mode = uiState.mode,
                            onPreviousMonth = onPreviousMonth,
                            onNextMonth = onNextMonth,
                            onModeToggle = onModeSelected,
                            onExportPdf = onExportPdf,
                            onExportImage = onExportImage,
                            onOpenSettings = onOpenSettings,
                            onLogTransaction = onLogTransaction,
                            onEditSalary = onEditSalary,
                            onEditCity = onEditCity
                        )
                    }

                    // Two-Column Expense Ledger (24 items matching infographic)
                    item {
                        SectionContainer {
                            ExpenseLedger(
                                expenses = board.expenseRows,
                                onExpenseClick = onExpenseClick,
                                onAddExpenseClick = onAddExpenseClick
                            )
                        }
                    }

                    // Total Monthly Expenses Banner
                    item {
                        SectionContainer {
                            TotalAllocationBanner(
                                amountText = board.totalAllocationText,
                                remainingText = board.remainingText,
                                salaryText = board.salaryText
                            )
                        }
                    }

                    // 4 Themed Summary Cards (2x2 Grid with colored pills)
                    item {
                        SectionContainer {
                            SummaryCardsGrid(
                                salaryVsExpenses = board.salaryVsExpenses,
                                savings = board.savings,
                                living = board.living,
                                annual = board.annual,
                                remainingText = board.remainingText,
                                totalSavingsText = board.totalSavingsText,
                                actualSpendingText = board.actualSpendingText,
                                annualBalanceText = board.annualBalanceText
                            )
                        }
                    }

                    // Executive Financial Health Score & Wellness Ratio
                    item {
                        SectionContainer {
                            val totalExpRupees = board.expenseRows.sumOf { it.amountMinor } / 100L
                            val totalSavRupees = board.savings.firstOrNull()?.value?.replace("₹", "")?.replace(",", "")?.trim()?.toLongOrNull() ?: 14_000L
                            val actLivingRupees = board.actualSpendingText.replace("₹", "").replace("/ month", "").replace(",", "").trim().toLongOrNull() ?: 55_350L

                            val healthReport = calculateFinancialHealth(
                                salaryRupees = userSettings.inHandSalaryRupees,
                                totalExpensesRupees = totalExpRupees,
                                totalSavingsRupees = totalSavRupees,
                                actualLivingRupees = actLivingRupees
                            )

                            FinancialHealthCard(report = healthReport)
                        }
                    }

                    // Bottom Analysis Row (Donut Chart & Verification Checklist)
                    item {
                        SectionContainer {
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isWide = maxWidth >= 580.dp

                                if (isWide) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CategoryDonutCard(
                                            totalMonthlyText = board.totalAllocationText,
                                            slices = board.donutSlices,
                                            modifier = Modifier.weight(1.05f)
                                        )

                                        VerificationSummaryCard(
                                            totalMonthlyText = board.totalAllocationText,
                                            salaryText = board.salaryText.replace("/ month", "").trim(),
                                            remainingText = board.remainingText.replace("/ month", "").trim(),
                                            annualExpensesText = board.annual.firstOrNull()?.value ?: "₹8,32,200",
                                            annualSalaryText = board.annual.getOrNull(1)?.value ?: "₹8,40,000",
                                            annualBalanceText = board.annualBalanceText,
                                            modifier = Modifier.weight(0.95f)
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        CategoryDonutCard(
                                            totalMonthlyText = board.totalAllocationText,
                                            slices = board.donutSlices
                                        )

                                        VerificationSummaryCard(
                                            totalMonthlyText = board.totalAllocationText,
                                            salaryText = board.salaryText.replace("/ month", "").trim(),
                                            remainingText = board.remainingText.replace("/ month", "").trim(),
                                            annualExpensesText = board.annual.firstOrNull()?.value ?: "₹8,32,200",
                                            annualSalaryText = board.annual.getOrNull(1)?.value ?: "₹8,40,000",
                                            annualBalanceText = board.annualBalanceText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        content()
    }
}
