package com.premraj.moneyboard.core.domain.repository

import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

data class CreateCategoryCommand(
    val name: String,
    val iconKey: String,
    val type: CategoryType,
    val group: CategoryGroup,
    val sortOrder: Int = 10_000
) {
    init {
        require(name.isNotBlank()) { "Category name cannot be blank" }
        require(name.length <= 60) { "Category name cannot exceed 60 characters" }
        require(iconKey.isNotBlank()) { "iconKey cannot be blank" }
    }
}

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<Category>>
    fun observeActiveCategories(type: CategoryType): Flow<List<Category>>
    fun observeAllCategories(): Flow<List<Category>>
    suspend fun getCategory(categoryId: String): Category?
    suspend fun createCategory(command: CreateCategoryCommand): Category
    suspend fun archiveCustomCategory(categoryId: String)
}
