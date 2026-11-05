package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi

@Composable
fun ExpenseLedger(
    expenses: List<ExpenseRowUi>,
    onExpenseClick: ((ExpenseRowUi) -> Unit)? = null,
    onAddExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (expenses.isEmpty() && onAddExpenseClick == null) return

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 600.dp

            // On tablets/desktop (isWide), split into 2 columns if >12 items; on phones, render a full-width readable single column
            val splitColumns = isWide && expenses.size > 12
            val half = if (splitColumns) (expenses.size + 1) / 2 else expenses.size
            val leftColumn = expenses.take(half)
            val rightColumn = if (splitColumns) expenses.drop(half) else emptyList()

            if (rightColumn.isEmpty()) {
                SingleTableCard(
                    items = leftColumn,
                    isCompact = false,
                    onExpenseClick = onExpenseClick
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SingleTableCard(
                            items = leftColumn,
                            isCompact = !isWide,
                            onExpenseClick = onExpenseClick
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SingleTableCard(
                            items = rightColumn,
                            isCompact = !isWide,
                            onExpenseClick = onExpenseClick
                        )
                    }
                }
            }
        }

        // Action button to Add / Restore Expense
        if (onAddExpenseClick != null) {
            val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDark) Color(0xFF142436) else MoneyBoardColors.SalarySurface)
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color(0xFF1E3B5C) else MoneyBoardColors.SalaryBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onAddExpenseClick() }
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add / Customize Expense Items",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp
                    ),
                    color = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700
                )
            }
        }
    }
}

@Composable
private fun SingleTableCard(
    items: List<ExpenseRowUi>,
    isCompact: Boolean = true,
    onExpenseClick: ((ExpenseRowUi) -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val cardBg = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight
    val borderColor = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = cardBg
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Table Header
            LedgerTableHeader(isCompact = isCompact, isDark = isDark)

            // Rows
            items.forEachIndexed { index, item ->
                LedgerTableRow(
                    item = item,
                    isCompact = isCompact,
                    isDark = isDark,
                    onClick = if (onExpenseClick != null) { { onExpenseClick(item) } } else null
                )

                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerTableHeader(isCompact: Boolean, isDark: Boolean = false) {
    val headerBg = if (isDark) Color(0xFF16324F) else MoneyBoardColors.LedgerHeader
    val headerText = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.HeaderNavyDark

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(headerBg)
            .padding(
                horizontal = if (isCompact) 4.dp else 8.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "No.",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 10.sp else 11.sp
            ),
            color = headerText,
            modifier = Modifier.width(if (isCompact) 20.dp else 26.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Expense",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 10.sp else 11.sp
            ),
            color = headerText,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )

        Text(
            text = "Monthly (₹)",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 10.sp else 11.sp
            ),
            color = headerText,
            textAlign = TextAlign.End,
            modifier = Modifier.width(if (isCompact) 56.dp else 72.dp)
        )
    }
}

@Composable
private fun LedgerTableRow(
    item: ExpenseRowUi,
    isCompact: Boolean,
    isDark: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val primaryText = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
    val secondaryText = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
            .padding(
                horizontal = if (isCompact) 4.dp else 8.dp,
                vertical = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number
        Text(
            text = item.number.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (isCompact) 10.sp else 11.sp,
                fontWeight = FontWeight.Normal
            ),
            color = secondaryText,
            modifier = Modifier.width(if (isCompact) 20.dp else 26.dp),
            textAlign = TextAlign.Center
        )

        // Illustrated Cropped Icon or Native Emoji (Untinted, matching reference infographic Image 2)
        ExpenseIconView(
            item = item,
            size = if (isCompact) 20.dp else 24.dp
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Title and optional formula subtext
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 2.dp)
        ) {
            val lines = item.title.split("\n")
            Text(
                text = lines[0],
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = if (isCompact) 10.sp else 11.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (lines.size > 1) {
                Text(
                    text = lines[1],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = if (isCompact) 8.sp else 9.5.sp
                    ),
                    color = secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Amount
        Text(
            text = item.amountText.replace("₹", "").trim(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = if (isCompact) 10.5.sp else 12.sp,
                fontWeight = FontWeight.Bold
            ),
            color = primaryText,
            textAlign = TextAlign.End,
            modifier = Modifier.width(if (isCompact) 56.dp else 72.dp)
        )
    }
}
