package com.premraj.moneyboard.feature.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.components.AnnualSummaryCard
import com.premraj.moneyboard.feature.dashboard.components.ExpenseLedger
import com.premraj.moneyboard.feature.dashboard.components.FinanceHeroHeader
import com.premraj.moneyboard.feature.dashboard.components.LivingSpendingCard
import com.premraj.moneyboard.feature.dashboard.components.SalaryVsExpensesCard
import com.premraj.moneyboard.feature.dashboard.components.SavingsCard
import com.premraj.moneyboard.feature.dashboard.components.TotalAllocationBanner

@Composable
fun FinanceBoardScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FinanceHeroHeader()
            }

            item {
                SectionContainer {
                    ExpenseLedger(
                        expenses = DashboardPreviewData.expenses
                    )
                }
            }

            item {
                SectionContainer {
                    TotalAllocationBanner()
                }
            }

            item {
                SectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SalaryVsExpensesCard(
                            lines = DashboardPreviewData.salaryVsExpenses
                        )

                        SavingsCard(
                            lines = DashboardPreviewData.savings
                        )

                        LivingSpendingCard(
                            lines = DashboardPreviewData.living
                        )

                        AnnualSummaryCard(
                            lines = DashboardPreviewData.annual
                        )
                    }
                }
            }

            item {
                SectionContainer {
                    AnalyticsPlaceholder()
                }
            }

            item {
                SectionContainer {
                    SummaryBlock()
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

@Composable
private fun SectionContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
    ) {
        content()
    }
}

@Composable
private fun AnalyticsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Spending Breakdown",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(
                        color = MoneyBoardColors.Blue500.copy(alpha = 0.20f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Monthly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "₹69,350",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                BreakdownLine("Housing", "14.5%")
                BreakdownLine("Food & Essentials", "14.4%")
                BreakdownLine("Lifestyle", "11.5%")
                BreakdownLine("Travel", "10.1%")
                BreakdownLine("Savings & Investments", "20.2%")
                BreakdownLine("Others", "29.3%")
            }
        }

        Text(
            text = "Part 01 uses a placeholder chart. A real interactive donut chart will be implemented after the finance data layer is connected.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BreakdownLine(
    label: String,
    percentage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = percentage,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MoneyBoardColors.SavingSurface,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MoneyBoardColors.Green700
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MoneyBoardColors.Green700
            )

            Text(
                text = "• In-hand salary: ₹70,000",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Total monthly allocation: ₹69,350",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Remaining: ₹650",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Savings and investments: ₹14,000",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )
        }
    }
}
