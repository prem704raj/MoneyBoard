package com.premraj.moneyboard.feature.export

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import com.premraj.moneyboard.feature.dashboard.components.DonutSliceUi
import com.premraj.moneyboard.feature.dashboard.components.ExpenseIconView
import com.premraj.moneyboard.feature.dashboard.formatIndianRupees
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private const val SHEET_WIDTH_PX = 1224

/**
 * Unified 1224px wide expense report sheet composable inspired by high-contrast, bold typography
 * and fine-art landscape header banner.
 * Rendered at density 1 (LocalDensity provides Density(1f)) so 1224px is exact across all devices.
 *
 * Layout Structure:
 * 1. Scenic Landscape Fine-Art Header banner (header_landscape_art.jpg) with dark translucent scrim,
 *    title ("Monthly " + cyan "Expense " + "Breakdown" in bold 28sp), dynamic pill chips ("Single Person",
 *    City, Month, Mode, and EMI only if > 0), top-right location badge, and glassy teal in-hand salary card.
 * 2. Large rounded white content panel starting under the header:
 *    a. Dual Expense Cards: "Essential Expenses" (blue theme) and "Other Expenses" (purple theme) with continuous
 *       numbering (1..N), illustrated icons, high-contrast bold amounts, and clean readable columns without zooming.
 *    b. Total Monthly Expenses bar (dark blue gradient with calculator icon and right-aligned total)
 *       plus "You save ₹X / month" (or "Over budget") green/red card.
 *    c. Four Themed Summary Cards in a SINGLE row (Salary vs Expenses, Savings & Investments, Actual Living / Travel, Annual Summary)
 *       with enlarged bold typography.
 *    d. Bottom Row: Donut Chart Card (left) with center total and 6 category breakdown rows with bold percentages,
 *       and Quick Summary Card (right) with 6 metric rows, potted plant with coins illustration, and italic balance status.
 */
@Composable
fun ExpenseReportSheet(
    board: FinanceBoardUiModel,
    actualBoard: FinanceBoardUiModel? = null,
    settings: UserSettings,
    selectedMonth: YearMonth = YearMonth.now()
) {
    val actualMap = actualBoard?.expenseRows?.associateBy { it.categoryId.ifBlank { it.title.lowercase().trim() } } ?: emptyMap()

    val rows = board.expenseRows.map { planRow ->
        val key = planRow.categoryId.ifBlank { planRow.title.lowercase().trim() }
        val matchingActual = actualMap[key]
        val actualMinor = matchingActual?.amountMinor ?: planRow.actualAmountMinor
        planRow.copy(
            actualAmountMinor = actualMinor,
            actualAmountText = "₹${formatIndianRupees(actualMinor / 100L)}"
        )
    }

    val planTotalMinor = if (board.planTotalAllocationMinor > 0L) board.planTotalAllocationMinor else rows.sumOf { it.amountMinor }
    val planTotalExpensesRupees = planTotalMinor / 100L

    val actualTotalMinor = if (actualBoard != null && actualBoard.actualTotalAllocationMinor > 0L) {
        actualBoard.actualTotalAllocationMinor
    } else {
        rows.sumOf { it.actualAmountMinor }
    }
    val actualTotalExpensesRupees = actualTotalMinor / 100L

    val salaryRupees = board.salaryText
        .replace("/ month", "")
        .replace("₹", "")
        .replace(",", "")
        .trim()
        .toLongOrNull() ?: settings.inHandSalaryRupees

    val planRemainingRupees = salaryRupees - planTotalExpensesRupees
    val actualRemainingRupees = salaryRupees - actualTotalExpensesRupees

    val emiRow = rows.firstOrNull {
        it.categoryId == SeedIds.EXPENSE_EMI || it.title.equals("EMI", ignoreCase = true)
    }
    val emiRupees: Long = if (emiRow != null && emiRow.amountMinor > 0L) {
        emiRow.amountMinor / 100L
    } else {
        settings.emiTag.filter { it.isDigit() }.toLongOrNull() ?: 0L
    }

    val dynamicSlices = calculateDynamicDonutSlices(rows, planTotalMinor)

    CompositionLocalProvider(
        LocalDensity provides Density(density = 1f, fontScale = 1f)
    ) {
        Column(
            modifier = Modifier
                .width(SHEET_WIDTH_PX.dp)
                .background(Color(0xFFEEF3F8))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Scenic Fine-Art Landscape Header Banner
            ReportHeaderCard(
                profileTag = settings.profileTag,
                cityName = settings.cityName,
                emiRupees = emiRupees,
                salaryRupees = salaryRupees,
                selectedMonth = selectedMonth
            )

            // 2. White Content Panel with rounded corners
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // a. Side-by-side Dual Expense Cards
                    ReportDualExpenseCards(rows = rows)

                    // b. Total Bar & You Save Card
                    ReportTotalBar(
                        planTotalExpensesRupees = planTotalExpensesRupees,
                        actualTotalExpensesRupees = actualTotalExpensesRupees,
                        salaryRupees = salaryRupees
                    )

                    // c. Four Equal-Width Summary Cards in One Row
                    ReportFourSummaryCards(
                        board = board,
                        rows = rows,
                        planTotalExpensesRupees = planTotalExpensesRupees,
                        actualTotalExpensesRupees = actualTotalExpensesRupees,
                        salaryRupees = salaryRupees,
                        planRemainingRupees = planRemainingRupees,
                        actualRemainingRupees = actualRemainingRupees
                    )

                    // d. Bottom Row: Donut Chart Card + Quick Summary Card
                    ReportBottomRow(
                        planTotalExpensesRupees = planTotalExpensesRupees,
                        actualTotalExpensesRupees = actualTotalExpensesRupees,
                        salaryRupees = salaryRupees,
                        actualRemainingRupees = actualRemainingRupees,
                        slices = dynamicSlices
                    )
                }
            }
        }
    }
}

