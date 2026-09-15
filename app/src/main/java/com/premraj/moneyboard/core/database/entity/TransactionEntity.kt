package com.premraj.moneyboard.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["categoryId"]),
        Index(value = ["type"]),
        Index(value = ["localDateEpochDay"]),
        Index(value = ["localDateEpochDay", "type", "deletedAtEpochMillis"]),
        Index(value = ["categoryId", "localDateEpochDay"]),
        Index(value = ["deletedAtEpochMillis"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val categoryId: String,
    val type: String,
    val amountMinor: Long,
    val currencyCode: String,
    val localDateEpochDay: Long,
    val occurredAtEpochMillis: Long,
    val note: String?,
    val merchant: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long?
)
