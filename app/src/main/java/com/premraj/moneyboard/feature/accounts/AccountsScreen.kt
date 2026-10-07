package com.premraj.moneyboard.feature.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.AccountType
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddAccountDialog,
                containerColor = MoneyBoardColors.Navy900,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Account")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Screen Header
            Text(
                text = "Accounts & Net Worth",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Net Worth Summary Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoneyBoardColors.Navy900),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED NET WORTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.summary.netWorthText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Cash & Bank",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = state.summary.totalCashAndBankText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Investments",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = state.summary.totalInvestmentsText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Liabilities",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                text = state.summary.totalLiabilitiesText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFB4AB)
                            )
                        }
                    }
                }
            }

            // Accounts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Your Accounts (${state.accounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(state.accounts, key = { it.id }) { acc ->
                    AccountCard(
                        account = acc,
                        onArchive = { viewModel.promptArchiveAccount(acc) }
                    )
                }
            }
        }
    }

    // Add Account Dialog
    if (state.isAddAccountDialogOpen) {
        var nameText by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(AccountType.BANK) }
        var balanceText by remember { mutableStateOf("0.00") }
        var includeInNetWorth by remember { mutableStateOf(true) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        val availableTypes = listOf(
            AccountType.BANK to "Bank",
            AccountType.CASH to "Cash",
            AccountType.WALLET to "Wallet",
            AccountType.CREDIT_CARD to "Credit Card",
            AccountType.INVESTMENT to "Investment",
            AccountType.OTHER to "Other"
        )

        AlertDialog(
            onDismissRequest = viewModel::closeAddAccountDialog,
            title = { Text("Add Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { if (it.length <= 60) nameText = it },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g. HDFC Salary, Wallet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Account Type",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableTypes.forEach { (type, label) ->
                                val isSelected = selectedType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedType = type },
                                    label = { Text(label, fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MoneyBoardColors.Navy900,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = {
                            balanceText = it
                            errorMsg = null
                        },
                        label = { Text("Opening Balance") },
                        prefix = { Text("₹ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Include in Net Worth",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = includeInNetWorth,
                            onCheckedChange = { includeInNetWorth = it }
                        )
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = try {
                            BigDecimal(balanceText.trim())
                        } catch (e: Exception) {
                            null
                        }

                        if (nameText.isBlank()) {
                            errorMsg = "Account name cannot be blank"
                        } else if (parsed == null || parsed < BigDecimal.ZERO) {
                            errorMsg = "Please enter a valid non-negative opening balance"
                        } else {
                            viewModel.createAccount(nameText, selectedType, parsed, includeInNetWorth)
                        }
                    }
                ) {
                    Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeAddAccountDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // Archive Confirmation Dialog
    if (state.accountToArchive != null) {
        val acc = state.accountToArchive!!
        AlertDialog(
            onDismissRequest = viewModel::dismissArchiveDialog,
            title = { Text("Archive ${acc.name}?") },
            text = {
                Text("Archived accounts are hidden from new transaction forms. Existing transactions and history linked to this account remain completely preserved.")
            },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmArchiveAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissArchiveDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AccountCard(
    account: AccountItemUi,
    onArchive: () -> Unit
) {
    val (icon, iconBg, iconColor) = when (account.type) {
        AccountType.BANK -> Triple(Icons.Rounded.AccountBalance, MoneyBoardColors.SalarySurface, MoneyBoardColors.Navy900)
        AccountType.CASH -> Triple(Icons.Rounded.Payments, MoneyBoardColors.SavingSurface, MoneyBoardColors.Green700)
        AccountType.WALLET -> Triple(Icons.Rounded.AccountBalanceWallet, MoneyBoardColors.SpendingSurface, MoneyBoardColors.Blue600)
        AccountType.CREDIT_CARD -> Triple(Icons.Rounded.CreditCard, MoneyBoardColors.SpendingSurface, MoneyBoardColors.Red600)
        AccountType.INVESTMENT -> Triple(Icons.Rounded.Savings, MoneyBoardColors.AnnualSurface, MoneyBoardColors.Purple700)
        AccountType.OTHER -> Triple(Icons.Rounded.MoreHoriz, MoneyBoardColors.SalarySurface, MoneyBoardColors.TextSecondary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = account.name,
                    modifier = Modifier.size(22.dp),
                    tint = iconColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = account.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "• Opening: ${account.openingBalanceText}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = account.currentBalanceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (account.isPositive) MaterialTheme.colorScheme.onSurface else MoneyBoardColors.Red600
                )
                IconButton(
                    onClick = onArchive,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = "Archive Account",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
