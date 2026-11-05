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
import java.time.LocalDate
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
            .observeIncomePlansInherited(month.toStorageKey())
            .map { list -> list.map { it.toDomain().withMonth(month) } }

    override fun observeCategoryPlans(
        month: YearMonth
    ): Flow<List<MonthlyCategoryPlan>> =
        monthlyPlanDao
            .observeCategoryPlansInherited(month.toStorageKey())
            .map { list -> list.map { it.toDomain().copy(month = month) } }

    override suspend fun setIncomePlan(
        command: SetIncomePlanCommand
    ): MonthlyIncomePlan {
        val now = timeProvider.nowEpochMillis()
        val monthKey = command.month.toStorageKey()

        // If this month does not yet have its own income plan rows, snapshot inherited plans first
        if (monthlyPlanDao.incomePlanCount(monthKey) == 0L) {
            val inherited = monthlyPlanDao.getIncomePlansInherited(monthKey)
            inherited.forEach { plan ->
                val adjustedExpectedDateEpochDay = plan.expectedDateEpochDay?.let { epochDay ->
                    val date = LocalDate.ofEpochDay(epochDay)
                    val dayOfMonth = date.dayOfMonth.coerceAtMost(command.month.lengthOfMonth())
                    command.month.atDay(dayOfMonth).toEpochDay()
                }
                monthlyPlanDao.upsertIncomePlan(
                    plan.copy(
                        id = idProvider.newId(),
                        monthKey = monthKey,
                        expectedDateEpochDay = adjustedExpectedDateEpochDay,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now
                    )
                )
            }
        }

        val id = command.id ?: idProvider.newId()
        val existing = monthlyPlanDao.getIncomePlan(id)

        val entity = MonthlyIncomePlanEntity(
            id = id,
            monthKey = monthKey,
            name = command.name.trim(),
            plannedAmountMinor = command.plannedAmount.amountMinor,
            currencyCode = command.plannedAmount.currencyCode,
            expectedDateEpochDay = command.expectedDate?.toEpochDay(),
            sortOrder = command.sortOrder,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now
        )

        monthlyPlanDao.upsertIncomePlan(entity)
        return entity.toDomain().withMonth(command.month)
    }

    override suspend fun deleteIncomePlan(id: String) {
        monthlyPlanDao.deleteIncomePlan(id)
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
        val now = timeProvider.nowEpochMillis()

        // If this month does not yet have its own independent category plans,
        // copy the inherited plan into this month so edits are preserved for this and future months
        if (monthlyPlanDao.categoryPlanCount(key) == 0L) {
            val inherited = monthlyPlanDao.getCategoryPlansInherited(key)
            inherited.forEach { plan ->
                monthlyPlanDao.upsertCategoryPlan(
                    plan.copy(
                        monthKey = key,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now
                    )
                )
            }
        }

        val existing = monthlyPlanDao.getCategoryPlan(key, command.categoryId)

        // Planned amount can be 0L (user manually sets planned expense to 0)
        val entity = MonthlyCategoryPlanEntity(
            monthKey = key,
            categoryId = command.categoryId,
            plannedAmountMinor = command.plannedAmount.amountMinor,
            currencyCode = command.plannedAmount.currencyCode,
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now
        )

        monthlyPlanDao.upsertCategoryPlan(entity)
        return entity.toDomain().copy(month = command.month)
    }

    override suspend fun deleteCategoryPlan(month: YearMonth, categoryId: String) {
        val key = month.toStorageKey()
        val now = timeProvider.nowEpochMillis()

        if (monthlyPlanDao.categoryPlanCount(key) == 0L) {
            val inherited = monthlyPlanDao.getCategoryPlansInherited(key)
            inherited.forEach { plan ->
                monthlyPlanDao.upsertCategoryPlan(
                    plan.copy(
                        monthKey = key,
                        createdAtEpochMillis = now,
                        updatedAtEpochMillis = now
                    )
                )
            }
        }

        monthlyPlanDao.deleteCategoryPlan(key, categoryId)
    }

    override suspend fun hasAnyPlan(month: YearMonth): Boolean {
        val key = month.toStorageKey()
        return monthlyPlanDao.incomePlanCount(key) > 0L ||
            monthlyPlanDao.categoryPlanCount(key) > 0L ||
            monthlyPlanDao.totalCategoryPlanCount() > 0L
    }
}
