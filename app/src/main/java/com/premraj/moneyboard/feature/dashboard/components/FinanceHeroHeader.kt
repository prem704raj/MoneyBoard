package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import com.premraj.moneyboard.feature.dashboard.formatIndianRupees
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun FinanceHeroHeader(
    salaryText: String = "₹0 / month",
    cityName: String = "Pune",
    profileTag: String = "Single Person",
    emiTag: String? = null,
    emiRupees: Long = 0L,
    expenseRows: List<ExpenseRowUi> = emptyList(),
    board: FinanceBoardUiModel? = null,
    mode: DashboardMode = DashboardMode.PLAN,
    selectedMonth: YearMonth? = null,
    onPreviousMonth: (() -> Unit)? = null,
    onNextMonth: (() -> Unit)? = null,
    onModeToggle: ((DashboardMode) -> Unit)? = null,
    onExportPdf: (() -> Unit)? = null,
    onExportImage: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    onLogTransaction: (() -> Unit)? = null,
    onEditSalary: (() -> Unit)? = null,
    onEditCity: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Aesthetic natural fine-art landscape painting background (no solid blue/green)
        Image(
            painter = painterResource(id = R.drawable.header_landscape_art),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Soft atmospheric dark vignette overlay to preserve high text contrast over the painting
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x990A0E1A), // Soft dark shade at top for status bar & title readability
                            Color(0x330A0E1A), // Subtle transparent center showing the picturesque landscape painting
                            Color(0xDD070B14)  // Rich grounded base for month & mode controls
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Title in ONE SINGLE LINE + Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Monthly ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Expense ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = Color(0xFFFDBA74),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Breakdown",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (onOpenSettings != null) {
                    HeaderIconButton(
                        icon = Icons.Rounded.Settings,
                        contentDescription = "Open Settings",
                        badgeColor = Color(0xFF7C3AED),
                        onClick = onOpenSettings
                    )
                }
            }

            // Row 2: Subtitle Chips (Profile, City, EMI if > 0)
            val derivedEmiRupees: Long = when {
                board != null -> {
                    val emiRow = board.expenseRows.firstOrNull {
                        it.categoryId == SeedIds.EXPENSE_EMI || it.title.equals("EMI", ignoreCase = true)
                    }
                    (emiRow?.amountMinor ?: 0L) / 100L
                }
                expenseRows.isNotEmpty() -> {
                    val emiRow = expenseRows.firstOrNull {
                        it.categoryId == SeedIds.EXPENSE_EMI || it.title.equals("EMI", ignoreCase = true)
                    }
                    (emiRow?.amountMinor ?: 0L) / 100L
                }
                emiRupees > 0L -> emiRupees
                !emiTag.isNullOrBlank() && !emiTag.equals("No EMI", ignoreCase = true) -> {
                    emiTag.filter { it.isDigit() }.toLongOrNull() ?: 0L
                }
                else -> 0L
            }

            val effectiveProfile = if (mode == DashboardMode.PLAN) {
                profileTag.trim().ifBlank { "Single Person" }
            } else {
                "Actual Expenses"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = effectiveProfile,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.32f)),
                    modifier = Modifier.clickable {
                        onEditCity?.invoke() ?: onOpenSettings?.invoke()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFFFDBA74),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = cityName.trim().ifBlank { "Pune" },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit Location",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }

                if (derivedEmiRupees > 0L) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "EMI ₹${formatIndianRupees(derivedEmiRupees)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Row 3: Colourful In-hand Salary Card + Distinct Colourful Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SalaryBadgePill(
                    salaryText = salaryText,
                    onClick = onEditSalary ?: onOpenSettings
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onLogTransaction != null) {
                        HeaderIconButton(
                            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = "Log Expense",
                            badgeColor = Color(0xFFF59E0B),
                            onClick = onLogTransaction
                        )
                    }

                    if (onExportImage != null) {
                        HeaderIconButton(
                            icon = Icons.Rounded.Image,
                            contentDescription = "Export as Image",
                            badgeColor = Color(0xFF0284C7),
                            onClick = onExportImage
                        )
                    }

                    if (onExportPdf != null) {
                        HeaderIconButton(
                            icon = Icons.Rounded.PictureAsPdf,
                            contentDescription = "Export PDF Report",
                            badgeColor = Color(0xFFE11D48),
                            onClick = onExportPdf
                        )
                    }
                }
            }

            // Row 4: Month navigation and Mode switcher row
            if (selectedMonth != null && onPreviousMonth != null && onNextMonth != null) {
                MonthAndModeControlBar(
                    month = selectedMonth,
                    mode = mode,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                    onModeToggle = onModeToggle
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(badgeColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SalaryBadgePill(
    salaryText: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                RoundedCornerShape(10.dp)
            )
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "In-hand Salary",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    softWrap = false
                )
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(10.dp)
                )
            }

            Text(
                text = salaryText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp
                ),
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun MonthAndModeControlBar(
    month: YearMonth,
    mode: DashboardMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onModeToggle: ((DashboardMode) -> Unit)?
) {
    val monthText = month.format(DateTimeFormatter.ofPattern("MMM yyyy"))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.22f),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month navigation (< Month Year >)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Previous Month",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onPrevious() }
            )

            Text(
                text = monthText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                ),
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Next Month",
                tint = Color.White,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNext() }
            )
        }

        // Mode switch (PLAN / ACTUAL)
        if (onModeToggle != null) {
            Row(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                ModePill(
                    title = "PLAN",
                    selected = mode == DashboardMode.PLAN,
                    selectedBg = Color.White,
                    selectedTextColor = Color(0xFF091830),
                    onClick = { onModeToggle(DashboardMode.PLAN) }
                )
                ModePill(
                    title = "ACTUAL",
                    selected = mode == DashboardMode.ACTUAL,
                    selectedBg = Color(0xFF10B981),
                    selectedTextColor = Color.White,
                    onClick = { onModeToggle(DashboardMode.ACTUAL) }
                )
            }
        }
    }
}

@Composable
private fun ModePill(
    title: String,
    selected: Boolean,
    selectedBg: Color,
    selectedTextColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (selected) selectedBg else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (selected) selectedTextColor else Color.White.copy(alpha = 0.75f),
            maxLines = 1,
            softWrap = false
        )
    }
}