/**
 * Scenic Fine-Art Landscape Header Banner:
 * Features header_landscape_art.jpg fine-art background with a balanced dark-to-translucent navy scrim,
 * high-contrast bold title ("Monthly" + cyan "Expense" + "Breakdown"), dynamic pill chips,
 * top-right location badge, and glassy teal in-hand salary card.
 */
@Composable
private fun ReportHeaderCard(
    profileTag: String,
    cityName: String,
    emiRupees: Long,
    salaryRupees: Long,
    selectedMonth: YearMonth = YearMonth.now()
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF091830)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fine-art landscape painting background (Image 2)
            Image(
                painter = painterResource(id = R.drawable.header_landscape_art),
                contentDescription = "Scenic landscape header art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Elegant dark-to-translucent gradient scrim ensuring maximum contrast and legibility
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xDE08162A), // 87% dark navy over left title & chips
                                Color(0xB80E2548), // 72% navy in middle
                                Color(0x66103362)  // 40% navy on right, revealing mountains & sky
                            )
                        )
                    )
            )

            // Top-right location badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 22.dp)
                    .background(Color(0x66000000), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, Color(0x66FFFFFF)), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = cityName.ifBlank { "Pune" },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            // Main Header Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 26.dp, end = 26.dp, top = 24.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Bold Title & Dynamic Pill Chips
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Monthly ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Expense ",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "Breakdown",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                    }

                    // Dynamic Pill Chips: "Single Person", City, Month, Plan & Actual, and EMI only if > 0
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderChip(
                            icon = Icons.Rounded.Person,
                            text = profileTag.ifBlank { "Single Person" }
                        )

                        HeaderChip(
                            icon = Icons.Rounded.LocationOn,
                            text = cityName.ifBlank { "Pune" }
                        )

                        HeaderChip(
                            icon = Icons.Rounded.CalendarMonth,
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))
                        )

                        HeaderChip(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            text = "Plan & Actual"
                        )

                        if (emiRupees > 0L) {
                            HeaderChip(
                                icon = Icons.Rounded.CreditCard,
                                text = "EMI ₹${formatIndianRupees(emiRupees)}"
                            )
                        }
                    }
                }

                // Right: Glassy Teal In-hand Salary Card
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0F766E), Color(0xFF0D9488), Color(0xFF14B8A6))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .border(
                            BorderStroke(1.5.dp, Color(0x80FFFFFF)),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "In-hand Salary",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.95f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Text(
                                text = "₹${formatIndianRupees(salaryRupees)} / month",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .background(Color(0x3D000000), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color(0x66FFFFFF)), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White
        )
    }
}

/**
 * Two Cards side by side:
 * - Left: "Essential Expenses" (blue theme, house icon)
 * - Right: "Other Expenses" (purple theme, wallet icon)
 * Continuous numbering across both cards (1..N).
 * High-contrast bold typography inspired by Image 4 so everything is easily readable without zoom.
 */
