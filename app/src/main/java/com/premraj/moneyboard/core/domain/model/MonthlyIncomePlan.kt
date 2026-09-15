package com.premraj.moneyboard.core.domain.model

import java.time.LocalDate
import java.time.YearMonth

data class MonthlyIncomePlan(
    val id: String,
    val month: YearMonth,
    val name: String,
    val plannedAmount: Money,
    val expectedDate: LocalDate?,
    val sortOrder: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
) {
    init {
        require(name.isNotBlank())
        require(plannedAmount.amountMinor > 0L)
        require(
            expectedDate == null ||
                YearMonth.from(expectedDate) == month
        )
    }
}
