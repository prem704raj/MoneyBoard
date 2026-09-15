package com.premraj.moneyboard.core.data.repository

import com.premraj.moneyboard.core.common.IdProvider
import com.premraj.moneyboard.core.common.TimeProvider
import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.database.dao.CategoryDao
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.CreateCategoryCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val dao: CategoryDao,
    private val idProvider: IdProvider,
    private val timeProvider: TimeProvider
) : CategoryRepository {
    override fun observeActiveCategories(): Flow<List<Category>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeActiveCategories(type: CategoryType): Flow<List<Category>> =
        dao.observeActiveByType(type.storageValue).map { list -> list.map { it.toDomain() } }

    override fun observeAllCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(categoryId: String): Category? =
        dao.getById(categoryId)?.toDomain()

    override suspend fun createCategory(command: CreateCategoryCommand): Category {
        val now = timeProvider.nowEpochMillis()
        val entity = CategoryEntity(
            id = idProvider.newId(),
            name = command.name.trim(),
            iconKey = command.iconKey.trim(),
            type = command.type.storageValue,
            groupKey = command.group.storageValue,
            sortOrder = command.sortOrder,
            systemCategory = false,
            archived = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun archiveCustomCategory(categoryId: String) {
        require(dao.archiveCustomCategory(categoryId, timeProvider.nowEpochMillis()) == 1) {
            "Category is missing, already archived, or is a protected system category"
        }
    }
}
