package com.premraj.moneyboard.feature.transactions

import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import java.time.LocalDate
import java.time.YearMonth

data class TransactionItemUi(
    val id: String,
    val raw: FinanceTransaction,
    val categoryName: String,
    val categoryIconKey: String,
    val accountName: String,
    val type: TransactionType,
    val amountText: String,
    val date: LocalDate,
    val formattedDate: String,
    val note: String?,
    val merchant: String?
)

data class TransactionGroupUi(
    val date: LocalDate,
    val dateHeader: String,
    val items: List<TransactionItemUi>
)

data class TransactionSummaryUi(
    val totalIncomeText: String,
    val totalExpenseText: String,
    val totalSavedInvestedText: String,
    val netCashflowText: String,
    val isNetPositive: Boolean
)

data class TransactionListUiState(
    val isLoading: Boolean = true,
    val selectedMonth: YearMonth = YearMonth.now(),
    val selectedTypeFilter: TransactionType? = null,
    val selectedAccountId: String? = null,
    val searchQuery: String = "",
    val groups: List<TransactionGroupUi> = emptyList(),
    val summary: TransactionSummaryUi = TransactionSummaryUi("₹0", "₹0", "₹0", "₹0", true),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isAddEditSheetOpen: Boolean = false,
    val editingTransaction: FinanceTransaction? = null,
    val lastDeletedTransactionId: String? = null,
    val errorMessage: String? = null
)
