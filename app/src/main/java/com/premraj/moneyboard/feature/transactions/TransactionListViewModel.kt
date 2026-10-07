package com.premraj.moneyboard.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.AccountRepository
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TransactionListViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val defaultCurrencyCode: String = "INR",
    private val locale: Locale = Locale.forLanguageTag("en-IN")
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    private val selectedAccountId = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")
    private val isAddEditSheetOpen = MutableStateFlow(false)
    private val editingTransaction = MutableStateFlow<FinanceTransaction?>(null)
    private val lastDeletedTransactionId = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val transactionsFlow = selectedMonth.flatMapLatest { month ->
        transactionRepository.observeTransactions(month)
    }

    private val filtersFlow = combine(
        selectedMonth,
        selectedTypeFilter,
        selectedAccountId,
        searchQuery
    ) { month, type, account, query ->
        FilterState(month, type, account, query)
    }

    private val sheetFlow = combine(
        isAddEditSheetOpen,
        editingTransaction,
        lastDeletedTransactionId,
        errorMessage
    ) { sheetOpen, editing, deletedId, error ->
        SheetState(sheetOpen, editing, deletedId, error)
    }

    private val dataFlow = combine(
        transactionsFlow,
        accountRepository.observeActiveAccounts(),
        categoryRepository.observeActiveCategories()
    ) { txs, accounts, categories ->
        DataState(txs, accounts, categories)
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        filtersFlow,
        dataFlow,
        sheetFlow
    ) { filters, data, sheet ->
        val accountMap = data.accounts.associateBy(Account::id)
        val categoryMap = data.categories.associateBy(Category::id)

        // Filter transactions
        val filtered = data.txList.filter { tx ->
            val matchesType = filters.typeFilter == null || tx.type == filters.typeFilter
            val matchesAccount = filters.accountId == null || tx.accountId == filters.accountId
            val query = filters.query.trim().lowercase()
            val matchesQuery = query.isBlank() ||
                (tx.note?.lowercase()?.contains(query) == true) ||
                (tx.merchant?.lowercase()?.contains(query) == true) ||
                (categoryMap[tx.categoryId]?.name?.lowercase()?.contains(query) == true) ||
                (accountMap[tx.accountId]?.name?.lowercase()?.contains(query) == true)

            matchesType && matchesAccount && matchesQuery
        }

        // Summary calculations
        var incomeMinor = 0L
        var expenseMinor = 0L
        var savedInvestedMinor = 0L

        data.txList.forEach { tx ->
            if (tx.amount.currencyCode == defaultCurrencyCode) {
                when (tx.type) {
                    TransactionType.INCOME -> incomeMinor += tx.amount.amountMinor
                    TransactionType.EXPENSE -> expenseMinor += tx.amount.amountMinor
                    TransactionType.SAVING, TransactionType.INVESTMENT -> savedInvestedMinor += tx.amount.amountMinor
                    TransactionType.TRANSFER -> Unit
                }
            }
        }

        val netMinor = incomeMinor - expenseMinor - savedInvestedMinor
        val summary = TransactionSummaryUi(
            totalIncomeText = MoneyFormatter.formatCompact(Money(incomeMinor, defaultCurrencyCode), locale),
            totalExpenseText = MoneyFormatter.formatCompact(Money(expenseMinor, defaultCurrencyCode), locale),
            totalSavedInvestedText = MoneyFormatter.formatCompact(Money(savedInvestedMinor, defaultCurrencyCode), locale),
            netCashflowText = MoneyFormatter.formatCompact(Money(netMinor, defaultCurrencyCode), locale),
            isNetPositive = netMinor >= 0
        )

        // Map to UI items and group by date
        val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
        val items = filtered.map { tx ->
            val cat = categoryMap[tx.categoryId]
            val acc = accountMap[tx.accountId]
            val catName = cat?.name ?: "Unknown"
            val iconKey = cat?.iconKey ?: "other"
            val accName = acc?.name ?: "Account"

            TransactionItemUi(
                id = tx.id,
                raw = tx,
                categoryName = catName,
                categoryIconKey = iconKey,
                accountName = accName,
                type = tx.type,
                amountText = MoneyFormatter.format(tx.amount, locale),
                date = tx.accountingDate,
                formattedDate = tx.accountingDate.format(dateFormatter),
                note = tx.note,
                merchant = tx.merchant
            )
        }

        val headerFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM", locale)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val groups = items.groupBy { it.date }
            .toList()
            .sortedByDescending { it.first }
            .map { (date, dateItems) ->
                val header = when (date) {
                    today -> "Today, ${date.format(DateTimeFormatter.ofPattern("dd MMM", locale))}"
                    yesterday -> "Yesterday, ${date.format(DateTimeFormatter.ofPattern("dd MMM", locale))}"
                    else -> date.format(headerFormatter)
                }
                TransactionGroupUi(
                    date = date,
                    dateHeader = header,
                    items = dateItems
                )
            }

        TransactionListUiState(
            isLoading = false,
            selectedMonth = filters.month,
            selectedTypeFilter = filters.typeFilter,
            selectedAccountId = filters.accountId,
            searchQuery = filters.query,
            groups = groups,
            summary = summary,
            accounts = data.accounts,
            categories = data.categories,
            isAddEditSheetOpen = sheet.isSheetOpen,
            editingTransaction = sheet.editingTx,
            lastDeletedTransactionId = sheet.lastDeletedId,
            errorMessage = sheet.errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TransactionListUiState()
    )

    fun selectMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun setTypeFilter(type: TransactionType?) {
        selectedTypeFilter.value = type
    }

    fun setAccountFilter(accountId: String?) {
        selectedAccountId.value = accountId
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun openCreateSheet() {
        editingTransaction.value = null
        isAddEditSheetOpen.value = true
    }

    fun openEditSheet(transaction: FinanceTransaction) {
        editingTransaction.value = transaction
        isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        isAddEditSheetOpen.value = false
        editingTransaction.value = null
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun saveTransaction(
        accountId: String,
        categoryId: String,
        type: TransactionType,
        amountMajor: BigDecimal,
        date: LocalDate,
        note: String?,
        merchant: String?
    ) {
        viewModelScope.launch {
            try {
                val currentAccounts = accountRepository.getAccount(accountId)
                val currency = currentAccounts?.openingBalance?.currencyCode ?: defaultCurrencyCode
                val money = Money.fromMajor(amountMajor, currency)
                val cleanNote = note?.trim()?.ifBlank { null }
                val cleanMerchant = merchant?.trim()?.ifBlank { null }

                val currentEditing = editingTransaction.value
                if (currentEditing == null) {
                    // Create
                    transactionRepository.createTransaction(
                        CreateTransactionCommand(
                            accountId = accountId,
                            categoryId = categoryId,
                            type = type,
                            amount = money,
                            accountingDate = date,
                            occurredAt = Instant.now(),
                            note = cleanNote,
                            merchant = cleanMerchant
                        )
                    )
                } else {
                    // Update
                    transactionRepository.updateTransaction(
                        currentEditing.copy(
                            accountId = accountId,
                            categoryId = categoryId,
                            type = type,
                            amount = money,
                            accountingDate = date,
                            note = cleanNote,
                            merchant = cleanMerchant
                        )
                    )
                }

                // If transaction date is in a different month, navigate to that month
                val txMonth = YearMonth.from(date)
                if (txMonth != selectedMonth.value) {
                    selectedMonth.value = txMonth
                }

                closeAddEditSheet()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to save transaction"
            }
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            try {
                transactionRepository.softDeleteTransaction(id)
                lastDeletedTransactionId.value = id
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to delete transaction"
            }
        }
    }

    fun undoDelete() {
        val idToRestore = lastDeletedTransactionId.value ?: return
        viewModelScope.launch {
            try {
                transactionRepository.restoreTransaction(idToRestore)
                lastDeletedTransactionId.value = null
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to restore transaction"
            }
        }
    }

    private data class FilterState(
        val month: YearMonth,
        val typeFilter: TransactionType?,
        val accountId: String?,
        val query: String
    )

    private data class SheetState(
        val isSheetOpen: Boolean,
        val editingTx: FinanceTransaction?,
        val lastDeletedId: String?,
        val errorMsg: String?
    )

    private data class DataState(
        val txList: List<FinanceTransaction>,
        val accounts: List<Account>,
        val categories: List<Category>
    )

    companion object {
        fun factory(
            transactionRepository: TransactionRepository,
            accountRepository: AccountRepository,
            categoryRepository: CategoryRepository,
            defaultCurrencyCode: String = "INR"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransactionListViewModel(
                    transactionRepository = transactionRepository,
                    accountRepository = accountRepository,
                    categoryRepository = categoryRepository,
                    defaultCurrencyCode = defaultCurrencyCode
                ) as T
            }
        }
    }
}
