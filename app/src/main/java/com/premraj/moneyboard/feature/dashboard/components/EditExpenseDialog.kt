package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi

@Composable
fun EditExpenseDialog(
    expense: ExpenseRowUi,
    mode: DashboardMode = DashboardMode.PLAN,
    onDismiss: () -> Unit,
    onSaveAmount: (Long) -> Unit,
    onRemoveExpense: () -> Unit
) {
    val initialCleanAmount = expense.amountText.replace(",", "").replace("₹", "").trim()
    var amountText by remember { mutableStateOf(initialCleanAmount) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val isActualMode = mode == DashboardMode.ACTUAL

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
            titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
            textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
            title = {
                Text(
                    if (isActualMode) "Clear Actual Spend for ${expense.title.split("\n")[0]}?"
                    else "Remove ${expense.title.split("\n")[0]}?"
                )
            },
            text = {
                Text(
                    if (isActualMode)
                        "This will reset your recorded actual spending for this category back to ₹0 for this month."
                    else
                        "This will exclude this item from your monthly budget plan. Your total expenses, savings rate, and annual calculations will update automatically."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDelete = false
                        onRemoveExpense()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isActualMode) "Clear" else "Remove", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDelete = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
        titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
        textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(expense.badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = expense.icon,
                        contentDescription = null,
                        tint = expense.badgeIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = expense.title.split("\n")[0],
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                    )
                    Text(
                        text = if (isActualMode) "Actual Spent Tracking" else "Monthly Budget Blueprint",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActualMode) MoneyBoardColors.Blue600 else MoneyBoardColors.Green700
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (expense.title.contains("\n")) {
                    Text(
                        text = expense.title.split("\n")[1],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() }
                    },
                    label = { Text(if (isActualMode) "Actual Spent Amount (₹)" else "Monthly Budget (₹)") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Remove / Clear button
                OutlinedButton(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActualMode) "Clear Actual Spend" else "Remove / Do Not Pay This",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = (amountText.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    onSaveAmount(amount)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isActualMode) "Save Actual Spend" else "Save Changes",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
