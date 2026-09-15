package com.premraj.moneyboard.core.domain.model

import java.time.YearMonth

data class MonthlyCategoryPlan(
    val month: YearMonth,
    val categoryId: String,
    val plannedAmount: Money,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
) {
    init {
        require(categoryId.isNotBlank())
        require(plannedAmount.amountMinor > 0L)
    }
}
