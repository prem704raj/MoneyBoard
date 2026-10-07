package com.premraj.moneyboard.feature.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.feature.dashboard.categoryIcon
import java.math.BigDecimal
import java.time.format.TextStyle

@Composable
fun PlanningScreen(
    viewModel: PlanningViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val locale = LocalConfiguration.current.locales[0]

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Month Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Planning • ${state.selectedMonth.month.getDisplayName(TextStyle.FULL, locale)} ${state.selectedMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Planned Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.summary.totalIncomeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MoneyBoardColors.Green700
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Budget Allocated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.summary.totalAllocatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MoneyBoardColors.Orange700
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Unallocated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.summary.remainingBalanceText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (state.summary.isPositive) MoneyBoardColors.Green700 else MoneyBoardColors.Red600
                        )
                    }
                }
            }

            // Plans List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Income Streams
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planned Income",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = viewModel::openAddIncomeDialog) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Income")
                        }
                    }
                }

                if (state.incomePlans.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "No income planned for this month. Tap '+ Add Income' to set expected salary or earnings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(state.incomePlans, key = { it.id }) { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditIncomeDialog(plan.raw) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MoneyBoardColors.SalarySurface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon("salary"),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MoneyBoardColors.Green700
                                        )
                                    }
                                    Text(
                                        text = plan.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = plan.amountText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MoneyBoardColors.Green700
                                )
                            }
                        }
                    }
                }

                // Section: Expense Category Budgets
                item {
                    Text(
                        text = "Expense Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.expensePlans, key = { it.categoryId }) { catPlan ->
                    CategoryBudgetRow(
                        item = catPlan,
                        accentColor = MoneyBoardColors.Red600,
                        surfaceColor = MoneyBoardColors.SpendingSurface,
                        onClick = { viewModel.openEditCategoryPlanDialog(catPlan) }
                    )
                }

                // Section: Savings & Investments
                item {
                    Text(
                        text = "Savings & Investment Targets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.savingPlans + state.investmentPlans, key = { it.categoryId }) { catPlan ->
                    CategoryBudgetRow(
                        item = catPlan,
                        accentColor = if (catPlan.type == CategoryType.SAVING) MoneyBoardColors.Blue600 else MoneyBoardColors.Purple700,
                        surfaceColor = if (catPlan.type == CategoryType.SAVING) MoneyBoardColors.SavingSurface else MoneyBoardColors.AnnualSurface,
                        onClick = { viewModel.openEditCategoryPlanDialog(catPlan) }
                    )
                }
            }
        }
    }

    // Add / Edit Income Dialog
    if (state.isAddIncomeDialogOpen) {
        var nameText by remember { mutableStateOf(state.editingIncomePlan?.name ?: "Salary") }
        var amountText by remember {
            val init = state.editingIncomePlan?.let {
                BigDecimal(it.plannedAmount.amountMinor).movePointLeft(2).stripTrailingZeros().toPlainString()
            } ?: ""
            mutableStateOf(init)
        }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = viewModel::closeIncomeDialog,
            title = {
                Text(if (state.editingIncomePlan == null) "Add Income Target" else "Edit Income Target")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Income Name") },
                        placeholder = { Text("e.g. Salary, Dividend, Freelance") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMsg = null
                        },
                        label = { Text("Planned Amount") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(text = errorMsg!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = try {
                            val v = BigDecimal(amountText.trim())
                            if (v <= BigDecimal.ZERO) null else v
                        } catch (e: Exception) {
                            null
                        }
                        if (nameText.isBlank()) {
                            errorMsg = "Please enter an income name"
                        } else if (parsed == null) {
                            errorMsg = "Please enter a valid amount greater than zero"
                        } else {
                            viewModel.saveIncomePlan(nameText, parsed)
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.editingIncomePlan != null) {
                        OutlinedButton(
                            onClick = { viewModel.deleteIncomePlan(state.editingIncomePlan!!.id) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = viewModel::closeIncomeDialog) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Edit Category Budget Dialog
    if (state.editingCategoryPlan != null) {
        val cat = state.editingCategoryPlan!!
        var amountText by remember {
            val init = if (cat.plannedAmountMinor > 0) {
                BigDecimal(cat.plannedAmountMinor).movePointLeft(2).stripTrailingZeros().toPlainString()
            } else ""
            mutableStateOf(init)
        }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = viewModel::closeCategoryPlanDialog,
            title = {
                Text("Set Budget: ${cat.categoryName}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter monthly budget for ${cat.categoryName}. Enter 0 to remove this category budget.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMsg = null
                        },
                        label = { Text("Budget Amount") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMsg != null
                    )
                    if (errorMsg != null) {
                        Text(text = errorMsg!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = try {
                            val trimmed = amountText.trim()
                            if (trimmed.isEmpty()) BigDecimal.ZERO else BigDecimal(trimmed)
                        } catch (e: Exception) {
                            null
                        }
                        if (parsed == null || parsed < BigDecimal.ZERO) {
                            errorMsg = "Please enter a valid non-negative amount"
                        } else {
                            viewModel.saveCategoryPlan(cat.categoryId, parsed)
                        }
                    }
                ) {
                    Text("Set Budget")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeCategoryPlanDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryBudgetRow(
    item: CategoryPlanItemUi,
    accentColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(surfaceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon(item.iconKey),
                        contentDescription = item.categoryName,
                        modifier = Modifier.size(18.dp),
                        tint = accentColor
                    )
                }
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.plannedAmountText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.hasBudget) FontWeight.Bold else FontWeight.Normal,
                    color = if (item.hasBudget) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit budget",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
