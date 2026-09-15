package com.premraj.moneyboard.core.domain.util

import java.time.LocalDate
import java.time.YearMonth

data class EpochDayRange(
    val startInclusive: Long,
    val endInclusive: Long
) {
    init {
        require(startInclusive <= endInclusive)
    }
}

fun YearMonth.toEpochDayRange(): EpochDayRange = EpochDayRange(
    startInclusive = atDay(1).toEpochDay(),
    endInclusive = atEndOfMonth().toEpochDay()
)

fun closedDateRange(start: LocalDate, endInclusive: LocalDate): EpochDayRange {
    require(!endInclusive.isBefore(start)) { "endInclusive must not be before start" }
    return EpochDayRange(start.toEpochDay(), endInclusive.toEpochDay())
}
