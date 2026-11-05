package com.premraj.moneyboard.testutil

/**
 * Authoritative reference implementation of HeaderSubtitleFormatter derived from:
 * - ORIGINAL_REQUEST.md (Requirement R2)
 * - PROJECT.md (Interface Contracts M2)
 * - explorer_survey_1/handoff.md (Section 2.2)
 */
object ReferenceHeaderSubtitleFormatter {

    /**
     * Formats the header subtitle strictly in the format:
     * - "Single Person | <City>" when [emiRupees] <= 0
     * - "Single Person | <City> | EMI ₹<formatted>" when [emiRupees] > 0
     *
     * Never returns "No EMI" or any placeholder when [emiRupees] is 0 or absent.
     */
    fun format(
        profileTag: String = "Single Person",
        cityName: String,
        emiRupees: Long
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