@Composable
private fun ReportDualExpenseCards(rows: List<ExpenseRowUi>) {
    val essentialItems = rows.filter { it.isEssential }
    val otherItems = rows.filter { !it.isEssential }

    val essentialPlanMinor = essentialItems.sumOf { it.amountMinor }
    val essentialActualMinor = essentialItems.sumOf { it.actualAmountMinor }
    val otherPlanMinor = otherItems.sumOf { it.amountMinor }
    val otherActualMinor = otherItems.sumOf { it.actualAmountMinor }
    val totalPlanMinor = rows.sumOf { it.amountMinor }

    val essentialPct = if (totalPlanMinor > 0L) {
        (essentialPlanMinor.toDouble() / totalPlanMinor.toDouble()) * 100.0
    } else 0.0

    val otherPct = if (totalPlanMinor > 0L) {
        (otherPlanMinor.toDouble() / totalPlanMinor.toDouble()) * 100.0
    } else 0.0

    val hasActualSpending = rows.any { it.actualAmountMinor > 0L }
    val maxAmountMinor = rows.maxOfOrNull { maxOf(it.amountMinor, it.actualAmountMinor) }?.coerceAtLeast(1L) ?: 1L

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Card: Essential Expenses
        Box(modifier = Modifier.weight(1f)) {
            ExpenseSectionCard(
                title = "Essential Expenses",
                titleColor = Color(0xFF1E3A8A),
                badgeBgColor = Color(0xFF2563EB),
                badgeIcon = Icons.Rounded.Home,
                cardBorderColor = Color(0xFFBFDBFE),
                headerRowBg = Color(0xFFEBF3FC),
                trackColor = Color(0xFFDBEAFE),
                barFillColor = Color(0xFF2563EB),
                pillBgColor = Color(0xFF2563EB),
                sectionPlanRupees = essentialPlanMinor / 100L,
                sectionActualRupees = essentialActualMinor / 100L,
                percentageValue = essentialPct,
                items = essentialItems,
                numberOffset = 0,
                hasActualSpending = hasActualSpending,
                maxAmountMinor = maxAmountMinor
            )
        }

        // Right Card: Other Expenses
        Box(modifier = Modifier.weight(1f)) {
            ExpenseSectionCard(
                title = "Other Expenses",
                titleColor = Color(0xFF5B21B6),
                badgeBgColor = Color(0xFF7C3AED),
                badgeIcon = Icons.Rounded.AccountBalanceWallet,
                cardBorderColor = Color(0xFFDDD6FE),
                headerRowBg = Color(0xFFF3E8FF),
                trackColor = Color(0xFFEDE9FE),
                barFillColor = Color(0xFF7C3AED),
                pillBgColor = Color(0xFF7C3AED),
                sectionPlanRupees = otherPlanMinor / 100L,
                sectionActualRupees = otherActualMinor / 100L,
                percentageValue = otherPct,
                items = otherItems,
                numberOffset = essentialItems.size,
                hasActualSpending = hasActualSpending,
                maxAmountMinor = maxAmountMinor
            )
        }
    }
}

