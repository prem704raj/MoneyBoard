package com.premraj.moneyboard.feature.accounts

import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.Money

data class AccountItemUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val openingBalanceText: String,
    val currentBalanceText: String,
    val currentBalanceMinor: Long,
    val currencyCode: String,
    val includeInNetWorth: Boolean,
    val isPositive: Boolean,
    val raw: Account
)

data class AccountsSummaryUi(
    val netWorthText: String,
    val totalCashAndBankText: String,
    val totalInvestmentsText: String,
    val totalLiabilitiesText: String,
    val isNetWorthPositive: Boolean
)

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountItemUi> = emptyList(),
    val summary: AccountsSummaryUi = AccountsSummaryUi("₹0", "₹0", "₹0", "₹0", true),
    val isAddAccountDialogOpen: Boolean = false,
    val accountToArchive: AccountItemUi? = null,
    val errorMessage: String? = null
)
