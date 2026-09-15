package com.premraj.moneyboard.core.data.mapper

import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate

fun TransactionEntity.toDomain() = FinanceTransaction(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    type = TransactionType.fromStorage(type),
    amount = Money(amountMinor, currencyCode),
    accountingDate = LocalDate.ofEpochDay(localDateEpochDay),
    occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
    note = note,
    merchant = merchant,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deletedAtEpochMillis = deletedAtEpochMillis
)

fun FinanceTransaction.toEntity() = TransactionEntity(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    type = type.storageValue,
    amountMinor = amount.amountMinor,
    currencyCode = amount.currencyCode,
    localDateEpochDay = accountingDate.toEpochDay(),
    occurredAtEpochMillis = occurredAt.toEpochMilli(),
    note = note,
    merchant = merchant,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    deletedAtEpochMillis = deletedAtEpochMillis
)
