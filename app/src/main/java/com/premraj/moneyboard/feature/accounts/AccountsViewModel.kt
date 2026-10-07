package com.premraj.moneyboard.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.AccountRepository
import com.premraj.moneyboard.core.domain.repository.CreateAccountCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

class AccountsViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val defaultCurrencyCode: String = "INR",
    private val locale: Locale = Locale.forLanguageTag("en-IN")
) : ViewModel() {

    private val isAddAccountDialogOpen = MutableStateFlow(false)
    private val accountToArchive = MutableStateFlow<AccountItemUi?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AccountsUiState> = combine(
        accountRepository.observeActiveAccounts(),
        transactionRepository.observeAllTransactions(),
        combine(isAddAccountDialogOpen, accountToArchive, errorMessage) { addOpen, archiveTarget, error ->
            Triple(addOpen, archiveTarget, error)
        }
    ) { accounts, allTransactions, dialogState ->
        val (isAddOpen, targetToArchive, errorMsg) = dialogState

        // Group transactions by accountId
        val txByAccount = allTransactions.groupBy(FinanceTransaction::accountId)

        var totalNetWorthMinor = 0L
        var totalCashBankMinor = 0L
        var totalInvestmentsMinor = 0L
        var totalLiabilitiesMinor = 0L

        val accountItems = accounts.map { acc ->
            val accountTxs = txByAccount[acc.id].orEmpty()
            var currentBalanceMinor = acc.openingBalance.amountMinor

            accountTxs.forEach { tx ->
                when (tx.type) {
                    TransactionType.INCOME -> currentBalanceMinor += tx.amount.amountMinor
                    TransactionType.EXPENSE,
                    TransactionType.SAVING,
                    TransactionType.INVESTMENT -> currentBalanceMinor -= tx.amount.amountMinor
                    TransactionType.TRANSFER -> Unit
                }
            }

            val accCurrency = acc.openingBalance.currencyCode
            if (accCurrency == defaultCurrencyCode) {
                if (acc.includeInNetWorth) {
                    totalNetWorthMinor += currentBalanceMinor
                }
                when (acc.type) {
                    AccountType.CASH, AccountType.BANK, AccountType.WALLET ->
                        totalCashBankMinor += currentBalanceMinor
                    AccountType.INVESTMENT ->
                        totalInvestmentsMinor += currentBalanceMinor
                    AccountType.CREDIT_CARD ->
                        totalLiabilitiesMinor += currentBalanceMinor
                    AccountType.OTHER -> Unit
                }
            }

            val currentMoney = Money(currentBalanceMinor, accCurrency)
            AccountItemUi(
                id = acc.id,
                name = acc.name,
                type = acc.type,
                openingBalanceText = MoneyFormatter.format(acc.openingBalance, locale),
                currentBalanceText = MoneyFormatter.format(currentMoney, locale),
                currentBalanceMinor = currentBalanceMinor,
                currencyCode = accCurrency,
                includeInNetWorth = acc.includeInNetWorth,
                isPositive = currentBalanceMinor >= 0,
                raw = acc
            )
        }.sortedWith(compareBy<AccountItemUi> { it.type.name }.thenBy { it.name })

        val summary = AccountsSummaryUi(
            netWorthText = MoneyFormatter.format(Money(totalNetWorthMinor, defaultCurrencyCode), locale),
            totalCashAndBankText = MoneyFormatter.format(Money(totalCashBankMinor, defaultCurrencyCode), locale),
            totalInvestmentsText = MoneyFormatter.format(Money(totalInvestmentsMinor, defaultCurrencyCode), locale),
            totalLiabilitiesText = MoneyFormatter.format(Money(totalLiabilitiesMinor, defaultCurrencyCode), locale),
            isNetWorthPositive = totalNetWorthMinor >= 0
        )

        AccountsUiState(
            isLoading = false,
            accounts = accountItems,
            summary = summary,
            isAddAccountDialogOpen = isAddOpen,
            accountToArchive = targetToArchive,
            errorMessage = errorMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AccountsUiState()
    )

    fun openAddAccountDialog() {
        isAddAccountDialogOpen.value = true
    }

    fun closeAddAccountDialog() {
        isAddAccountDialogOpen.value = false
    }

    fun promptArchiveAccount(account: AccountItemUi) {
        accountToArchive.value = account
    }

    fun dismissArchiveDialog() {
        accountToArchive.value = null
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun createAccount(
        name: String,
        type: AccountType,
        openingBalanceMajor: BigDecimal,
        includeInNetWorth: Boolean
    ) {
        viewModelScope.launch {
            try {
                val money = Money.fromMajor(openingBalanceMajor, defaultCurrencyCode)
                accountRepository.createAccount(
                    CreateAccountCommand(
                        name = name.trim(),
                        type = type,
                        openingBalance = money,
                        includeInNetWorth = includeInNetWorth
                    )
                )
                closeAddAccountDialog()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to create account"
            }
        }
    }

    fun confirmArchiveAccount() {
        val target = accountToArchive.value ?: return
        viewModelScope.launch {
            try {
                accountRepository.archiveAccount(target.id)
                dismissArchiveDialog()
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Failed to archive account"
            }
        }
    }

    companion object {
        fun factory(
            accountRepository: AccountRepository,
            transactionRepository: TransactionRepository,
            defaultCurrencyCode: String = "INR"
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AccountsViewModel(
                    accountRepository = accountRepository,
                    transactionRepository = transactionRepository,
                    defaultCurrencyCode = defaultCurrencyCode
                ) as T
            }
        }
    }
}
