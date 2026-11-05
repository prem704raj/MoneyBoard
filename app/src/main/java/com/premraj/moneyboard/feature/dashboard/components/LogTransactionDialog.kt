package com.premraj.moneyboard.feature.dashboard.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import com.premraj.moneyboard.feature.dashboard.categoryBadge

@Composable
fun LogTransactionDialog(
    availableCategories: List<ExpenseRowUi>,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        categoryId: String,
        amountRupees: Long,
        merchant: String?,
        note: String?,
        accountId: String
    ) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember {
        mutableStateOf(availableCategories.firstOrNull()?.categoryId ?: SeedIds.EXPENSE_DINING)
    }
    var merchantText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("account.cash") } // Cash or Bank

    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
        titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
        textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MoneyBoardColors.BadgeTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Receipt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Log Actual Expense",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input -> amountText = input.filter { it.isDigit() } },
                    label = { Text("Expense Amount (₹)") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableCategories) { cat ->
                        val isSelected = cat.categoryId == selectedCategoryId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedCategoryId = cat.categoryId }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) {
                                        if (isDark) Color(0xFF60A5FA) else MoneyBoardColors.Blue700
                                    } else {
                                        if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) {
                                if (isDark) Color(0xFF1B3654) else MoneyBoardColors.SalarySurface
                            } else {
                                if (isDark) MoneyBoardColors.DarkSurface else MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(cat.badgeColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = null,
                                            tint = cat.badgeIconTint,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    Text(
                                        text = cat.title.split("\n")[0],
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF60A5FA) else MoneyBoardColors.Blue700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Merchant / Location
                OutlinedTextField(
                    value = merchantText,
                    onValueChange = { merchantText = it },
                    label = { Text("Merchant / Store (e.g. Swiggy, DMart, Shell)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Description (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount > 0L) {
                        onSaveTransaction(
                            selectedCategoryId,
                            amount,
                            merchantText.trim().ifEmpty { null },
                            noteText.trim().ifEmpty { null },
                            selectedAccount
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Record Expense", color = Color.White, fontWeight = FontWeight.Bold)
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

