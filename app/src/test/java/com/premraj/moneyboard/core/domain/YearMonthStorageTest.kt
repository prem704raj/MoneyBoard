package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.util.toStorageKey
import com.premraj.moneyboard.core.domain.util.yearMonthFromStorageKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.YearMonth

class YearMonthStorageTest {

    @Test
    fun toStorageKey_september2026() {
        assertEquals(202609, YearMonth.of(2026, 9).toStorageKey())
    }

    @Test
    fun toStorageKey_january2027() {
        assertEquals(202701, YearMonth.of(2027, 1).toStorageKey())
    }

    @Test
    fun toStorageKey_december2026() {
        assertEquals(202612, YearMonth.of(2026, 12).toStorageKey())
    }

    @Test
    fun roundTrip_allMonths() {
        for (m in 1..12) {
            val ym = YearMonth.of(2026, m)
            assertEquals(ym, yearMonthFromStorageKey(ym.toStorageKey()))
        }
    }

    @Test
    fun fromStorageKey_invalidMonth0_rejected() {
        assertThrows(IllegalArgumentException::class.java) {
            yearMonthFromStorageKey(202600)
        }
    }

    @Test
    fun fromStorageKey_invalidMonth13_rejected() {
        assertThrows(IllegalArgumentException::class.java) {
            yearMonthFromStorageKey(202613)
        }
    }

    @Test
    fun fromStorageKey_validEdges() {
        assertEquals(YearMonth.of(2026, 1), yearMonthFromStorageKey(202601))
        assertEquals(YearMonth.of(2026, 12), yearMonthFromStorageKey(202612))
    }
}
