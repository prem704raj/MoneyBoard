package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import java.util.Locale
import kotlin.math.roundToInt

data class FinancialHealthReport(
    val score: Int,
    val grade: String,
    val title: String,
    val advice: String,
    val scoreColor: Color,
    val savingsRatePct: Float,
    val livingRatioPct: Float,
    val cushionRupees: Long
)

fun calculateFinancialHealth(
    salaryRupees: Long,
    totalExpensesRupees: Long,
    totalSavingsRupees: Long,
    actualLivingRupees: Long
): FinancialHealthReport {
    val salary = salaryRupees.coerceAtLeast(1L).toFloat()
    val savings = totalSavingsRupees.coerceAtLeast(0L).toFloat()
    val living = actualLivingRupees.coerceAtLeast(0L).toFloat()
    val remaining = salaryRupees - totalExpensesRupees

    val savingsRatePct = (savings / salary * 100f).coerceIn(0f, 100f)
    val livingRatioPct = (living / salary * 100f).coerceIn(0f, 100f)

    // 1. Savings Score (Target: >= 20%) -> Max 35 points
    val savingsScore = (savingsRatePct / 20f * 35f).coerceIn(0f, 35f)

    // 2. Living Expense Score (50/30/20 Rule: Living costs should be <= 50%) -> Max 35 points
    val livingScore = when {
        livingRatioPct <= 50f -> 35f
        livingRatioPct <= 65f -> 35f - ((livingRatioPct - 50f) / 15f * 15f)
        livingRatioPct <= 85f -> 20f - ((livingRatioPct - 65f) / 20f * 15f)
        else -> 5f
    }

    // 3. Cashflow Buffer Score (Surplus cushion) -> Max 30 points
    val bufferScore = when {
        remaining >= (salaryRupees * 0.15f) -> 30f
        remaining > 0L -> 15f + ((remaining.toFloat() / (salaryRupees * 0.15f)) * 15f)
        remaining == 0L -> 10f
        else -> 0f
    }

    val totalScore = (savingsScore + livingScore + bufferScore).roundToInt().coerceIn(10, 100)

    val (grade, title, advice, color) = when {
        totalScore >= 85 -> Quadruple(
            "A+",
            "Elite Wealth Builder",
            "Outstanding discipline! Your ${String.format(Locale.US, "%.1f", savingsRatePct)}% savings rate exceeds the recommended 20% benchmark for long-term wealth building.",
            MoneyBoardColors.Green700
        )
        totalScore >= 70 -> Quadruple(
            "B+",
            "Solid Financial Health",
            "Well-managed budget! Your living costs (${String.format(Locale.US, "%.0f", livingRatioPct)}%) are disciplined and balanced.",
            MoneyBoardColors.Blue700
        )
        totalScore >= 50 -> Quadruple(
            "C",
            "Moderate Budget Strain",
            "Budget is tight with remaining cashflow under ₹${remaining}. Consider optimizing lifestyle overhead.",
            MoneyBoardColors.Orange700
        )
        else -> Quadruple(
            "D",
            "Cashflow Deficit Alert",
            "Expenses currently exceed or match your full in-hand salary. High risk if emergency occurs.",
            MoneyBoardColors.Red600
        )
    }

    return FinancialHealthReport(
        score = totalScore,
        grade = grade,
        title = title,
        advice = advice,
        scoreColor = color,
        savingsRatePct = savingsRatePct,
        livingRatioPct = livingRatioPct,
        cushionRupees = remaining
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun FinancialHealthCard(
    report: FinancialHealthReport,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = report.score / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "healthScore"
    )

    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MoneyBoardColors.BadgeSkyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Executive Financial Health Score",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Grade Pill (never wraps or splits characters)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(report.scoreColor.copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = report.scoreColor.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "Grade ${report.grade}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = report.scoreColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Score Banner + Dial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Ring Score
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(54.dp),
                        color = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine,
                        strokeWidth = 5.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(54.dp),
                        color = report.scoreColor,
                        strokeWidth = 5.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${report.score}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            ),
                            color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                        )
                    }
                }

                // Rating & Advice Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = report.scoreColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = report.advice,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        ),
                        color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                }
            }

            // 3 Pillar KPI Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiPill(
                    label = "Savings Ratio",
                    value = String.format(Locale.US, "%.1f%%", report.savingsRatePct),
                    target = "Rule: 20%+",
                    isGood = report.savingsRatePct >= 20f,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    label = "Living Needs",
                    value = String.format(Locale.US, "%.0f%%", report.livingRatioPct),
                    target = "Rule: < 50%",
                    isGood = report.livingRatioPct <= 50f,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    label = "Monthly Buffer",
                    value = if (report.cushionRupees >= 0) "+₹${report.cushionRupees}" else "-₹${-report.cushionRupees}",
                    target = "Rule: Positive",
                    isGood = report.cushionRupees > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiPill(
    label: String,
    value: String,
    target: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val pillBg = if (isDark) {
        if (isGood) Color(0xFF0F261E) else Color(0xFF2A1515)
    } else {
        if (isGood) MoneyBoardColors.SalarySurface else Color(0xFFFDF2F2)
    }
    val pillBorder = if (isDark) {
        if (isGood) Color(0xFF1B4D3E) else Color(0xFF5A2525)
    } else {
        if (isGood) MoneyBoardColors.SalaryBorder else Color(0xFFF8D7DA)
    }
    val valueColor = if (isGood) {
        if (isDark) Color(0xFF81C784) else MoneyBoardColors.Green700
    } else {
        if (isDark) Color(0xFFFF8A80) else MoneyBoardColors.Red600
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(pillBg)
            .border(
                width = 1.dp,
                color = pillBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(vertical = 6.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = valueColor
            )
            Text(
                text = target,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = if (isDark) MoneyBoardColors.DarkTextSecondary.copy(alpha = 0.8f) else MoneyBoardColors.TextSecondary.copy(alpha = 0.8f)
            )
        }
    }
}

