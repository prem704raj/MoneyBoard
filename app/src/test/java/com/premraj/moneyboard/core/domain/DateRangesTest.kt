package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.util.toEpochDayRange
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateRangesTest {
    @Test
    fun leapFebruary_has29Days() {
        val range = YearMonth.of(2028, 2).toEpochDayRange()
        assertEquals(LocalDate.of(2028, 2, 1).toEpochDay(), range.startInclusive)
        assertEquals(LocalDate.of(2028, 2, 29).toEpochDay(), range.endInclusive)
    }

    @Test
    fun september2026_isCorrect() {
        val range = YearMonth.of(2026, 9).toEpochDayRange()
        assertEquals(LocalDate.of(2026, 9, 1).toEpochDay(), range.startInclusive)
        assertEquals(LocalDate.of(2026, 9, 30).toEpochDay(), range.endInclusive)
    }
}
