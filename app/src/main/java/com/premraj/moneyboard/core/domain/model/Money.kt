package com.premraj.moneyboard.core.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

data class Money(
    val amountMinor: Long,
    val currencyCode: String
) {
    init {
        currency(currencyCode)
    }

    operator fun plus(other: Money): Money {
        require(currencyCode == other.currencyCode) {
            "Currency mismatch: $currencyCode != ${other.currencyCode}"
        }
        return Money(Math.addExact(amountMinor, other.amountMinor), currencyCode)
    }

    operator fun minus(other: Money): Money {
        require(currencyCode == other.currencyCode) {
            "Currency mismatch: $currencyCode != ${other.currencyCode}"
        }
        return Money(Math.subtractExact(amountMinor, other.amountMinor), currencyCode)
    }

    operator fun times(multiplier: Int): Money {
        return Money(
            amountMinor = Math.multiplyExact(
                amountMinor,
                multiplier.toLong()
            ),
            currencyCode = currencyCode
        )
    }

    fun isPositive() = amountMinor > 0L
    fun isNegative() = amountMinor < 0L
    fun isZero() = amountMinor == 0L

    companion object {
        fun zero(currencyCode: String) = Money(0L, currencyCode)

        fun fromMajor(amountMajor: BigDecimal, currencyCode: String): Money {
            val currency = currency(currencyCode)
            val digits = currency.defaultFractionDigits
            require(digits >= 0) { "Unsupported fraction digits for $currencyCode" }
            val minor = amountMajor
                .movePointRight(digits)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact()
            return Money(minor, currency.currencyCode)
        }

        fun sum(values: Iterable<Money>, currencyCode: String): Money {
            var total = zero(currencyCode)
            values.forEach { total += it }
            return total
        }

        private fun currency(code: String): Currency {
            require(code.length == 3 && code == code.uppercase()) {
                "currencyCode must be an uppercase ISO-4217 code"
            }
            return try {
                Currency.getInstance(code)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unsupported currency code: $code", e)
            }
        }
    }
}
