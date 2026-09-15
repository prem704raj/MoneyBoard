package com.premraj.moneyboard.core.database.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type", "archived"]),
        Index(value = ["groupKey"]),
        Index(value = ["sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val type: String,
    val groupKey: String,
    val sortOrder: Int,
    val systemCategory: Boolean,
    val archived: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
