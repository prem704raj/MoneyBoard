package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

data class DonutSliceUi(
    val title: String,
    val subCategories: String,
    val percentageText: String,
    val percentageValue: Float,
    val color: Color
)

@Composable
fun CategoryDonutCard(
    totalMonthlyText: String = "₹69,350",
    slices: List<DonutSliceUi> = defaultDonutSlices(),
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val cardBg = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight
    val borderColor = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            val isWide = maxWidth >= 460.dp

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DonutChartRing(
                        totalMonthlyText = totalMonthlyText,
                        slices = slices,
                        isDark = isDark,
                        modifier = Modifier.size(140.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    DonutLegendList(
                        slices = slices,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DonutChartRing(
                        totalMonthlyText = totalMonthlyText,
                        slices = slices,
                        isDark = isDark,
                        modifier = Modifier.size(116.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    DonutLegendList(
                        slices = slices,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        isCompact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChartRing(
    totalMonthlyText: String,
    slices: List<DonutSliceUi>,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = size.minDimension * 0.22f
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            var currentAngle = -90f
            val total = slices.sumOf { it.percentageValue.toDouble() }.toFloat().coerceAtLeast(1f)

            slices.forEach { slice ->
                val sweep = (slice.percentageValue / total) * 360f
                drawArc(
                    color = slice.color,
                    startAngle = currentAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                currentAngle += sweep
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Monthly\nExpenses",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 11.sp
                ),
                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = totalMonthlyText,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                ),
                color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
            )
        }
    }
}

@Composable
private fun DonutLegendList(
    slices: List<DonutSliceUi>,
    isDark: Boolean = false,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val primaryText = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
    val secondaryText = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 4.dp else 6.dp)
    ) {
        slices.forEach { slice ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 8.dp else 10.dp)
                            .clip(CircleShape)
                            .background(slice.color)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = slice.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = if (isCompact) 10.sp else 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (slice.subCategories.isNotEmpty()) {
                                Text(
                                    text = " (${slice.subCategories})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = if (isCompact) 8.5.sp else 9.5.sp
                                    ),
                                    color = secondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Text(
                    text = slice.percentageText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = if (isCompact) 10.sp else 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = primaryText
                )
            }
        }
    }
}

fun defaultDonutSlices(): List<DonutSliceUi> = listOf(
    DonutSliceUi(
        title = "Housing",
        subCategories = "Rent",
        percentageText = "14.5%",
        percentageValue = 14.5f,
        color = MoneyBoardColors.ChartHousing
    ),
    DonutSliceUi(
        title = "Food & Essentials",
        subCategories = "Groceries + Food + Gas",
        percentageText = "14.4%",
        percentageValue = 14.4f,
        color = MoneyBoardColors.ChartFood
    ),
    DonutSliceUi(
        title = "Lifestyle",
        subCategories = "Shopping + Outing + Saloon + Laundry",
        percentageText = "11.5%",
        percentageValue = 11.5f,
        color = MoneyBoardColors.ChartLifestyle
    ),
    DonutSliceUi(
        title = "Travel",
        subCategories = "Domestic Trip + Hometown Visit",
        percentageText = "10.1%",
        percentageValue = 10.1f,
        color = MoneyBoardColors.ChartTravel
    ),
    DonutSliceUi(
        title = "Savings & Investments",
        subCategories = "SIP + LIC + Emergency",
        percentageText = "20.2%",
        percentageValue = 20.2f,
        color = MoneyBoardColors.ChartSavings
    ),
    DonutSliceUi(
        title = "Others",
        subCategories = "Electricity, Wi-Fi, Mobile, Medicine, etc.",
        percentageText = "29.3%",
        percentageValue = 29.3f,
        color = MoneyBoardColors.ChartOthers
    )
)

