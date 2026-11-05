package com.premraj.moneyboard.core.util

/**
 * Formats header subtitle strictly in the format:
 * - "Single Person | <City>" when [emiRupees] <= 0
 * - "Single Person | <City> | EMI ₹<formatted>" when [emiRupees] > 0
 *
 * If EMI is 0, negative, or absent, the EMI segment is omitted completely.
 * Never outputs "No EMI" or any placeholder.
 */
object HeaderSubtitleFormatter {

    fun format(
        profileTag: String = "Single Person",
        cityName: String = "Pune",
        emiRupees: Long = 0L
    ): String {
        val profile = profileTag.trim().ifBlank { "Single Person" }
        val city = cityName.trim().ifBlank { "Pune" }
        val base = "$profile | $city"
        return if (emiRupees > 0L) {
            val formattedEmi = "%,d".format(emiRupees)
            "$base | EMI ₹$formattedEmi"
        } else {
            base
        }
    }
}
