package com.premraj.moneyboard.core.data.repository

import com.premraj.moneyboard.core.common.IdProvider
import com.premraj.moneyboard.core.common.TimeProvider
import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.database.dao.CategoryDao
import com.premraj.moneyboard.core.database.dao.MonthlyPlanDao
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import com.premraj.moneyboard.core.domain.util.toStorageKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class RoomMonthlyPlanRepository(
    private val monthlyPlanDao: MonthlyPlanDao,
    private val categoryDao: CategoryDao,
    private val idProvider: IdProvider,
    private val timeProvider: TimeProvider
) : MonthlyPlanRepository {

    override fun observeIncomePlans(
        month: YearMonth
    ): Flow<List<MonthlyIncomePlan>> =
        monthlyPlanDao
            .observeIncomePlans(month.toStorageKey())
            .map { list -> list.map { it.toDomain() } }

    override fun observeCategoryPlans(
        month: YearMonth
    ): Flow<List<MonthlyCategoryPlan>> =
        monthlyPlanDao
            .observeCategoryPlans(month.toStorageKey())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun setIncomePlan(
        command: SetIncomePlanCommand
    ): MonthlyIncomePlan {
        val now = timeProvider.nowEpochMillis()
        val id = command.id ?: idProvider.newId()
        val existing = monthlyPlanDao.getIncomePlan(id)

        val entity = MonthlyIncomePlanEntity(
            id = id,
            monthKey = command.month.toStorageKey(),
            name = command.name.trim(),
            plannedAmountMinor = command.plannedAmount.amountMinor,
            currencyCode = command.plannedAmount.currencyCode,
            expectedDateEpochDay = command.expectedDate?.toEpochDay(),
            sortOrder = command.sortOrder,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now
        )

        monthlyPlanDao.upsertIncomePlan(entity)
        return entity.toDomain()
    }

    override suspend fun deleteIncomePlan(id: String) {
        require(monthlyPlanDao.deleteIncomePlan(id) == 1) {
            "Income plan does not exist: $id"
        }
    }

    override suspend fun setCategoryPlan(
        command: SetCategoryPlanCommand
    ): MonthlyCategoryPlan? {
        val category = categoryDao.getById(command.categoryId)
            ?: error("Category does not exist: ${command.categoryId}")

        require(!category.archived) {
            "Cannot plan against an archived category"
        }

        val type = CategoryType.fromStorage(category.type)

        require(
            type == CategoryType.EXPENSE ||
                type == CategoryType.SAVING ||
                type == CategoryType.INVESTMENT
        ) {
            "Monthly category plan supports EXPENSE, SAVING, INVESTMENT only"
        }

        val key = command.month.toStorageKey()

        if (command.plannedAmount.amountMinor == 0L) {
            monthlyPlanDao.deleteCategoryPlan(key, command.categoryId)
            return null
        }

        val now = timeProvider.nowEpochMillis()
        val existing = monthlyPlanDao.getCategoryPlan(key, command.categoryId)

        val entity = MonthlyCategoryPlanEntity(
            monthKey = key,
            categoryId = command.categoryId,
            plannedAmountMinor = command.plannedAmount.amountMinor,
            currencyCode = command.plannedAmount.currencyCode,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now
        )

        monthlyPlanDao.upsertCategoryPlan(entity)
        return entity.toDomain()
    }

    override suspend fun hasAnyPlan(month: YearMonth): Boolean {
        val key = month.toStorageKey()

        return monthlyPlanDao.incomePlanCount(key) > 0L ||
            monthlyPlanDao.categoryPlanCount(key) > 0L
    }
}
