package com.premraj.moneyboard.feature.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.validation.EmojiValidator
import com.premraj.moneyboard.feature.dashboard.categoryBadge

data class AvailableCategoryItem(
    val categoryId: String,
    val name: String,
    val iconKey: String,
    val defaultRupees: Long,
    val group: CategoryGroup = CategoryGroup.OTHER,
    val type: CategoryType = CategoryType.EXPENSE,
    val systemCategory: Boolean = true
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    existingCategoryIds: Set<String>,
    onDismiss: () -> Unit,
    onAddExpense: (categoryId: String, amountRupees: Long) -> Unit,
    onAddCustomExpense: ((name: String, amountRupees: Long, emoji: String, type: CategoryType, group: CategoryGroup) -> Unit)? = null
) {
    val allOptions = remember {
        listOf(
            AvailableCategoryItem(SeedIds.EXPENSE_RENT, "Rent", "rent", 10_000L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_GROCERIES, "Groceries", "groceries", 6_000L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_GAS, "Gas", "gas", 900L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_HOUSE_HELP, "Maid", "maid", 3_000L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_DINING, "Online food", "online_food", 3_000L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_ELECTRICITY, "Electricity", "electricity", 1_500L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_WIFI, "Wi-Fi", "wifi", 600L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_MOBILE, "Mobile recharge", "mobile", 350L, CategoryGroup.UTILITIES, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_CAB, "Cab/Rapido", "cab", 2_000L, CategoryGroup.TRANSPORT, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_LAUNDRY, "Laundry", "laundry", 300L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_SHOPPING, "Shopping", "shopping", 2_000L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_GYM, "Gym", "gym", 1_200L, CategoryGroup.HEALTH, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_FITNESS_DIET, "Gym diet", "gym_diet", 2_000L, CategoryGroup.HEALTH, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_SALON, "Skincare and saloon", "salon", 1_000L, CategoryGroup.HEALTH, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_OUTING, "Weekend outing", "outing", 3_000L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_TRAVEL, "Yearly one Domestic trip (30,000 ÷ 12)", "travel", 2_500L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_HOMETOWN, "Yearly 2 times Hometown visit (42,000 ÷ 12)", "hometown", 3_500L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_MEDICINE, "Medicine", "medicine", 500L, CategoryGroup.HEALTH, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_FAMILY_SUPPORT, "Sending to parents", "family", 10_000L, CategoryGroup.FAMILY_SUPPORT, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.INVESTMENT_SIP, "SIP", "sip", 5_000L, CategoryGroup.INVESTMENTS, CategoryType.INVESTMENT),
            AvailableCategoryItem(SeedIds.EXPENSE_HEALTH_INSURANCE, "Health insurance", "health_insurance", 1_000L, CategoryGroup.HEALTH, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.SAVING_LIC_POLICY, "LIC", "lic", 2_000L, CategoryGroup.INSURANCE, CategoryType.SAVING),
            AvailableCategoryItem(SeedIds.SAVING_EMERGENCY_FUND, "Emergency fund", "emergency_fund", 7_000L, CategoryGroup.SAVINGS, CategoryType.SAVING),
            AvailableCategoryItem(SeedIds.EXPENSE_WATER, "Water", "water", 500L, CategoryGroup.HOUSING, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_FUEL, "Fuel", "fuel", 2_000L, CategoryGroup.TRANSPORT, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_PUBLIC_TRANSPORT, "Public Transport", "bus", 1_000L, CategoryGroup.TRANSPORT, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_EDUCATION, "Education", "education", 1_000L, CategoryGroup.EDUCATION, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_EMI, "EMI", "debt", 5_000L, CategoryGroup.DEBT, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_SUBSCRIPTIONS, "Subscriptions", "subscriptions", 600L, CategoryGroup.LIFESTYLE, CategoryType.EXPENSE),
            AvailableCategoryItem(SeedIds.EXPENSE_OTHER, "Other expenses", "other", 1_000L, CategoryGroup.OTHER, CategoryType.EXPENSE)
        )
    }

    val removedPresets = remember(existingCategoryIds) {
        allOptions.filter { it.systemCategory && !existingCategoryIds.contains(it.categoryId) }
    }

    // Default to Restore section (0) if there are removed presets; otherwise Custom section (1)
    var selectedTab by remember { mutableIntStateOf(if (removedPresets.isNotEmpty()) 0 else 1) }

    // Custom form state
    var customName by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }
    var customIcon by remember { mutableStateOf("") }
    var isCustomEssential by remember { mutableStateOf(true) }
    var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }
    var selectedGroup by remember { mutableStateOf(CategoryGroup.LIFESTYLE) }

    // Selected preset for amount customisation
    var selectedPresetItem by remember { mutableStateOf<AvailableCategoryItem?>(null) }
    var presetAmountInput by remember { mutableStateOf("") }

    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
        titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
        textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
        title = {
            Text(
                text = if (selectedPresetItem != null) "Set Amount for ${selectedPresetItem?.name}" else "Add / Restore Expense",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
            )
        },
        text = {
            if (selectedPresetItem != null) {
                // Preset amount adjustment sub-view
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Customize the monthly amount for this preset:",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                    OutlinedTextField(
                        value = presetAmountInput,
                        onValueChange = { presetAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Monthly Amount (₹)") },
                        prefix = { Text("₹ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Two clear sections switcher (Restore Presets vs Add Custom)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = if (isDark) Color(0xFF142436) else MoneyBoardColors.BackgroundLight,
                        contentColor = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Navy900
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = if (removedPresets.isNotEmpty()) "Restore (${removedPresets.size})" else "Restore",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Add Custom",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }

                    // Section A: Restore Section
                    if (selectedTab == 0) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (removedPresets.isEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) Color(0xFF142436) else MoneyBoardColors.SalarySurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFF1E3B5C) else MoneyBoardColors.SalaryBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF81C784) else MoneyBoardColors.Green700,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "All preset expenses are currently active",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Removed presets (${removedPresets.size}) — tap to restore:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(removedPresets) { item ->
                                        PresetItemRow(
                                            item = item,
                                            isAlreadyActive = false,
                                            isDark = isDark,
                                            onClick = {
                                                selectedPresetItem = item
                                                presetAmountInput = item.defaultRupees.toString()
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "All Preset Expenses Catalog:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                            )

                            // Complete catalog of all options using FlowRow to prevent character wrapping
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(allOptions) { item ->
                                    val isAlreadyActive = existingCategoryIds.contains(item.categoryId)
                                    PresetItemRow(
                                        item = item,
                                        isAlreadyActive = isAlreadyActive,
                                        isDark = isDark,
                                        onClick = {
                                            selectedPresetItem = item
                                            presetAmountInput = item.defaultRupees.toString()
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Section B: Add Custom Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 340.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text("Expense Name *") },
                                placeholder = { Text("e.g. Piano Lessons") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = customAmount,
                                onValueChange = { customAmount = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Monthly Amount (₹) *") },
                                prefix = { Text("₹ ") },
                                placeholder = { Text("e.g. 3000") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            val isEmojiValid = customIcon.isNotBlank() && EmojiValidator.isValidEmoji(customIcon)
                            val isEmojiError = customIcon.isNotBlank() && !isEmojiValid

                            OutlinedTextField(
                                value = customIcon,
                                onValueChange = { input ->
                                    val grapheme = EmojiValidator.extractFirstGrapheme(input.trim())
                                    customIcon = grapheme ?: input.trim()
                                },
                                label = { Text("Icon (Single Emoji) *") },
                                placeholder = { Text("e.g. 🎹, 🍕, 🎸, 🐶") },
                                singleLine = true,
                                isError = isEmojiError,
                                supportingText = {
                                    if (isEmojiError) {
                                        Text(
                                            text = "Invalid emoji. Non-emoji characters rejected.",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp
                                        )
                                    } else if (isEmojiValid) {
                                        Text(
                                            text = "Valid emoji icon preview: $customIcon",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text("Enter 1 emoji character from keyboard", fontSize = 11.sp)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Category Type",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    CategoryType.EXPENSE to "Expense",
                                    CategoryType.SAVING to "Saving",
                                    CategoryType.INVESTMENT to "Investment"
                                ).forEach { (type, label) ->
                                    FilterChip(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    )
                                }
                            }

                            Text(
                                text = "Category Group",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    CategoryGroup.LIFESTYLE to "Lifestyle",
                                    CategoryGroup.HEALTH to "Health",
                                    CategoryGroup.TRANSPORT to "Transport",
                                    CategoryGroup.HOUSING to "Housing",
                                    CategoryGroup.OTHER to "Other"
                                ).forEach { (group, label) ->
                                    FilterChip(
                                        selected = selectedGroup == group,
                                        onClick = { selectedGroup = group },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Text(
                                text = "Report Section",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = isCustomEssential,
                                    onClick = { isCustomEssential = true },
                                    label = { Text("Essential Expenses", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = !isCustomEssential,
                                    onClick = { isCustomEssential = false },
                                    label = { Text("Other Expenses", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedPresetItem != null) {
                Button(
                    onClick = {
                        val amount = presetAmountInput.toLongOrNull() ?: selectedPresetItem?.defaultRupees ?: 1_000L
                        onAddExpense(selectedPresetItem!!.categoryId, amount)
                        selectedPresetItem = null
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Restore Preset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else if (selectedTab == 1) {
                val isFormValid = customName.trim().isNotBlank() &&
                    (customAmount.toLongOrNull() ?: 0L) > 0L &&
                    EmojiValidator.isValidEmoji(customIcon)

                Button(
                    onClick = {
                        val amt = customAmount.toLong()
                        val taggedIcon = if (isCustomEssential) "${customIcon.trim()}|essential" else "${customIcon.trim()}|other"
                        if (onAddCustomExpense != null) {
                            onAddCustomExpense(
                                customName.trim(),
                                amt,
                                taggedIcon,
                                selectedType,
                                selectedGroup
                            )
                        } else {
                            onAddExpense(SeedIds.EXPENSE_OTHER, amt)
                        }
                        onDismiss()
                    },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add Custom Expense", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (selectedPresetItem != null) {
                        selectedPresetItem = null
                    } else {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )
            ) {
                Text(if (selectedPresetItem != null) "Back" else "Cancel")
            }
        }
    )
}

/**
 * Individual preset row using Compose FlowRow for item title and badge.
 * Badge text has maxLines = 1 and softWrap = false to permanently prevent vertical character stacking (Image 1 fix).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PresetItemRow(
    item: AvailableCategoryItem,
    isAlreadyActive: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val drawableRes = CategoryIconMapper.getPresetDrawable(item.categoryId, item.iconKey)
    val badge = categoryBadge(item.iconKey)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isAlreadyActive) {
            if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight
        } else {
            if (isDark) MoneyBoardColors.DarkSurface else MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(26.dp),
                contentAlignment = Alignment.Center
            ) {
                if (drawableRes != null) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(badge.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badge.icon,
                            contentDescription = null,
                            tint = badge.iconTint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // FlowRow layout: Title and badge cleanly flow without squeezing badge into vertical character stack
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Text(
                    text = if (isAlreadyActive) "Active" else "₹${item.defaultRupees}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isAlreadyActive) FontWeight.Normal else FontWeight.Bold
                    ),
                    color = if (isAlreadyActive) {
                        if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    } else {
                        if (isDark) Color(0xFF81C784) else MoneyBoardColors.Green700
                    },
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
