package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MoneyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class MoneyFormatterTest {

    @Test
    fun formatCompact_inr_wholeAmount() {
        val money = Money(70_000_00L, "INR")
        val result = MoneyFormatter.formatCompact(money, Locale.Builder().setLanguage("en").setRegion("IN").build())
        // Should contain the amount without decimals
        assertTrue(
            "Expected formatted INR amount to contain '70,000', got: $result",
            result.contains("70,000") || result.contains("70000")
        )
    }

    @Test
    fun formatCompact_inr_fractionalAmount() {
        val money = Money(70_000_50L, "INR")
        val result = MoneyFormatter.formatCompact(money, Locale.Builder().setLanguage("en").setRegion("IN").build())
        assertTrue(
            "Expected formatted INR to have decimal, got: $result",
            result.contains("50") || result.contains(".50")
        )
    }

    @Test
    fun formatSavingsRate_normalValue() {
        assertEquals("20%", MoneyFormatter.formatSavingsRate(2000))
    }

    @Test
    fun formatSavingsRate_fractionalValue() {
        assertEquals("20.5%", MoneyFormatter.formatSavingsRate(2050))
    }

    @Test
    fun formatSavingsRate_null() {
        assertEquals("—", MoneyFormatter.formatSavingsRate(null))
    }

    @Test
    fun formatSavingsRate_zero() {
        assertEquals("0%", MoneyFormatter.formatSavingsRate(0))
    }
}

class MoneyMultiCurrencyTest {

    // --- JPY has 0 fraction digits ---

    @Test
    fun jpy_zeroFractionDigits() {
        val money = Money.fromMajor(BigDecimal("5000"), "JPY")
        assertEquals(5000L, money.amountMinor)
    }

    @Test
    fun jpy_rejectsDecimal() {
        assertThrows(ArithmeticException::class.java) {
            Money.fromMajor(BigDecimal("100.5"), "JPY")
        }
    }

    @Test
    fun jpy_addition() {
        val result = Money(1000L, "JPY") + Money(2500L, "JPY")
        assertEquals(3500L, result.amountMinor)
    }

    // --- USD has 2 fraction digits ---

    @Test
    fun usd_fromMajor() {
        val money = Money.fromMajor(BigDecimal("123.45"), "USD")
        assertEquals(12345L, money.amountMinor)
    }

    // --- KWD has 3 fraction digits ---

    @Test
    fun kwd_threeFractionDigits() {
        val money = Money.fromMajor(BigDecimal("1.234"), "KWD")
        assertEquals(1234L, money.amountMinor)
    }

    @Test
    fun kwd_rejectsExcessPrecision() {
        assertThrows(ArithmeticException::class.java) {
            Money.fromMajor(BigDecimal("1.2345"), "KWD")
        }
    }

    // --- Cross-currency prohibition ---

    @Test
    fun crossCurrency_additionRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "USD") + Money(100L, "JPY")
        }
    }

    @Test
    fun crossCurrency_subtractionRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "INR") - Money(100L, "USD")
        }
    }

    // --- Overflow ---

    @Test
    fun addition_overflow_throwsArithmeticException() {
        assertThrows(ArithmeticException::class.java) {
            Money(Long.MAX_VALUE, "INR") + Money(1L, "INR")
        }
    }

    @Test
    fun subtraction_overflow_throwsArithmeticException() {
        assertThrows(ArithmeticException::class.java) {
            Money(Long.MIN_VALUE, "INR") - Money(1L, "INR")
        }
    }

    @Test
    fun multiplication_overflow_throwsArithmeticException() {
        assertThrows(ArithmeticException::class.java) {
            Money(Long.MAX_VALUE / 2 + 1, "INR") * 2
        }
    }

    // --- Invalid currency codes ---

    @Test
    fun invalidCurrencyCode_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "XYZ")
        }
    }

    @Test
    fun emptyString_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "")
        }
    }

    @Test
    fun twoLetterCode_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "US")
        }
    }

    // --- Money.zero ---

    @Test
    fun zero_hasZeroAmount() {
        val zero = Money.zero("INR")
        assertEquals(0L, zero.amountMinor)
        assertEquals("INR", zero.currencyCode)
        assertTrue(zero.isZero())
    }

    // --- Money.sum ---

    @Test
    fun sum_emptyList_returnsZero() {
        val result = Money.sum(emptyList(), "INR")
        assertEquals(0L, result.amountMinor)
    }

    @Test
    fun sum_multipleValues() {
        val values = listOf(
            Money(100L, "INR"),
            Money(200L, "INR"),
            Money(300L, "INR")
        )
        val result = Money.sum(values, "INR")
        assertEquals(600L, result.amountMinor)
    }

    // --- Negative values ---

    @Test
    fun subtraction_canProduceNegative() {
        val result = Money(100L, "INR") - Money(200L, "INR")
        assertEquals(-100L, result.amountMinor)
        assertTrue(result.isNegative())
    }

    // --- fromMajor round-trip ---

    @Test
    fun inr_roundTrip_exactAmount() {
        val major = BigDecimal("1234.56")
        val money = Money.fromMajor(major, "INR")
        assertEquals(123456L, money.amountMinor)
    }
}
