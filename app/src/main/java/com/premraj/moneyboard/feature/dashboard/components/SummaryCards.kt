package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.SummaryLineUi

@Composable
fun SalaryVsExpensesCard(
    lines: List<SummaryLineUi>,
    modifier: Modifier = Modifier
) {
    SummaryCard(
        title = "Salary vs Expenses",
        icon = Icons.Rounded.Wallet,
        iconTint = MoneyBoardColors.Blue700,
        containerColor = MoneyBoardColors.SalarySurface,
        lines = lines,
        modifier = modifier
    )
}

@Composable
fun SavingsCard(
    lines: List<SummaryLineUi>,
    modifier: Modifier = Modifier
) {
    SummaryCard(
        title = "Savings & Investments",
        icon = Icons.Rounded.Savings,
        iconTint = MoneyBoardColors.Green700,
        containerColor = MoneyBoardColors.SavingSurface,
        lines = lines,
        modifier = modifier
    )
}

@Composable
fun LivingSpendingCard(
    lines: List<SummaryLineUi>,
    modifier: Modifier = Modifier
) {
    SummaryCard(
        title = "Actual Living / Family / Travel",
        icon = Icons.Rounded.Home,
        iconTint = MoneyBoardColors.Purple700,
        containerColor = MoneyBoardColors.SpendingSurface,
        lines = lines,
        modifier = modifier
    )
}

@Composable
fun AnnualSummaryCard(
    lines: List<SummaryLineUi>,
    modifier: Modifier = Modifier
) {
    SummaryCard(
        title = "12-Month Projection",
        icon = Icons.Rounded.CalendarMonth,
        iconTint = MoneyBoardColors.Orange700,
        containerColor = MoneyBoardColors.AnnualSurface,
        lines = lines,
        modifier = modifier
    )
}

@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    lines: List<SummaryLineUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = iconTint.copy(alpha = 0.16f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = iconTint
            )
        }

        lines.forEachIndexed { index, line ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = line.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MoneyBoardColors.TextSecondary
                )

                Text(
                    text = line.value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MoneyBoardColors.TextPrimary,
                    fontWeight = if (index == lines.lastIndex) {
                        FontWeight.Bold
                    } else {
                        FontWeight.SemiBold
                    }
                )
            }
        }
    }
}
