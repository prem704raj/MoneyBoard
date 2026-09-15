package com.premraj.moneyboard.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY type ASC, sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<CategoryEntity>>

    @Query(
        """
        SELECT *
        FROM categories
        ORDER BY type ASC, sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type AND archived = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeActiveByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countAll(): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET archived = 1, updatedAtEpochMillis = :updatedAt WHERE id = :id AND systemCategory = 0")
    suspend fun archiveCustomCategory(id: String, updatedAt: Long): Int
}
