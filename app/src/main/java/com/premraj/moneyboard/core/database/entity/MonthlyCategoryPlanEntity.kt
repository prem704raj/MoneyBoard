package com.premraj.moneyboard.core.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "monthly_category_plans",
    primaryKeys = ["monthKey", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["monthKey"])
    ]
)
data class MonthlyCategoryPlanEntity(
    val monthKey: Int,
    val categoryId: String,
    val plannedAmountMinor: Long,
    val currencyCode: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