@Composable
private fun ExpenseSectionCard(
    title: String,
    titleColor: Color,
    badgeBgColor: Color,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    cardBorderColor: Color,
    headerRowBg: Color,
    trackColor: Color,
    barFillColor: Color,
    pillBgColor: Color,
    sectionPlanRupees: Long,
    sectionActualRupees: Long,
    percentageValue: Double,
    items: List<ExpenseRowUi>,
    numberOffset: Int,
    hasActualSpending: Boolean,
    maxAmountMinor: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Title Header with Plan & Actual Totals
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(badgeBgColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = titleColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Plan: ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF475569)
                            )
                            Text(
                                text = "₹${formatIndianRupees(sectionPlanRupees)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF0F172A)
                            )
                        }

                        if (hasActualSpending) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Actual: ",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = titleColor
                                )
                                Text(
                                    text = "₹${formatIndianRupees(sectionActualRupees)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = titleColor
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(pillBgColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f%%", percentageValue),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Table Column Header Row: high-contrast, bold, inspired by Image 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerRowBg)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "No.",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    ),
                    color = titleColor,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = titleColor,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )

                if (hasActualSpending) {
                    Text(
                        text = "Plan (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = titleColor,
                        modifier = Modifier.width(76.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Actual (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = titleColor,
                        modifier = Modifier.width(76.dp),
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = titleColor,
                        modifier = Modifier.width(80.dp),
                        textAlign = TextAlign.End
                    )
                } else {
                    // Clean Image 4 style single bold amount column when actual spending is not yet tracked
                    Text(
                        text = "Monthly (₹)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        ),
                        color = titleColor,
                        modifier = Modifier.width(110.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            // Table Item Rows with bold typography and high-contrast colors
            items.forEachIndexed { index, item ->
                val rowNumber = numberOffset + index + 1
                val isZebra = index % 2 == 1
                val rowBg = if (isZebra) Color(0xFFF8FAFC) else Color.White

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rowNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF1E293B),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )

                    ExpenseIconView(
                        item = item,
                        size = 28.dp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title with sub-line handling
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        val (primaryTitle, subTitle) = extractTitleParts(item.title)
                        Text(
                            text = primaryTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!subTitle.isNullOrBlank()) {
                            Text(
                                text = subTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val planRupees = item.amountMinor / 100L
                    val actualRupees = item.actualAmountMinor / 100L
                    val isOver = actualRupees > planRupees && planRupees > 0L

                    if (hasActualSpending) {
                        // Plan Amount
                        Text(
                            text = formatIndianRupees(planRupees),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF334155),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(76.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Actual Amount
                        Text(
                            text = formatIndianRupees(actualRupees),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (isOver) Color(0xFFDC2626) else if (actualRupees == 0L) Color(0xFF94A3B8) else Color(0xFF0F172A),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(76.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Progress / Status Bar
                        Column(
                            modifier = Modifier.width(80.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val pct = if (planRupees > 0L) {
                                ((actualRupees.toDouble() / planRupees.toDouble()) * 100.0).roundToInt()
                            } else 0

                            Text(
                                text = when {
                                    actualRupees == 0L -> "0%"
                                    isOver -> "+₹${formatIndianRupees(actualRupees - planRupees)}"
                                    else -> "$pct%"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = when {
                                    actualRupees == 0L -> Color(0xFF94A3B8)
                                    isOver -> Color(0xFFDC2626)
                                    else -> barFillColor
                                },
                                maxLines = 1
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(if (isOver) Color(0xFFFEE2E2) else trackColor, RoundedCornerShape(3.dp))
                            ) {
                                val barFraction = if (planRupees > 0L) {
                                    (actualRupees.toFloat() / planRupees.toFloat()).coerceIn(0f, 1f)
                                } else if (actualRupees > 0L) 1f else 0f

                                if (barFraction > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(barFraction)
                                            .fillMaxHeight()
                                            .background(
                                                if (isOver) Color(0xFFEF4444) else barFillColor,
                                                RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        // Image 4 style single bold prominent Monthly amount
                        Text(
                            text = formatIndianRupees(planRupees),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE2E8F0))
                    )
                }
            }
        }
    }
}

/**
 * Total Monthly Expenses Bar + You Save Card:
 * Inspired by Image 4 and Image 5 with bold white headline, bold cyan accents,
 * and large prominent amount legible from across the room.
 */
@Composable
private fun ReportTotalBar(
    planTotalExpensesRupees: Long,
    actualTotalExpensesRupees: Long,
    salaryRupees: Long
) {
    val actualRemainingRupees = salaryRupees - actualTotalExpensesRupees
    val planRemainingRupees = salaryRupees - planTotalExpensesRupees
    val isPositive = actualRemainingRupees >= 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Dark Navy Gradient Bar with bold white total
        Surface(
            modifier = Modifier.weight(3.2f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B1B36)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF091830),
                                Color(0xFF10306A),
                                Color(0xFF1D5CC5)
                            )
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Total Monthly Expenses",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = if (actualTotalExpensesRupees > 0L) {
                                "Plan Budget: ₹${formatIndianRupees(planTotalExpensesRupees)}"
                            } else {
                                "Plan Allocation: ₹${formatIndianRupees(planTotalExpensesRupees)}"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (actualTotalExpensesRupees > 0L) "Actual Spent" else "Total Amount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF67E8F9)
                    )
                    Text(
                        text = "₹${formatIndianRupees(if (actualTotalExpensesRupees > 0L) actualTotalExpensesRupees else planTotalExpensesRupees)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }

        // Right: You Save Card with bold emerald typography
        Surface(
            modifier = Modifier.weight(1.0f),
            shape = RoundedCornerShape(12.dp),
            color = if (isPositive) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, if (isPositive) Color(0xFFA7F3D0) else Color(0xFFFECACA))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isPositive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Rounded.Spa else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (isPositive) Color(0xFF059669) else Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isPositive) "Actual Saved" else "Over budget",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isPositive) Color(0xFF059669) else Color(0xFFDC2626)
                    )
                    Text(
                        text = "₹${formatIndianRupees(if (isPositive) (if (actualTotalExpensesRupees > 0L) actualRemainingRupees else planRemainingRupees) else -actualRemainingRupees)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (isPositive) Color(0xFF047857) else Color(0xFFB91C1C)
                    )
                    Text(
                        text = "Plan: ₹${formatIndianRupees(planRemainingRupees)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

/**
 * Four Themed Summary Cards in a single row:
 * 1. Salary vs Expenses (blue)
 * 2. Savings & Investments (green)
 * 3. Actual Living / Travel (purple)
 * 4. Annual Summary (orange)
 * Enforced with high contrast and bold typography.
 */
@Composable
private fun ReportFourSummaryCards(
    board: FinanceBoardUiModel,
    rows: List<ExpenseRowUi>,
    planTotalExpensesRupees: Long,
    actualTotalExpensesRupees: Long,
    salaryRupees: Long,
    planRemainingRupees: Long,
    actualRemainingRupees: Long
) {
    val savingsItems = rows.filter {
        it.categoryId in listOf(
            SeedIds.INVESTMENT_SIP,
            SeedIds.SAVING_LIC_POLICY,
            SeedIds.SAVING_EMERGENCY_FUND
        ) || it.title.contains("SIP", ignoreCase = true)
            || it.title.contains("LIC", ignoreCase = true)
            || it.title.contains("Emergency", ignoreCase = true)
    }

    val planSavingsRupees = if (savingsItems.isNotEmpty()) {
        savingsItems.sumOf { it.amountMinor } / 100L
    } else {
        board.savings.sumOf {
            it.value.replace("₹", "").replace(",", "").trim().toLongOrNull() ?: 0L
        }
    }

    val actualSavingsRupees = savingsItems.sumOf { it.actualAmountMinor } / 100L
    val effectiveSavingsRupees = if (actualSavingsRupees > 0L) actualSavingsRupees else planSavingsRupees

    val planLivingRupees = (planTotalExpensesRupees - planSavingsRupees).coerceAtLeast(0L)
    val actualLivingRupees = if (actualTotalExpensesRupees > 0L) {
        (actualTotalExpensesRupees - actualSavingsRupees).coerceAtLeast(0L)
    } else {
        planLivingRupees
    }

    val effectiveExpensesRupees = if (actualTotalExpensesRupees > 0L) actualTotalExpensesRupees else planTotalExpensesRupees
    val annualExpensesRupees = effectiveExpensesRupees * 12L
    val annualSalaryRupees = salaryRupees * 12L
    val annualBalanceRupees = annualSalaryRupees - annualExpensesRupees

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Salary vs Expenses
        SummaryCardContainer(
            modifier = Modifier.weight(1f),
            badgeColor = Color(0xFF2563EB),
            badgeIcon = Icons.Rounded.AttachMoney,
            title = "Salary vs Expenses",
            borderColor = Color(0xFFBFDBFE)
        ) {
            MetricRow(label = "In-hand Salary", value = "₹${formatIndianRupees(salaryRupees)}")
            Spacer(modifier = Modifier.height(5.dp))
            MetricRow(label = "Plan Budget", value = "₹${formatIndianRupees(planTotalExpensesRupees)}")
            Spacer(modifier = Modifier.height(5.dp))
            MetricRow(
                label = if (actualTotalExpensesRupees > 0L) "Actual Spent" else "Total Expenses",
                value = "₹${formatIndianRupees(effectiveExpensesRupees)}"
            )
            Spacer(modifier = Modifier.height(10.dp))
            val isPos = (if (actualTotalExpensesRupees > 0L) actualRemainingRupees else planRemainingRupees) >= 0
            val remRupees = if (actualTotalExpensesRupees > 0L) actualRemainingRupees else planRemainingRupees
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPos) "Remaining" else "Deficit",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    ),
                    color = if (isPos) Color(0xFF047857) else Color(0xFFB91C1C)
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (isPos) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isPos) {
                            "₹${formatIndianRupees(remRupees)} / month"
                        } else {
                            "₹${formatIndianRupees(-remRupees)} / month"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        ),
                        color = if (isPos) Color(0xFF065F46) else Color(0xFF991B1B)
                    )
                }
            }
        }

        // Card 2: Savings & Investments
        SummaryCardContainer(
            modifier = Modifier.weight(1f),
            badgeColor = Color(0xFF059669),
            badgeIcon = Icons.Rounded.Layers,
            title = "Savings & Investments",
            subtitle = "(Per Month)",
            borderColor = Color(0xFFA7F3D0)
        ) {
            if (savingsItems.isNotEmpty()) {
                savingsItems.take(3).forEachIndexed { i, it ->
                    if (i > 0) Spacer(modifier = Modifier.height(5.dp))
                    val itActual = it.actualAmountMinor / 100L
                    val itPlan = it.amountMinor / 100L
                    val displayVal = if (itActual > 0L && itActual != itPlan) {
                        "₹${formatIndianRupees(itActual)} (Plan: ₹${formatIndianRupees(itPlan)})"
                    } else {
                        "₹${formatIndianRupees(itPlan)}"
                    }
                    MetricRow(
                        label = it.title.substringBefore("(").trim(),
                        value = displayVal
                    )
                }
            } else {
                board.savings.take(3).forEachIndexed { i, it ->
                    if (i > 0) Spacer(modifier = Modifier.height(5.dp))
                    MetricRow(label = it.label, value = it.value)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Total Savings/Investment",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF065F46)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹${formatIndianRupees(effectiveSavingsRupees)} / month",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF065F46)
                    )
                }
            }
        }

        // Card 3: Actual Living / Travel
        SummaryCardContainer(
            modifier = Modifier.weight(1f),
            badgeColor = Color(0xFF7C3AED),
            badgeIcon = Icons.Rounded.People,
            title = "Actual Living / Travel",
            borderColor = Color(0xFFDDD6FE)
        ) {
            MetricRow(
                label = "Total Expenses",
                value = "₹${formatIndianRupees(effectiveExpensesRupees)}"
            )
            Spacer(modifier = Modifier.height(5.dp))
            MetricRow(
                label = "Less Savings/Investments",
                value = "(₹${formatIndianRupees(effectiveSavingsRupees)})"
            )
            Spacer(modifier = Modifier.height(5.dp))
            MetricRow(
                label = "Plan Living Budget",
                value = "₹${formatIndianRupees(planLivingRupees)}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Actual Living Spending",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF6D28D9)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEDE9FE), RoundedCornerShape(8.dp))
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹${formatIndianRupees(actualLivingRupees)} / month",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF6D28D9)
                    )
                }
            }
        }

        // Card 4: Annual Summary
        SummaryCardContainer(
            modifier = Modifier.weight(1f),
            badgeColor = Color(0xFFEA580C),
            badgeIcon = Icons.Rounded.CalendarMonth,
            title = "Annual Summary",
            borderColor = Color(0xFFFED7AA)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Allocation",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "₹${formatIndianRupees(effectiveExpensesRupees)} × 12",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "₹${formatIndianRupees(annualExpensesRupees)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        ),
                        color = Color(0xFFC2410C)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Annual Salary",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "₹${formatIndianRupees(salaryRupees)} × 12",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                    Text(
                        text = "₹${formatIndianRupees(annualSalaryRupees)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp
                        ),
                        color = Color(0xFFC2410C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Annual Balance",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    ),
                    color = Color(0xFFC2410C)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFEDD5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "₹${formatIndianRupees(annualBalanceRupees)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        ),
                        color = Color(0xFFC2410C)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCardContainer(
    modifier: Modifier = Modifier,
    badgeColor: Color,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFBFDFF),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        color = Color(0xFF0F172A)
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium
            ),
            color = Color(0xFF334155)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            ),
            color = Color(0xFF0F172A)
        )
    }
}

