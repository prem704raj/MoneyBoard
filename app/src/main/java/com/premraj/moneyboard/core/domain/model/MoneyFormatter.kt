package com.premraj.moneyboard.core.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Canonical money formatter for MoneyBoard.
 *
 * All user-visible monetary values MUST go through this formatter.
 * Storage uses [Money.amountMinor]; this converts to display strings
 * respecting currency fraction digits, locale grouping, and symbol placement.
 */
object MoneyFormatter {

    /**
     * Formats a [Money] value for display using the given [locale].
     * Examples:
     *   INR 7000000 (minor) → "₹70,000.00" (en-IN)
     *   JPY 5000   (minor) → "¥5,000"      (ja-JP)
     *   USD 12345  (minor) → "$123.45"      (en-US)
     */
    fun format(money: Money, locale: Locale = Locale.getDefault()): String {
        val currency = Currency.getInstance(money.currencyCode)
        val fractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
        val majorAmount = BigDecimal.valueOf(money.amountMinor)
            .movePointLeft(fractionDigits)

        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            minimumFractionDigits = fractionDigits
            maximumFractionDigits = fractionDigits
        }

        return formatter.format(majorAmount)
    }

    /**
     * Formats without fraction digits for cleaner dashboard display
     * when the minor amount is an exact multiple of the currency's minor unit.
     * Falls back to full precision if there are fractional parts.
     *
     * Examples:
     *   INR 7000000 (minor) → "₹70,000"
     *   INR 7000050 (minor) → "₹70,000.50"
     */
    fun formatCompact(money: Money, locale: Locale = Locale.getDefault()): String {
        val currency = Currency.getInstance(money.currencyCode)
        val fractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
        val majorAmount = BigDecimal.valueOf(money.amountMinor)
            .movePointLeft(fractionDigits)

        val hasDecimal = majorAmount.stripTrailingZeros().scale() > 0

        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = currency
            if (hasDecimal) {
                minimumFractionDigits = fractionDigits
                maximumFractionDigits = fractionDigits
            } else {
                minimumFractionDigits = 0
                maximumFractionDigits = 0
            }
        }

        return formatter.format(majorAmount)
    }

    /**
     * Formats a savings rate from basis points.
     * 2050 basis points → "20.5%"
     * null → "—"
     */
    fun formatSavingsRate(basisPoints: Int?): String {
        if (basisPoints == null) return "—"
        val percent = BigDecimal.valueOf(basisPoints.toLong())
            .movePointLeft(2)
            .stripTrailingZeros()
        return "${percent.toPlainString()}%"
    }
}
