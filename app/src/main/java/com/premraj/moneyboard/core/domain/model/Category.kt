package com.premraj.moneyboard.core.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconKey: String,
    val type: CategoryType,
    val group: CategoryGroup,
    val sortOrder: Int,
    val systemCategory: Boolean,
    val archived: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
