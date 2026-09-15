package com.premraj.moneyboard.core.data.mapper

import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.Money

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = AccountType.fromStorage(type),
    openingBalance = Money(openingBalanceMinor, currencyCode),
    includeInNetWorth = includeInNetWorth,
    archived = archived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type.storageValue,
    openingBalanceMinor = openingBalance.amountMinor,
    currencyCode = openingBalance.currencyCode,
    includeInNetWorth = includeInNetWorth,
    archived = archived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)
