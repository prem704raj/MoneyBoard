package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.SummaryLineUi

@Composable
fun SummaryCardsGrid(
    salaryVsExpenses: List<SummaryLineUi>,
    savings: List<SummaryLineUi>,
    living: List<SummaryLineUi>,
    annual: List<SummaryLineUi>,
    remainingText: String = "₹650 / month",
    totalSavingsText: String = "₹14,000 / month",
    actualSpendingText: String = "₹55,350 / month",
    annualBalanceText: String = "₹7,800",
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 480.dp

        if (isWide) {
            // 2x2 Grid for wider screens
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SalaryCardView(
                        lines = salaryVsExpenses,
                        remainingText = remainingText,
                        modifier = Modifier.weight(1f)
                    )
                    SavingsCardView(
                        lines = savings,
                        totalSavingsText = totalSavingsText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LivingCardView(
                        lines = living,
                        actualSpendingText = actualSpendingText,
                        modifier = Modifier.weight(1f)
                    )
                    AnnualCardView(
                        lines = annual,
                        annualBalanceText = annualBalanceText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // 2x2 Grid on mobile too with compact spacing so it matches the infographic layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SalaryCardView(
                        lines = salaryVsExpenses,
                        remainingText = remainingText,
                        modifier = Modifier.weight(1f)
                    )
                    SavingsCardView(
                        lines = savings,
                        totalSavingsText = totalSavingsText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LivingCardView(
                        lines = living,
                        actualSpendingText = actualSpendingText,
                        modifier = Modifier.weight(1f)
                    )
                    AnnualCardView(
                        lines = annual,
                        annualBalanceText = annualBalanceText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SalaryCardView(
    lines: List<SummaryLineUi>,
    remainingText: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    InfographicSummaryCard(
        title = "Salary vs Expenses",
        icon = Icons.Rounded.Wallet,
        iconTint = Color.White,
        iconBg = MoneyBoardColors.Blue600,
        titleColor = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.SalaryTitleColor,
        surfaceColor = if (isDark) Color(0xFF102133) else MoneyBoardColors.SalarySurface,
        borderColor = if (isDark) Color(0xFF1B3D5E) else MoneyBoardColors.SalaryBorder,
        lines = lines,
        pillLabel = "Remaining",
        pillValue = remainingText,
        pillBg = if (isDark) Color(0xFF0F3826) else MoneyBoardColors.PillGreenBg,
        pillTextColor = if (isDark) Color(0xFF81C784) else MoneyBoardColors.PillGreenText,
        modifier = modifier
    )
}

@Composable
fun SavingsCardView(
    lines: List<SummaryLineUi>,
    totalSavingsText: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    InfographicSummaryCard(
        title = "Savings & Investments\n(Per Month)",
        icon = Icons.Rounded.Savings,
        iconTint = Color.White,
        iconBg = MoneyBoardColors.Green700,
        titleColor = if (isDark) Color(0xFF81C784) else MoneyBoardColors.SavingTitleColor,
        surfaceColor = if (isDark) Color(0xFF0F261E) else MoneyBoardColors.SavingSurface,
        borderColor = if (isDark) Color(0xFF1B4E3C) else MoneyBoardColors.SavingBorder,
        lines = lines,
        pillLabel = "Total Savings",
        pillValue = totalSavingsText,
        pillBg = if (isDark) Color(0xFF0F3826) else MoneyBoardColors.PillGreenBg,
        pillTextColor = if (isDark) Color(0xFF81C784) else MoneyBoardColors.PillGreenText,
        modifier = modifier
    )
}

@Composable
fun LivingCardView(
    lines: List<SummaryLineUi>,
    actualSpendingText: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    InfographicSummaryCard(
        title = "Actual Living / Family /\nTravel Spending",
        icon = Icons.Rounded.Home,
        iconTint = Color.White,
        iconBg = MoneyBoardColors.Purple700,
        titleColor = if (isDark) Color(0xFFCE93D8) else MoneyBoardColors.SpendingTitleColor,
        surfaceColor = if (isDark) Color(0xFF1F172E) else MoneyBoardColors.SpendingSurface,
        borderColor = if (isDark) Color(0xFF3F2B60) else MoneyBoardColors.SpendingBorder,
        lines = lines,
        pillLabel = "Actual Spending",
        pillValue = actualSpendingText,
        pillBg = if (isDark) Color(0xFF33204D) else MoneyBoardColors.PillPurpleBg,
        pillTextColor = if (isDark) Color(0xFFCE93D8) else MoneyBoardColors.PillPurpleText,
        modifier = modifier
    )
}

@Composable
fun AnnualCardView(
    lines: List<SummaryLineUi>,
    annualBalanceText: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    InfographicSummaryCard(
        title = "Annual Summary",
        icon = Icons.Rounded.CalendarMonth,
        iconTint = Color.White,
        iconBg = MoneyBoardColors.Orange700,
        titleColor = if (isDark) Color(0xFFFFB74D) else MoneyBoardColors.AnnualTitleColor,
        surfaceColor = if (isDark) Color(0xFF271A14) else MoneyBoardColors.AnnualSurface,
        borderColor = if (isDark) Color(0xFF553220) else MoneyBoardColors.AnnualBorder,
        lines = lines,
        lineValueColor = if (isDark) Color(0xFFFFB74D) else MoneyBoardColors.AnnualTitleColor,
        pillLabel = "Annual Balance",
        pillValue = annualBalanceText,
        pillBg = if (isDark) Color(0xFF422214) else MoneyBoardColors.PillOrangeBg,
        pillTextColor = if (isDark) Color(0xFFFFB74D) else MoneyBoardColors.PillOrangeText,
        modifier = modifier
    )
}

@Composable
private fun InfographicSummaryCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    titleColor: Color,
    surfaceColor: Color,
    borderColor: Color,
    lines: List<SummaryLineUi>,
    pillLabel: String,
    pillValue: String,
    pillBg: Color,
    pillTextColor: Color,
    lineValueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: Icon + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        ),
                        color = titleColor,
                        maxLines = 2
                    )
                }

                // Data Rows
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
                    lines.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = line.label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.5.sp
                                    ),
                                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                                )
                                if (line.subLabel != null) {
                                    Text(
                                        text = line.subLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp
                                        ),
                                        color = if (isDark) MoneyBoardColors.DarkTextSecondary.copy(alpha = 0.8f) else MoneyBoardColors.TextSecondary.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Text(
                                text = line.value,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = lineValueColor ?: if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Highlight Row (Label on Left, Pill only around Value on Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pillLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    ),
                    color = titleColor,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pillBg)
                        .padding(horizontal = 7.dp, vertical = 3.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pillValue,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        ),
                        color = pillTextColor,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
