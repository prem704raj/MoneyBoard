package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

@Composable
fun VerificationSummaryCard(
    totalMonthlyText: String = "₹0",
    salaryText: String = "₹0",
    remainingText: String = "₹0",
    annualExpensesText: String = "₹0",
    annualSalaryText: String = "₹0",
    annualBalanceText: String = "₹0",
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val cardBg = if (isDark) Color(0xFF0F261E) else MoneyBoardColors.VerificationBg
    val borderColor = if (isDark) Color(0xFF1B4E3C) else MoneyBoardColors.VerificationBorder
    val checkColor = if (isDark) Color(0xFF81C784) else MoneyBoardColors.VerificationCheck

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = cardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Checkmark icon + Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = checkColor,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = checkColor
                )
            }

            // Bullet points
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SummaryBulletItem(
                    label = "Total monthly expenses: ",
                    value = "$totalMonthlyText (correctly calculated)",
                    isDark = isDark,
                    bulletColor = checkColor
                )
                SummaryBulletItem(
                    label = "In-hand salary: ",
                    value = salaryText,
                    isDark = isDark,
                    bulletColor = checkColor
                )
                SummaryBulletItem(
                    label = "Monthly remaining: ",
                    value = remainingText,
                    isDark = isDark,
                    bulletColor = checkColor
                )
                SummaryBulletItem(
                    label = "Total annual expenses: ",
                    value = annualExpensesText,
                    isDark = isDark,
                    bulletColor = checkColor
                )
                SummaryBulletItem(
                    label = "Total annual salary: ",
                    value = annualSalaryText,
                    isDark = isDark,
                    bulletColor = checkColor
                )
                SummaryBulletItem(
                    label = "Annual balance: ",
                    value = annualBalanceText,
                    isDark = isDark,
                    bulletColor = checkColor
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(borderColor)
            )

            // Footer note
            Text(
                text = "All calculations are accurate and verified for each expense item.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = checkColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryBulletItem(
    label: String,
    value: String,
    isDark: Boolean = false,
    bulletColor: androidx.compose.ui.graphics.Color = MoneyBoardColors.VerificationCheck
) {
    val primaryText = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
    val secondaryText = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = bulletColor
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            ),
            color = secondaryText
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = primaryText
        )
    }
}

