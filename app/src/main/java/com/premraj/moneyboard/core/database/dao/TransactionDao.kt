package com.premraj.moneyboard.core.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getByIdIncludingDeleted(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id AND deletedAtEpochMillis IS NULL LIMIT 1")
    suspend fun getActiveById(id: String): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions
        WHERE localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
          AND deletedAtEpochMillis IS NULL
        ORDER BY localDateEpochDay DESC, occurredAtEpochMillis DESC, createdAtEpochMillis DESC
        """
    )
    fun observeActiveBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0)
        FROM transactions
        WHERE localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
          AND type = :type
          AND currencyCode = :currencyCode
          AND deletedAtEpochMillis IS NULL
        """
    )
    fun observeTotalForTypeBetween(
        startEpochDay: Long,
        endEpochDay: Long,
        type: String,
        currencyCode: String
    ): Flow<Long>

    @Query(
        """
        SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS totalMinor
        FROM transactions
        WHERE localDateEpochDay BETWEEN :startEpochDay AND :endEpochDay
          AND type = :type
          AND currencyCode = :currencyCode
          AND deletedAtEpochMillis IS NULL
        GROUP BY categoryId
        ORDER BY totalMinor DESC
        """
    )
    fun observeCategoryTotalsBetween(
        startEpochDay: Long,
        endEpochDay: Long,
        type: String,
        currencyCode: String
    ): Flow<List<CategoryTotalRow>>

    @Query(
        """
        UPDATE transactions
        SET deletedAtEpochMillis = :deletedAt, updatedAtEpochMillis = :deletedAt
        WHERE id = :transactionId AND deletedAtEpochMillis IS NULL
        """
    )
    suspend fun softDelete(transactionId: String, deletedAt: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE deletedAtEpochMillis IS NULL")
    suspend fun countActive(): Long
}
