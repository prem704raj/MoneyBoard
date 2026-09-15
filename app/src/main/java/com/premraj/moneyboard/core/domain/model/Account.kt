package com.premraj.moneyboard.core.domain.model

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val openingBalance: Money,
    val includeInNetWorth: Boolean,
    val archived: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
