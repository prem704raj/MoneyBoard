package com.premraj.moneyboard.core.domain.repository

import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

data class CreateTransactionCommand(
    val accountId: String,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
    val accountingDate: LocalDate,
    val occurredAt: Instant,
    val note: String? = null,
    val merchant: String? = null
) {
    init {
        require(accountId.isNotBlank())
        require(categoryId.isNotBlank())
        require(amount.amountMinor > 0L) { "Transaction amount must be greater than zero" }
        require(note == null || note.length <= FinanceTransaction.MAX_NOTE_LENGTH)
        require(merchant == null || merchant.length <= FinanceTransaction.MAX_MERCHANT_LENGTH)
    }
}

interface TransactionRepository {
    fun observeTransactions(month: YearMonth): Flow<List<FinanceTransaction>>
    fun observeTotal(month: YearMonth, type: TransactionType, currencyCode: String): Flow<Money>
    fun observeCategoryTotals(month: YearMonth, type: TransactionType, currencyCode: String): Flow<List<CategoryTotalRow>>
    suspend fun getTransaction(transactionId: String): FinanceTransaction?
    suspend fun createTransaction(command: CreateTransactionCommand): FinanceTransaction
    suspend fun updateTransaction(transaction: FinanceTransaction): FinanceTransaction
    suspend fun softDeleteTransaction(transactionId: String)
}
