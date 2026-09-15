package com.premraj.moneyboard.core.domain.model

import java.time.Instant
import java.time.LocalDate

data class FinanceTransaction(
    val id: String,
    val accountId: String,
    val categoryId: String,
    val type: TransactionType,
    val amount: Money,
    val accountingDate: LocalDate,
    val occurredAt: Instant,
    val note: String?,
    val merchant: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val deletedAtEpochMillis: Long?
) {
    init {
        require(amount.amountMinor > 0L) { "Transaction amount must be greater than zero" }
        require(note == null || note.length <= MAX_NOTE_LENGTH) {
            "Transaction note cannot exceed $MAX_NOTE_LENGTH characters"
        }
        require(merchant == null || merchant.length <= MAX_MERCHANT_LENGTH) {
            "Merchant cannot exceed $MAX_MERCHANT_LENGTH characters"
        }
    }

    companion object {
        const val MAX_NOTE_LENGTH = 500
        const val MAX_MERCHANT_LENGTH = 120
    }
}