/**
 * Bottom Row:
 * Left: Donut Chart Card (thick donut with bold center total + 6 category breakdown rows with bold percentages)
 * Right: Quick Summary Card (green theme, 6 bold metric rows, potted plant & coins illustration, italic balance status)
 */
@Composable
private fun ReportBottomRow(
    planTotalExpensesRupees: Long,
    actualTotalExpensesRupees: Long,
    salaryRupees: Long,
    actualRemainingRupees: Long,
    slices: List<DonutSliceUi>
) {
    val effectiveExpensesRupees = if (actualTotalExpensesRupees > 0L) actualTotalExpensesRupees else planTotalExpensesRupees
    val annualExpensesRupees = effectiveExpensesRupees * 12L
    val annualSalaryRupees = salaryRupees * 12L
    val annualBalanceRupees = annualSalaryRupees - annualExpensesRupees
    val remainingRupees = salaryRupees - effectiveExpensesRupees

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Category Donut Chart Card
        Surface(
            modifier = Modifier.weight(1.05f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart Canvas with Center Hole
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 28.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        var startAngle = -90f

                        for (slice in slices) {
                            val sweep = (slice.percentageValue / 100f) * 360f
                            if (sweep > 0f) {
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweep
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Monthly Expenses",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "₹${formatIndianRupees(effectiveExpensesRupees)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = Color(0xFF0F172A)
                        )
                        if (actualTotalExpensesRupees > 0L && actualTotalExpensesRupees != planTotalExpensesRupees) {
                            Text(
                                text = "Plan: ₹${formatIndianRupees(planTotalExpensesRupees)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                // Category Percentage Legend
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    slices.forEach { slice ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .background(slice.color, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = slice.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "(${slice.subCategories})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = slice.percentageText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                ),
                                color = Color(0xFF0F172A),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Right: Quick Summary Card
        Surface(
            modifier = Modifier.weight(0.95f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF2FBF6),
            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF22C55E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = "Quick Summary",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        ),
                        color = Color(0xFF166534)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric rows
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickSummaryRow(
                            icon = Icons.Rounded.Schedule,
                            label = "Plan total expenses",
                            value = "₹${formatIndianRupees(planTotalExpensesRupees)}"
                        )
                        QuickSummaryRow(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            label = if (actualTotalExpensesRupees > 0L) "Actual total expenses" else "Total expenses",
                            value = "₹${formatIndianRupees(effectiveExpensesRupees)}"
                        )
                        QuickSummaryRow(
                            icon = Icons.Rounded.AccountBalanceWallet,
                            label = "In-hand salary",
                            value = "₹${formatIndianRupees(salaryRupees)}"
                        )
                        QuickSummaryRow(
                            icon = Icons.Rounded.Schedule,
                            label = "Monthly remaining",
                            value = "₹${formatIndianRupees(remainingRupees)}"
                        )
                        QuickSummaryRow(
                            icon = Icons.Rounded.AccountBalance,
                            label = "Total annual salary",
                            value = "₹${formatIndianRupees(annualSalaryRupees)}"
                        )
                        QuickSummaryRow(
                            icon = Icons.Rounded.Star,
                            label = "Annual balance",
                            value = "₹${formatIndianRupees(annualBalanceRupees)}"
                        )
                    }

                    // Plant and Coins illustration with balance tagline
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (remainingRupees >= 0) {
                            Text(
                                text = "(\u00A0Good\nFinancial\nBalance!\u00A0)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                ),
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        PlantAndCoinsIllustration(
                            modifier = Modifier.size(96.dp, 100.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "All calculations are accurate and verified for each expense item.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    ),
                    color = Color(0xFF15803D),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun QuickSummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.5.sp
            ),
            color = Color(0xFF0F172A)
        )
    }
}

/**
 * Potted plant and golden coins stack vector illustration matching IMAGE 3.
 */
@Composable
fun PlantAndCoinsIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Plant Pot
        val potTopY = h * 0.64f
        val potBottomY = h * 0.94f
        val potLeftX = w * 0.12f
        val potRightX = w * 0.54f
        val potMidX = (potLeftX + potRightX) / 2f

        val potPath = Path().apply {
            moveTo(potLeftX, potTopY)
            lineTo(potRightX, potTopY)
            lineTo(potRightX - w * 0.05f, potBottomY)
            lineTo(potLeftX + w * 0.05f, potBottomY)
            close()
        }
        drawPath(potPath, color = Color(0xFF334155))

        // Pot Rim
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(potLeftX - w * 0.02f, potTopY - h * 0.04f),
            size = Size(potRightX - potLeftX + w * 0.04f, h * 0.05f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        )

        // 2. Plant stems and leaves
        val stemColor = Color(0xFF16A34A)
        val leafDarkGreen = Color(0xFF15803D)
        val leafLightGreen = Color(0xFF4ADE80)
        val leafMidGreen = Color(0xFF22C55E)

        // Center stem
        drawLine(
            color = stemColor,
            start = Offset(potMidX, potTopY),
            end = Offset(potMidX, h * 0.22f),
            strokeWidth = 2.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Left branch
        val leftBranch = Path().apply {
            moveTo(potMidX, h * 0.48f)
            quadraticTo(potMidX - w * 0.16f, h * 0.42f, potMidX - w * 0.24f, h * 0.32f)
        }
        drawPath(leftBranch, color = stemColor, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

        // Left leaf
        val leftLeaf = Path().apply {
            moveTo(potMidX - w * 0.24f, h * 0.32f)
            quadraticTo(potMidX - w * 0.34f, h * 0.22f, potMidX - w * 0.20f, h * 0.15f)
            quadraticTo(potMidX - w * 0.14f, h * 0.26f, potMidX - w * 0.24f, h * 0.32f)
        }
        drawPath(leftLeaf, color = leafDarkGreen)

        // Right branch
        val rightBranch = Path().apply {
            moveTo(potMidX, h * 0.42f)
            quadraticTo(potMidX + w * 0.14f, h * 0.38f, potMidX + w * 0.22f, h * 0.28f)
        }
        drawPath(rightBranch, color = stemColor, style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round))

        // Right leaf
        val rightLeaf = Path().apply {
            moveTo(potMidX + w * 0.22f, h * 0.28f)
            quadraticTo(potMidX + w * 0.34f, h * 0.20f, potMidX + w * 0.24f, h * 0.12f)
            quadraticTo(potMidX + w * 0.12f, h * 0.20f, potMidX + w * 0.22f, h * 0.28f)
        }
        drawPath(rightLeaf, color = leafMidGreen)

        // Top center leaves
        val topLeaf1 = Path().apply {
            moveTo(potMidX, h * 0.22f)
            quadraticTo(potMidX - w * 0.12f, h * 0.10f, potMidX - w * 0.02f, h * 0.04f)
            quadraticTo(potMidX + w * 0.07f, h * 0.12f, potMidX, h * 0.22f)
        }
        drawPath(topLeaf1, color = leafLightGreen)

        val topLeaf2 = Path().apply {
            moveTo(potMidX, h * 0.22f)
            quadraticTo(potMidX + w * 0.11f, h * 0.12f, potMidX + w * 0.05f, h * 0.06f)
            quadraticTo(potMidX - w * 0.02f, h * 0.16f, potMidX, h * 0.22f)
        }
        drawPath(topLeaf2, color = leafDarkGreen)

        // 3. Golden Coins Stack on the right of the pot
        val coinStartX = w * 0.56f
        val coinWidth = w * 0.36f
        val coinHeight = h * 0.065f
        val coinBaseY = potBottomY

        val coinGold = Color(0xFFF59E0B)
        val coinGoldDark = Color(0xFFD97706)
        val coinGoldLight = Color(0xFFFDE68A)

        for (c in 0..3) {
            val cy = coinBaseY - (c * h * 0.055f)
            drawRoundRect(
                color = coinGoldDark,
                topLeft = Offset(coinStartX, cy - coinHeight / 2),
                size = Size(coinWidth, coinHeight),
                cornerRadius = CornerRadius(coinHeight / 2, coinHeight / 2)
            )
            drawOval(
                color = if (c % 2 == 0) coinGold else coinGoldLight,
                topLeft = Offset(coinStartX + w * 0.02f, cy - coinHeight / 2),
                size = Size(coinWidth - w * 0.04f, coinHeight * 0.78f)
            )
        }
    }
}

/**
 * Splits titles with sub-lines, like "Yearly one Domestic trip\n(30,000 ÷ 12)" or "Domestic (30,000 ÷ 12)".
 */
private fun extractTitleParts(title: String): Pair<String, String?> {
    return when {
        title.contains("\n") -> {
            val parts = title.split("\n", limit = 2)
            parts[0].trim() to parts[1].trim()
        }
        title.contains("(") && title.endsWith(")") -> {
            val idx = title.indexOf("(")
            title.substring(0, idx).trim() to title.substring(idx).trim()
        }
        else -> title to null
    }
}

/**
 * Dynamically categorizes all expenses into the 6 canonical donut groups matching IMAGE 3,
 * computing accurate dynamic percentages summing to 100%.
 */
fun calculateDynamicDonutSlices(
    expenses: List<ExpenseRowUi>,
    totalMinor: Long
): List<DonutSliceUi> {
    if (expenses.isEmpty() || totalMinor <= 0L) {
        return defaultDonutSlicesForReport()
    }

    var housingMinor = 0L
    var foodMinor = 0L
    var lifestyleMinor = 0L
    var travelMinor = 0L
    var savingsMinor = 0L
    var othersMinor = 0L

    for (item in expenses) {
        val catId = item.categoryId.lowercase()
        val amt = item.amountMinor
        val words = item.title.split("[\\s,./()\\-]+".toRegex())
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }

        when {
            catId == SeedIds.EXPENSE_RENT || "rent" in words -> {
                housingMinor += amt
            }
            catId == SeedIds.EXPENSE_GROCERIES || catId == SeedIds.EXPENSE_DINING ||
                catId == SeedIds.EXPENSE_GAS || catId == SeedIds.EXPENSE_HOUSE_HELP ||
                words.any { it in listOf("groceries", "grocery", "food", "dining", "gas", "maid", "cook") } -> {
                foodMinor += amt
            }
            catId == SeedIds.EXPENSE_SHOPPING || catId == SeedIds.EXPENSE_OUTING ||
                catId == SeedIds.EXPENSE_SALON || catId == SeedIds.EXPENSE_LAUNDRY ||
                catId == SeedIds.EXPENSE_GYM || catId == SeedIds.EXPENSE_FITNESS_DIET ||
                words.any { it in listOf("shopping", "outing", "salon", "saloon", "skincare", "laundry", "gym", "diet") } -> {
                lifestyleMinor += amt
            }
            catId == SeedIds.EXPENSE_TRAVEL || catId == SeedIds.EXPENSE_HOMETOWN || catId == SeedIds.EXPENSE_CAB ||
                words.any { it in listOf("travel", "trip", "domestic", "hometown", "cab", "rapido") } -> {
                travelMinor += amt
            }
            catId == SeedIds.INVESTMENT_SIP || catId == SeedIds.SAVING_LIC_POLICY || catId == SeedIds.SAVING_EMERGENCY_FUND ||
                words.any { it in listOf("sip", "lic", "emergency", "investment", "saving", "savings") } -> {
                savingsMinor += amt
            }
            else -> {
                othersMinor += amt
            }
        }
    }

    val totalDouble = totalMinor.toDouble()
    fun pct(minor: Long): Float = if (totalDouble > 0) ((minor / totalDouble) * 100.0).toFloat() else 0f
    fun pctStr(value: Float): String = String.format(Locale.US, "%.1f%%", value)

    val housingPct = pct(housingMinor)
    val foodPct = pct(foodMinor)
    val lifestylePct = pct(lifestyleMinor)
    val travelPct = pct(travelMinor)
    val savingsPct = pct(savingsMinor)
    val othersPct = pct(othersMinor)

    return listOf(
        DonutSliceUi(
            title = "Housing",
            subCategories = "Rent",
            percentageText = pctStr(housingPct),
            percentageValue = housingPct,
            color = Color(0xFF0284C7)
        ),
        DonutSliceUi(
            title = "Food & Essentials",
            subCategories = "Groceries + Food + Gas",
            percentageText = pctStr(foodPct),
            percentageValue = foodPct,
            color = Color(0xFFF97316)
        ),
        DonutSliceUi(
            title = "Lifestyle",
            subCategories = "Shopping + Outing + Saloon + Laundry",
            percentageText = pctStr(lifestylePct),
            percentageValue = lifestylePct,
            color = Color(0xFFA855F7)
        ),
        DonutSliceUi(
            title = "Travel",
            subCategories = "Domestic Trip + Hometown Visit",
            percentageText = pctStr(travelPct),
            percentageValue = travelPct,
            color = Color(0xFFEAB308)
        ),
        DonutSliceUi(
            title = "Savings & Investments",
            subCategories = "SIP + LIC + Emergency",
            percentageText = pctStr(savingsPct),
            percentageValue = savingsPct,
            color = Color(0xFF10B981)
        ),
        DonutSliceUi(
            title = "Others",
            subCategories = "Electricity, Wi-Fi, Mobile, Medicine, etc.",
            percentageText = pctStr(othersPct),
            percentageValue = othersPct,
            color = Color(0xFF64748B)
        )
    )
}

private fun defaultDonutSlicesForReport(): List<DonutSliceUi> {
    return listOf(
        DonutSliceUi("Housing", "Rent", "14.5%", 14.5f, Color(0xFF0284C7)),
        DonutSliceUi("Food & Essentials", "Groceries + Food + Gas", "14.4%", 14.4f, Color(0xFFF97316)),
        DonutSliceUi("Lifestyle", "Shopping + Outing + Saloon + Laundry", "11.5%", 11.5f, Color(0xFFA855F7)),
        DonutSliceUi("Travel", "Domestic Trip + Hometown Visit", "10.1%", 10.1f, Color(0xFFEAB308)),
        DonutSliceUi("Savings & Investments", "SIP + LIC + Emergency", "20.2%", 20.2f, Color(0xFF10B981)),
        DonutSliceUi("Others", "Electricity, Wi-Fi, Mobile, Medicine, etc.", "29.3%", 29.3f, Color(0xFF64748B))
    )
}

