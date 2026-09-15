package com.premraj.moneyboard.core.data.mapper

import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.util.yearMonthFromStorageKey
import java.time.LocalDate

fun MonthlyIncomePlanEntity.toDomain(): MonthlyIncomePlan =
    MonthlyIncomePlan(
        id = id,
        month = yearMonthFromStorageKey(monthKey),
        name = name,
        plannedAmount = Money(plannedAmountMinor, currencyCode),
        expectedDate = expectedDateEpochDay?.let { LocalDate.ofEpochDay(it) },
        sortOrder = sortOrder,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis
    )

fun MonthlyCategoryPlanEntity.toDomain(): MonthlyCategoryPlan =
    MonthlyCategoryPlan(
        month = yearMonthFromStorageKey(monthKey),
        categoryId = categoryId,
        plannedAmount = Money(plannedAmountMinor, currencyCode),
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis
    )
