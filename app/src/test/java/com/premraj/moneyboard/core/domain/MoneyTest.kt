package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigDecimal

class MoneyTest {
    @Test
    fun plus_sameCurrency_addsExactly() {
        val result = Money(10_001L, "INR") + Money(20_002L, "INR")
        assertEquals(30_003L, result.amountMinor)
        assertEquals("INR", result.currencyCode)
    }

    @Test
    fun minus_canProduceNegativeBalance() {
        val result = Money(10_000L, "INR") - Money(12_500L, "INR")
        assertEquals(-2_500L, result.amountMinor)
    }

    @Test
    fun plus_differentCurrency_rejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "INR") + Money(100L, "USD")
        }
    }

    @Test
    fun fromMajor_inrConvertsToPaiseExactly() {
        val money = Money.fromMajor(BigDecimal("70000.25"), "INR")
        assertEquals(7_000_025L, money.amountMinor)
    }

    @Test
    fun unsupportedSubPaisePrecision_isRejected() {
        assertThrows(ArithmeticException::class.java) {
            Money.fromMajor(BigDecimal("10.001"), "INR")
        }
    }

    @Test
    fun lowercaseCurrencyCode_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            Money(100L, "inr")
        }
    }
}
