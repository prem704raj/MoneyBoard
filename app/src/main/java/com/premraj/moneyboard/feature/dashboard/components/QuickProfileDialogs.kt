package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditSalaryQuickDialog(
    currentSalaryRupees: Long,
    onDismiss: () -> Unit,
    onSaveSalary: (Long) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    var salaryInput by remember { mutableStateOf(currentSalaryRupees.toString()) }
    val presets = listOf(50_000L, 70_000L, 85_000L, 100_000L, 150_000L)

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
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700
                )
                Text(
                    text = "Edit In-hand Salary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Update your monthly net take-home salary. Your budget balance and savings rate will recalculate immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                OutlinedTextField(
                    value = salaryInput,
                    onValueChange = { input ->
                        salaryInput = input.filter { it.isDigit() }
                    },
                    prefix = {
                        Text(
                            text = "₹ ",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                        )
                    },
                    label = { Text("Monthly In-hand Salary") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                        unfocusedTextColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                        focusedBorderColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700,
                        unfocusedBorderColor = if (isDark) Color(0xFF2E4260) else MoneyBoardColors.LedgerLine,
                        focusedLabelColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700,
                        unfocusedLabelColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
                        cursorColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700
                    )
                )

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = salaryInput == preset.toString()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900
                            } else {
                                if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) {
                                    if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900
                                } else {
                                    if (isDark) Color(0xFF2E4260) else MoneyBoardColors.LedgerLine
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { salaryInput = preset.toString() }
                        ) {
                            Text(
                                text = "₹${"%,d".format(preset)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = salaryInput.toLongOrNull() ?: currentSalaryRupees
                    if (amount > 0) {
                        onSaveSalary(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Salary", color = Color.White, fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditCityQuickDialog(
    currentCity: String,
    onDismiss: () -> Unit,
    onSaveCity: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    var cityInput by remember { mutableStateOf(currentCity) }
    val presets = listOf("Pune", "Mumbai", "Bengaluru", "Delhi NCR", "Hyderabad", "Chennai", "Kolkata")

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
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700
                )
                Text(
                    text = "Change Location",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select or type your city. This personalizes cost-of-living insights and PDF export reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                OutlinedTextField(
                    value = cityInput,
                    onValueChange = { cityInput = it },
                    label = { Text("City Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                        unfocusedTextColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                        focusedBorderColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700,
                        unfocusedBorderColor = if (isDark) Color(0xFF2E4260) else MoneyBoardColors.LedgerLine,
                        focusedLabelColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700,
                        unfocusedLabelColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
                        cursorColor = if (isDark) Color(0xFF64B5F6) else MoneyBoardColors.Blue700
                    )
                )

                Text(
                    text = "Popular Cities:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = cityInput.equals(preset, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900
                            } else {
                                if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) {
                                    if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900
                                } else {
                                    if (isDark) Color(0xFF2E4260) else MoneyBoardColors.LedgerLine
                                }
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { cityInput = preset }
                        ) {
                            Text(
                                text = preset,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = cityInput.trim().ifEmpty { "Pune" }
                    onSaveCity(trimmed)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Location", color = Color.White, fontWeight = FontWeight.Bold)
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

