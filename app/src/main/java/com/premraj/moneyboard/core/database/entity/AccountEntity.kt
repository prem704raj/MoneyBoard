package com.premraj.moneyboard.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["archived"]),
        Index(value = ["type"])
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val openingBalanceMinor: Long,
    val currencyCode: String,
    val includeInNetWorth: Boolean,
    val archived: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
