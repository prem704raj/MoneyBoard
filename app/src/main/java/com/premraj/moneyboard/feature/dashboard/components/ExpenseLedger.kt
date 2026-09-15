package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi

@Composable
fun ExpenseLedger(
    expenses: List<ExpenseRowUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MoneyBoardColors.LedgerLine,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        LedgerHeader()

        expenses.forEachIndexed { index, item ->
            LedgerRow(
                item = item
            )

            if (index != expenses.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            MoneyBoardColors.LedgerLine.copy(alpha = 0.72f)
                        )
                )
            }
        }
    }
}

@Composable
private fun LedgerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MoneyBoardColors.LedgerHeader,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "No.",
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MoneyBoardColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Expense",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MoneyBoardColors.TextPrimary
        )

        Text(
            text = "Monthly (₹)",
            modifier = Modifier.width(104.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MoneyBoardColors.TextPrimary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LedgerRow(
    item: ExpenseRowUi
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.number.toString(),
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MoneyBoardColors.Blue700
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = item.amountText,
            modifier = Modifier.width(104.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}
