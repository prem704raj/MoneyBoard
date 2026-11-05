package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.util.toStorageKey
import com.premraj.moneyboard.core.domain.util.yearMonthFromStorageKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Empirical Adversarial Challenger Test Suite for Milestone 5:
 * DAO Queries, Repository Mappings, Date Clamping & ₹0 Support.
 */
class Milestone5DaoAndRepoChallengerTest {

    // =========================================================================
    // 1. SQLITE INTEGER DIVISION & MONTH DISTANCE MATH
    // =========================================================================

    /**
     * Emulates SQLite integer division and modulo math:
     * ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:monthKey / 100) * 12 + (:monthKey % 100)))
     */
    private fun sqliteMonthDistance(key1: Int, key2: Int): Int {
        val m1 = (key1 / 100) * 12 + (key1 % 100)
        val m2 = (key2 / 100) * 12 + (key2 % 100)
        return abs(m1 - m2)
    }

    @Test
    fun sqliteMonthDistance_matchesCalendarDistance_acrossYearBoundaries() {
        // Test 202612 vs 202701 explicitly
        val dec2026 = YearMonth.of(2026, 12).toStorageKey() // 202612
        val jan2027 = YearMonth.of(2027, 1).toStorageKey()  // 202701
        assertEquals(1, sqliteMonthDistance(dec2026, jan2027))

        // Test 202312 vs 202401
        val dec2023 = YearMonth.of(2023, 12).toStorageKey()
        val jan2024 = YearMonth.of(2024, 1).toStorageKey()
        assertEquals(1, sqliteMonthDistance(dec2023, jan2024))

        // Exhaustive check: every month pair between 2020 and 2035 (32,400 pairs)
        for (year1 in 2020..2035) {
            for (month1 in 1..12) {
                val ym1 = YearMonth.of(year1, month1)
                val key1 = ym1.toStorageKey()
                for (year2 in 2020..2035) {
                    for (month2 in 1..12) {
                        val ym2 = YearMonth.of(year2, month2)
                        val key2 = ym2.toStorageKey()

                        val expectedDistance = abs(ChronoUnit.MONTHS.between(ym1, ym2)).toInt()
                        val calculatedDistance = sqliteMonthDistance(key1, key2)

                        assertEquals(
                            "Distance mismatch between $ym1 and $ym2",
                            expectedDistance,
                            calculatedDistance
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // 2. 3-TIER COALESCE QUERY SIMULATION & RESOLUTION LOGIC
    // =========================================================================

    private fun resolveInheritedMonthKey(
        targetKey: Int,
        existingMonthKeys: List<Int>
    ): Int? {
        // Tier 1: Exact match
        val tier1 = existingMonthKeys.firstOrNull { it == targetKey }
        if (tier1 != null) return tier1

        // Tier 2: Latest plan <= targetKey
        val tier2 = existingMonthKeys
            .filter { it <= targetKey }
            .maxOrNull()
        if (tier2 != null) return tier2

        // Tier 3: Closest calendar distance
        val tier3 = existingMonthKeys
            .minByOrNull { sqliteMonthDistance(it, targetKey) }
        return tier3
    }

    @Test
    fun resolveInheritedMonthKey_emptyTable_returnsNullGracefully() {
        val result = resolveInheritedMonthKey(202610, emptyList())
        assertNull(result)
    }

    @Test
    fun resolveInheritedMonthKey_exactMatch_resolvesExact() {
        val keys = listOf(202610)
        val result = resolveInheritedMonthKey(202610, keys)
        assertEquals(202610, result)
    }

    @Test
    fun resolveInheritedMonthKey_futureMonth_inheritsLatestPlan() {
        val keys = listOf(202610)
        // 1 month into future
        assertEquals(202610, resolveInheritedMonthKey(202611, keys))
        // 6 months into future across year boundary
        assertEquals(202610, resolveInheritedMonthKey(202704, keys))
    }

    @Test
    fun resolveInheritedMonthKey_pastMonthBeforeAllPlans_inheritsClosestPlan() {
        val keys = listOf(202610)
        // 1 month into past
        assertEquals(202610, resolveInheritedMonthKey(202609, keys))
        // Prior year
        assertEquals(202610, resolveInheritedMonthKey(202512, keys))
    }

    @Test
    fun resolveInheritedMonthKey_multiplePlans_interleavedCorrectly() {
        val keys = listOf(202604, 202608, 202702)

        // Between 202604 and 202608: inherits 202604 (latest <= target)
        assertEquals(202604, resolveInheritedMonthKey(202605, keys))
        assertEquals(202604, resolveInheritedMonthKey(202607, keys))

        // Exactly 202608
        assertEquals(202608, resolveInheritedMonthKey(202608, keys))

        // Between 202608 and 202702 across year boundary: inherits 202608
        assertEquals(202608, resolveInheritedMonthKey(202612, keys))
        assertEquals(202608, resolveInheritedMonthKey(202701, keys))

        // Before earliest (202604): inherits closest (202604)
        assertEquals(202604, resolveInheritedMonthKey(202601, keys))
        assertEquals(202604, resolveInheritedMonthKey(202512, keys))

        // After latest (202702): inherits latest (202702)
        assertEquals(202702, resolveInheritedMonthKey(202703, keys))
    }

    // =========================================================================
    // 3. ZERO-AMOUNT PLAN SUPPORT (MonthlyCategoryPlan & MonthlyIncomePlan)
    // =========================================================================

    @Test
    fun monthlyCategoryPlan_zeroAmount_isPermitted() {
        val plan = MonthlyCategoryPlan(
            month = YearMonth.of(2026, 10),
            categoryId = "cat-rent",
            plannedAmount = Money(0L, "INR"),
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        assertEquals(0L, plan.plannedAmount.amountMinor)
    }

    @Test
    fun monthlyCategoryPlan_negativeAmount_throwsIllegalArgument() {
        try {
            MonthlyCategoryPlan(
                month = YearMonth.of(2026, 10),
                categoryId = "cat-rent",
                plannedAmount = Money(-1L, "INR"),
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
            fail("Expected IllegalArgumentException for negative planned amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Failed requirement") == true || e.message?.isNotEmpty() == true)
        }
    }

    @Test
    fun monthlyIncomePlan_zeroAmount_isPermitted() {
        val plan = MonthlyIncomePlan(
            id = "inc-1",
            month = YearMonth.of(2026, 10),
            name = "Freelance",
            plannedAmount = Money(0L, "INR"),
            expectedDate = null,
            sortOrder = 0,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        assertEquals(0L, plan.plannedAmount.amountMinor)
    }

    @Test
    fun monthlyIncomePlan_negativeAmount_throwsIllegalArgument() {
        try {
            MonthlyIncomePlan(
                id = "inc-1",
                month = YearMonth.of(2026, 10),
                name = "Salary",
                plannedAmount = Money(-500L, "INR"),
                expectedDate = null,
                sortOrder = 0,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
            fail("Expected IllegalArgumentException for negative planned amount")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message?.contains("Failed requirement") == true || e.message?.isNotEmpty() == true)
        }
    }

    // =========================================================================
    // 4. DATE CLAMPING & PROJECTION (withMonth and Room snapshotting)
    // =========================================================================

    @Test
    fun withMonth_clampsDayToTargetMonthLength_leapYearsAndOddMonths() {
        val basePlan = MonthlyIncomePlan(
            id = "inc-1",
            month = YearMonth.of(2024, 1),
            name = "Salary",
            plannedAmount = Money(5000000L, "INR"),
            expectedDate = LocalDate.of(2024, 1, 31),
            sortOrder = 0,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        // Leap year: Feb 2024 has 29 days
        val feb2024 = basePlan.withMonth(YearMonth.of(2024, 2))
        assertEquals(LocalDate.of(2024, 2, 29), feb2024.expectedDate)
        assertEquals(YearMonth.of(2024, 2), feb2024.month)

        // Non-leap year: Feb 2025 has 28 days
        val feb2025 = basePlan.withMonth(YearMonth.of(2025, 2))
        assertEquals(LocalDate.of(2025, 2, 28), feb2025.expectedDate)
        assertEquals(YearMonth.of(2025, 2), feb2025.month)

        // 30-day month: April 2026
        val apr2026 = basePlan.withMonth(YearMonth.of(2026, 4))
        assertEquals(LocalDate.of(2026, 4, 30), apr2026.expectedDate)

        // Non-clamping day: 15th
        val midMonthPlan = basePlan.copy(expectedDate = LocalDate.of(2024, 1, 15))
        val febMidMonth = midMonthPlan.withMonth(YearMonth.of(2024, 2))
        assertEquals(LocalDate.of(2024, 2, 15), febMidMonth.expectedDate)

        // Null expectedDate stays null
        val nullDatePlan = basePlan.copy(expectedDate = null)
        val febNullDate = nullDatePlan.withMonth(YearMonth.of(2024, 2))
        assertNull(febNullDate.expectedDate)
    }

    // =========================================================================
    // 5. MAPPER DEFENSIVE CLAMPING (MonthlyPlanMapper)
    // =========================================================================

    @Test
    fun monthlyIncomePlanMapper_defensivelyClampsCorruptedLegacyDates() {
        // Legacy corrupted row: monthKey is 202602, but epoch day is Jan 31
        val corruptedEntity = MonthlyIncomePlanEntity(
            id = "legacy-1",
            monthKey = 202602,
            name = "Salary",
            plannedAmountMinor = 5000000L,
            currencyCode = "INR",
            expectedDateEpochDay = LocalDate.of(2026, 1, 31).toEpochDay(),
            sortOrder = 0,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        // Does not throw IllegalArgumentException, and clamps to Feb 28, 2026
        val domain = corruptedEntity.toDomain()
        assertEquals(YearMonth.of(2026, 2), domain.month)
        assertEquals(LocalDate.of(2026, 2, 28), domain.expectedDate)
    }

    @Test
    fun monthlyCategoryPlanMapper_mapsFieldsFaithfully() {
        val entity = MonthlyCategoryPlanEntity(
            monthKey = 202610,
            categoryId = "groceries",
            plannedAmountMinor = 1200000L,
            currencyCode = "INR",
            createdAtEpochMillis = 2000L,
            updatedAtEpochMillis = 3000L
        )
        val domain = entity.toDomain()
        assertEquals(YearMonth.of(2026, 10), domain.month)
        assertEquals("groceries", domain.categoryId)
        assertEquals(1200000L, domain.plannedAmount.amountMinor)
    }
}
