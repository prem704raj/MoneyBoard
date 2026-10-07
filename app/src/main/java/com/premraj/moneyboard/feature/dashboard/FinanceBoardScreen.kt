package com.premraj.moneyboard.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import com.premraj.moneyboard.feature.dashboard.components.AnnualSummaryCard
import com.premraj.moneyboard.feature.dashboard.components.ExpenseLedger
import com.premraj.moneyboard.feature.dashboard.components.FinanceHeroHeader
import com.premraj.moneyboard.feature.dashboard.components.LivingSpendingCard
import com.premraj.moneyboard.feature.dashboard.components.SalaryVsExpensesCard
import com.premraj.moneyboard.feature.dashboard.components.SavingsCard
import com.premraj.moneyboard.feature.dashboard.components.TotalAllocationBanner
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Main dashboard screen connected to real Room data via [DashboardViewModel].
 */
@Composable
fun FinanceBoardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(uiState.errorMessage!!)
            uiState.snapshot != null -> {
                val uiModel = DashboardUiMapper.map(
                    snapshot = uiState.snapshot!!,
                    mode = uiState.mode
                )
                DashboardContent(
                    uiModel = uiModel,
                    selectedMonth = uiState.selectedMonth,
                    mode = uiState.mode,
                    hasPlan = uiState.snapshot!!.hasPlan,
                    hasActivity = uiState.snapshot!!.hasActualActivity,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onSelectMode = viewModel::selectMode
                )
            }
            else -> EmptyState()
        }
    }
}

@Composable
private fun DashboardContent(
    uiModel: FinanceBoardUiModel,
    selectedMonth: YearMonth,
    mode: DashboardMode,
    hasPlan: Boolean,
    hasActivity: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMode: (DashboardMode) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FinanceHeroHeader(
                incomeText = uiModel.salaryText,
                monthLabel = formatMonthLabel(selectedMonth)
            )
        }

        item {
            SectionContainer {
                MonthNavigator(
                    selectedMonth = selectedMonth,
                    mode = mode,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onSelectMode = onSelectMode
                )
            }
        }

        // Show appropriate content based on data availability
        val showContent = when (mode) {
            DashboardMode.PLAN -> hasPlan
            DashboardMode.ACTUAL -> hasActivity
        }

        if (!showContent) {
            item {
                SectionContainer {
                    NoDataForMode(mode)
                }
            }
        } else {
            if (uiModel.expenseRows.isNotEmpty()) {
                item {
                    SectionContainer {
                        ExpenseLedger(expenses = uiModel.expenseRows)
                    }
                }
            }

            item {
                SectionContainer {
                    TotalAllocationBanner(
                        amountText = uiModel.totalAllocationText
                    )
                }
            }

            item {
                SectionContainer {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SalaryVsExpensesCard(lines = uiModel.salaryVsExpenses)

                        if (uiModel.savings.isNotEmpty()) {
                            SavingsCard(lines = uiModel.savings)
                        }

                        LivingSpendingCard(lines = uiModel.living)
                        AnnualSummaryCard(lines = uiModel.annual)
                    }
                }
            }

            item {
                SectionContainer {
                    SummaryBlock(
                        incomeText = uiModel.salaryText,
                        allocationText = uiModel.totalAllocationText,
                        remainingText = uiModel.remainingText,
                        savingsRateText = uiModel.savingsRateText,
                        mode = mode
                    )
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

@Composable
private fun MonthNavigator(
    selectedMonth: YearMonth,
    mode: DashboardMode,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectMode: (DashboardMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Previous month"
                )
            }

            Text(
                text = formatMonthLabel(selectedMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Next month"
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = mode == DashboardMode.PLAN,
                onClick = { onSelectMode(DashboardMode.PLAN) },
                label = { Text("Plan") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MoneyBoardColors.Blue700,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            )

            FilterChip(
                selected = mode == DashboardMode.ACTUAL,
                onClick = { onSelectMode(DashboardMode.ACTUAL) },
                label = { Text("Actual") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MoneyBoardColors.Green700,
                    selectedLabelColor = Color.White
                )
            )
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
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MoneyBoardColors.Blue700
            )
            Text(
                text = "Loading your finances…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = MoneyBoardColors.Red600,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Welcome to MoneyBoard",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Set up your monthly plan or add your first transaction to see your dashboard come alive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NoDataForMode(mode: DashboardMode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (mode) {
                DashboardMode.PLAN -> "No plan set for this month"
                DashboardMode.ACTUAL -> "No transactions this month"
            },
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = when (mode) {
                DashboardMode.PLAN -> "Create a monthly plan to see your budget breakdown."
                DashboardMode.ACTUAL -> "Add transactions to see your actual spending."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SummaryBlock(
    incomeText: String,
    allocationText: String,
    remainingText: String,
    savingsRateText: String,
    mode: DashboardMode
) {
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
                text = if (mode == DashboardMode.PLAN) "Plan Summary"
                       else "Actual Summary",
                style = MaterialTheme.typography.titleMedium,
                color = MoneyBoardColors.Green700
            )

            val prefix = if (mode == DashboardMode.PLAN) "Planned" else "Actual"

            Text(
                text = "• $prefix income: $incomeText",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Total allocation: $allocationText",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Remaining: $remainingText",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )

            Text(
                text = "• Savings rate: $savingsRateText",
                style = MaterialTheme.typography.bodySmall,
                color = MoneyBoardColors.TextPrimary
            )
        }
    }
}

private fun formatMonthLabel(month: YearMonth): String {
    val monthName = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$monthName ${month.year}"
}
