package com.premraj.moneyboard.core.util

import com.premraj.moneyboard.testutil.HeaderSubtitleFormatterAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Requirement-driven test suite for HeaderSubtitleFormatter covering Tier 1 and Tier 2.
 *
 * Requirements:
 * - ORIGINAL_REQUEST.md (Requirement R2): Construct the header subtitle strictly in the format:
 *   "Single Person | <City>" plus "| EMI ₹X" only when EMI > 0.
 *   If there is no EMI (or EMI is 0), never show "No EMI" or any placeholder; remove the EMI segment completely.
 *   Use this dynamic subtitle consistently in both the in-app screen and the exported report.
 * - PROJECT.md (Interface Contracts M2, Milestones M2/M4).
 */
class HeaderSubtitleFormatterTest {

    // =========================================================================
    // Tier 1: Feature Coverage — 0 EMI and Positive EMI
    // =========================================================================

    @Test
    fun zeroEmi_strictlyFormatsWithoutEmiSegment() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Single Person",
            cityName = "Pune",
            emiRupees = 0L
        )

        assertEquals("Single Person | Pune", result)
        assertFalse("Must never contain 'No EMI'", result.contains("No EMI"))
        assertFalse("Must never contain 'EMI'", result.contains("EMI"))
    }

    @Test
    fun positiveEmi_appendsEmiSegmentWithRupeeSymbol() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Single Person",
            cityName = "Pune",
            emiRupees = 5_000L
        )

        assertEquals("Single Person | Pune | EMI ₹5,000", result)
    }

    @Test
    fun customProfileAndCity_formatsCorrectly() {
        val withEmi = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Married Couple",
            cityName = "Bengaluru",
            emiRupees = 25_000L
        )
        assertEquals("Married Couple | Bengaluru | EMI ₹25,000", withEmi)

        val withoutEmi = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Married Couple",
            cityName = "Bengaluru",
            emiRupees = 0L
        )
        assertEquals("Married Couple | Bengaluru", withoutEmi)
    }

    // =========================================================================
    // Tier 2: Boundary & Corner Cases
    // =========================================================================

    @Test
    fun negativeEmi_treatedAsZeroEmi_omitsEmiSegment() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Single Person",
            cityName = "Delhi",
            emiRupees = -1_000L
        )

        assertEquals("Single Person | Delhi", result)
        assertFalse(result.contains("EMI"))
    }

    @Test
    fun largeEmi_formatsWithStandardCommas() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Executive",
            cityName = "Mumbai",
            emiRupees = 50_000L
        )

        assertEquals("Executive | Mumbai | EMI ₹50,000", result)
    }

    @Test
    fun veryLargeEmi_containsEmiSegmentAndFormattedDigits() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Executive",
            cityName = "Mumbai",
            emiRupees = 150_000L
        )

        assertTrue("Must start with header prefix", result.startsWith("Executive | Mumbai | EMI ₹"))
        assertTrue("Must contain formatted digits", result.contains("150,000") || result.contains("1,50,000"))
    }

    @Test
    fun blankProfileTag_fallbacksToDefault() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "   ",
            cityName = "Hyderabad",
            emiRupees = 0L
        )

        assertEquals("Single Person | Hyderabad", result)
    }

    @Test
    fun blankCityName_fallbacksToDefault() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "Single Person",
            cityName = "",
            emiRupees = 10_000L
        )

        assertEquals("Single Person | Pune | EMI ₹10,000", result)
    }

    @Test
    fun whitespacePaddedInputs_trimmedProperly() {
        val result = HeaderSubtitleFormatterAdapter.format(
            profileTag = "  Single Person  ",
            cityName = "  Chennai  ",
            emiRupees = 8_000L
        )

        assertEquals("Single Person | Chennai | EMI ₹8,000", result)
    }
}
