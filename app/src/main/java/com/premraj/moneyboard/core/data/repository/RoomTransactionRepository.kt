package com.premraj.moneyboard.core.data.repository

import com.premraj.moneyboard.core.common.IdProvider
import com.premraj.moneyboard.core.common.TimeProvider
import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.data.mapper.toEntity
import com.premraj.moneyboard.core.database.dao.AccountDao
import com.premraj.moneyboard.core.database.dao.CategoryDao
import com.premraj.moneyboard.core.database.dao.TransactionDao
import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import com.premraj.moneyboard.core.domain.util.toEpochDayRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class RoomTransactionRepository(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val idProvider: IdProvider,
    private val timeProvider: TimeProvider
) : TransactionRepository {

    override fun observeTransactions(month: YearMonth): Flow<List<FinanceTransaction>> {
        val r = month.toEpochDayRange()
        return transactionDao.observeActiveBetween(r.startInclusive, r.endInclusive)
            .map { rows -> rows.map { it.toDomain() } }
    }

    override fun observeTotal(
        month: YearMonth,
        type: TransactionType,
        currencyCode: String
    ): Flow<Money> {
        val r = month.toEpochDayRange()
        return transactionDao.observeTotalForTypeBetween(
            r.startInclusive,
            r.endInclusive,
            type.storageValue,
            currencyCode
        ).map { Money(it, currencyCode) }
    }

    override fun observeCategoryTotals(
        month: YearMonth,
        type: TransactionType,
        currencyCode: String
    ): Flow<List<CategoryTotalRow>> {
        val r = month.toEpochDayRange()
        return transactionDao.observeCategoryTotalsBetween(
            r.startInclusive,
            r.endInclusive,
            type.storageValue,
            currencyCode
        )
    }

    override suspend fun getTransaction(transactionId: String): FinanceTransaction? =
        transactionDao.getActiveById(transactionId)?.toDomain()

    override suspend fun createTransaction(command: CreateTransactionCommand): FinanceTransaction {
        validateReferences(
            accountId = command.accountId,
            categoryId = command.categoryId,
            transactionType = command.type,
            currencyCode = command.amount.currencyCode
        )
        val now = timeProvider.nowEpochMillis()
        val entity = TransactionEntity(
            id = idProvider.newId(),
            accountId = command.accountId,
            categoryId = command.categoryId,
            type = command.type.storageValue,
            amountMinor = command.amount.amountMinor,
            currencyCode = command.amount.currencyCode,
            localDateEpochDay = command.accountingDate.toEpochDay(),
            occurredAtEpochMillis = command.occurredAt.toEpochMilli(),
            note = command.note?.trim()?.takeIf { it.isNotEmpty() },
            merchant = command.merchant?.trim()?.takeIf { it.isNotEmpty() },
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now,
            deletedAtEpochMillis = null
        )
        transactionDao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun updateTransaction(transaction: FinanceTransaction): FinanceTransaction {
        require(transaction.deletedAtEpochMillis == null) { "Deleted transactions cannot be edited" }
        validateReferences(
            transaction.accountId,
            transaction.categoryId,
            transaction.type,
            transaction.amount.currencyCode
        )
        val existing = transactionDao.getActiveById(transaction.id)
            ?: error("Transaction does not exist: ${transaction.id}")
        val updated = transaction.copy(
            createdAtEpochMillis = existing.createdAtEpochMillis,
            updatedAtEpochMillis = timeProvider.nowEpochMillis(),
            deletedAtEpochMillis = null
        )
        transactionDao.update(updated.toEntity())
        return updated
    }

    override suspend fun softDeleteTransaction(transactionId: String) {
        require(transactionDao.softDelete(transactionId, timeProvider.nowEpochMillis()) == 1) {
            "Transaction does not exist or is already deleted: $transactionId"
        }
    }

    private suspend fun validateReferences(
        accountId: String,
        categoryId: String,
        transactionType: TransactionType,
        currencyCode: String
    ) {
        val account = accountDao.getById(accountId)
            ?: error("Account does not exist: $accountId")
        require(!account.archived) { "Cannot use an archived account" }
        require(account.currencyCode == currencyCode) {
            "Transaction currency $currencyCode does not match account currency ${account.currencyCode}"
        }

        val category = categoryDao.getById(categoryId)
            ?: error("Category does not exist: $categoryId")
        require(!category.archived) { "Cannot use an archived category" }
        val categoryType = CategoryType.fromStorage(category.type)
        require(categoryType.accepts(transactionType)) {
            "Category ${category.name} cannot be used for ${transactionType.storageValue}"
        }
    }
}
