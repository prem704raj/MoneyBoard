package com.premraj.moneyboard.core.database.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPlanDao {

    @Query(
        """
        SELECT *
        FROM monthly_income_plans
        WHERE monthKey = :monthKey
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    fun observeIncomePlans(
        monthKey: Int
    ): Flow<List<MonthlyIncomePlanEntity>>

    @Query(
        """
        SELECT *
        FROM monthly_category_plans
        WHERE monthKey = :monthKey
        ORDER BY categoryId ASC
        """
    )
    fun observeCategoryPlans(
        monthKey: Int
    ): Flow<List<MonthlyCategoryPlanEntity>>

    @Query(
        """
        SELECT *
        FROM monthly_category_plans
        WHERE monthKey = COALESCE(
            (SELECT monthKey FROM monthly_category_plans WHERE monthKey = :monthKey LIMIT 1),
            (SELECT monthKey FROM monthly_category_plans WHERE monthKey <= :monthKey ORDER BY monthKey DESC LIMIT 1),
            (SELECT monthKey FROM monthly_category_plans ORDER BY ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:monthKey / 100) * 12 + (:monthKey % 100))) ASC LIMIT 1)
        )
        ORDER BY categoryId ASC
        """
    )
    fun observeCategoryPlansInherited(
        monthKey: Int
    ): Flow<List<MonthlyCategoryPlanEntity>>

    @Query(
        """
        SELECT *
        FROM monthly_income_plans
        WHERE monthKey = COALESCE(
            (SELECT monthKey FROM monthly_income_plans WHERE monthKey = :monthKey LIMIT 1),
            (SELECT monthKey FROM monthly_income_plans WHERE monthKey <= :monthKey ORDER BY monthKey DESC LIMIT 1),
            (SELECT monthKey FROM monthly_income_plans ORDER BY ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:monthKey / 100) * 12 + (:monthKey % 100))) ASC LIMIT 1)
        )
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    fun observeIncomePlansInherited(
        monthKey: Int
    ): Flow<List<MonthlyIncomePlanEntity>>

    @Query(
        """
        SELECT *
        FROM monthly_category_plans
        WHERE monthKey = COALESCE(
            (SELECT monthKey FROM monthly_category_plans WHERE monthKey = :monthKey LIMIT 1),
            (SELECT monthKey FROM monthly_category_plans WHERE monthKey <= :monthKey ORDER BY monthKey DESC LIMIT 1),
            (SELECT monthKey FROM monthly_category_plans ORDER BY ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:monthKey / 100) * 12 + (:monthKey % 100))) ASC LIMIT 1)
        )
        ORDER BY categoryId ASC
        """
    )
    suspend fun getCategoryPlansInherited(
        monthKey: Int
    ): List<MonthlyCategoryPlanEntity>

    @Query(
        """
        SELECT *
        FROM monthly_income_plans
        WHERE monthKey = COALESCE(
            (SELECT monthKey FROM monthly_income_plans WHERE monthKey = :monthKey LIMIT 1),
            (SELECT monthKey FROM monthly_income_plans WHERE monthKey <= :monthKey ORDER BY monthKey DESC LIMIT 1),
            (SELECT monthKey FROM monthly_income_plans ORDER BY ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:monthKey / 100) * 12 + (:monthKey % 100))) ASC LIMIT 1)
        )
        ORDER BY sortOrder ASC, name COLLATE NOCASE ASC
        """
    )
    suspend fun getIncomePlansInherited(
        monthKey: Int
    ): List<MonthlyIncomePlanEntity>

    @Query(
        """
        SELECT *
        FROM monthly_income_plans
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun getIncomePlan(
        id: String
    ): MonthlyIncomePlanEntity?

    @Query(
        """
        SELECT *
        FROM monthly_category_plans
        WHERE monthKey = :monthKey
          AND categoryId = :categoryId
        LIMIT 1
        """
    )
    suspend fun getCategoryPlan(
        monthKey: Int,
        categoryId: String
    ): MonthlyCategoryPlanEntity?

    @Upsert
    suspend fun upsertIncomePlan(
        entity: MonthlyIncomePlanEntity
    )

    @Upsert
    suspend fun upsertCategoryPlan(
        entity: MonthlyCategoryPlanEntity
    )

    @Query("DELETE FROM monthly_income_plans WHERE id = :id")
    suspend fun deleteIncomePlan(id: String): Int

    @Query(
        """
        DELETE FROM monthly_category_plans
        WHERE monthKey = :monthKey
          AND categoryId = :categoryId
        """
    )
    suspend fun deleteCategoryPlan(
        monthKey: Int,
        categoryId: String
    ): Int

    @Query(
        "SELECT COUNT(*) FROM monthly_income_plans WHERE monthKey = :monthKey"
    )
    suspend fun incomePlanCount(monthKey: Int): Long

    @Query(
        "SELECT COUNT(*) FROM monthly_category_plans WHERE monthKey = :monthKey"
    )
    suspend fun categoryPlanCount(monthKey: Int): Long

    @Query("SELECT COUNT(*) FROM monthly_category_plans")
    suspend fun totalCategoryPlanCount(): Long

    @Query("SELECT COUNT(*) FROM monthly_income_plans")
    suspend fun totalIncomePlanCount(): Long

    @Query("SELECT * FROM monthly_income_plans")
    suspend fun getAllIncomePlans(): List<MonthlyIncomePlanEntity>

    @Query("SELECT * FROM monthly_category_plans")
    suspend fun getAllCategoryPlans(): List<MonthlyCategoryPlanEntity>

    @Upsert
    suspend fun upsertIncomePlans(entities: List<MonthlyIncomePlanEntity>)

    @Upsert
    suspend fun upsertCategoryPlans(entities: List<MonthlyCategoryPlanEntity>)
}
