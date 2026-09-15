package com.premraj.moneyboard.core.data.mapper

import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    type = CategoryType.fromStorage(type),
    group = CategoryGroup.fromStorage(groupKey),
    sortOrder = sortOrder,
    systemCategory = systemCategory,
    archived = archived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    type = type.storageValue,
    groupKey = group.storageValue,
    sortOrder = sortOrder,
    systemCategory = systemCategory,
    archived = archived,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis
)
