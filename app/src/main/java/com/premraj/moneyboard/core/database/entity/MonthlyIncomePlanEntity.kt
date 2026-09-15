package com.premraj.moneyboard.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "monthly_income_plans",
    indices = [
        Index(value = ["monthKey"]),
        Index(value = ["monthKey", "sortOrder"])
    ]
)
data class MonthlyIncomePlanEntity(
    @PrimaryKey val id: String,
    val monthKey: Int,
    val name: String,
    val plannedAmountMinor: Long,
    val currencyCode: String,
    val expectedDateEpochDay: Long?,
    val sortOrder: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
