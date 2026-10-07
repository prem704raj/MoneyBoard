package com.premraj.moneyboard.feature.transactions

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.feature.dashboard.categoryIcon
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTransactionSheet(
    accounts: List<Account>,
    categories: List<Category>,
    editingTransaction: FinanceTransaction?,
    onDismiss: () -> Unit,
    onSave: (
        accountId: String,
        categoryId: String,
        type: TransactionType,
        amount: BigDecimal,
        date: LocalDate,
        note: String?,
        merchant: String?
    ) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Allowed types: NO TRANSFER in v1 UI per MB-006!
    val allowedTypes = listOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.SAVING,
        TransactionType.INVESTMENT
    )

    var selectedType by remember {
        mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE)
    }

    var amountText by remember {
        val initialAmount = editingTransaction?.let {
            BigDecimal(it.amount.amountMinor).movePointLeft(2).stripTrailingZeros().toPlainString()
        } ?: ""
        mutableStateOf(initialAmount)
    }

    var selectedAccountId by remember {
        mutableStateOf(editingTransaction?.accountId ?: accounts.firstOrNull()?.id ?: "")
    }

    // Categories matching the selected type
    val matchingCategories = remember(categories, selectedType) {
        categories.filter { it.type.accepts(selectedType) }
    }

    var selectedCategoryId by remember {
        val initialCatId = editingTransaction?.categoryId
        val found = matchingCategories.firstOrNull { it.id == initialCatId }
        mutableStateOf(found?.id ?: matchingCategories.firstOrNull()?.id ?: "")
    }

    // Update category selection when type changes if current category is incompatible
    if (matchingCategories.none { it.id == selectedCategoryId }) {
        selectedCategoryId = matchingCategories.firstOrNull()?.id ?: ""
    }

    var selectedDate by remember {
        mutableStateOf(editingTransaction?.accountingDate ?: LocalDate.now())
    }

    var noteText by remember {
        mutableStateOf(editingTransaction?.note ?: "")
    }

    var merchantText by remember {
        mutableStateOf(editingTransaction?.merchant ?: "")
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingTransaction == null) "New Transaction" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Type Selector (Excludes TRANSFER)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allowedTypes.forEach { type ->
                    val isSelected = selectedType == type
                    val (label, activeColor) = when (type) {
                        TransactionType.EXPENSE -> "Expense" to MoneyBoardColors.Red600
                        TransactionType.INCOME -> "Income" to MoneyBoardColors.Green700
                        TransactionType.SAVING -> "Saving" to MoneyBoardColors.Blue600
                        TransactionType.INVESTMENT -> "Investment" to MoneyBoardColors.Purple700
                        TransactionType.TRANSFER -> "Transfer" to MoneyBoardColors.Orange700
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    inputError = null
                },
                label = { Text("Amount") },
                placeholder = { Text("0.00") },
                prefix = {
                    Text(
                        text = "₹ ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = inputError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Account Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = acc.id == selectedAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text(acc.name) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MoneyBoardColors.Navy900,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Category Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    matchingCategories.forEach { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = categoryIcon(cat.iconKey),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            },
                            label = { Text(cat.name, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MoneyBoardColors.Navy900,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Date Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Note (Optional)
            OutlinedTextField(
                value = noteText,
                onValueChange = { if (it.length <= 500) noteText = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("e.g. Grocery run, monthly rent...") },
                singleLine = false,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Merchant / Payee (Optional)
            OutlinedTextField(
                value = merchantText,
                onValueChange = { if (it.length <= 120) merchantText = it },
                label = { Text("Merchant / Payee (Optional)") },
                placeholder = { Text("e.g. Amazon, Swiggy, DMart...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Error message if any
            if (inputError != null) {
                Text(
                    text = inputError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (editingTransaction != null && onDelete != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(editingTransaction.id)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete")
                    }
                }

                Button(
                    onClick = {
                        val parsed = try {
                            val v = BigDecimal(amountText.trim())
                            if (v <= BigDecimal.ZERO) null else v
                        } catch (e: Exception) {
                            null
                        }

                        when {
                            parsed == null -> inputError = "Please enter a valid amount greater than zero"
                            selectedAccountId.isBlank() -> inputError = "Please select an account"
                            selectedCategoryId.isBlank() -> inputError = "Please select a category"
                            else -> {
                                onSave(
                                    selectedAccountId,
                                    selectedCategoryId,
                                    selectedType,
                                    parsed,
                                    selectedDate,
                                    noteText,
                                    merchantText
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(if (editingTransaction != null && onDelete != null) 1.5f else 1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoneyBoardColors.Navy900)
                ) {
                    Text(
                        text = if (editingTransaction == null) "Add Transaction" else "Save Changes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
