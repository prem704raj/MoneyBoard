package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class FinanceTransactionTest {

    private fun tx(
        amount: Money = Money(100_00L, "INR"),
        note: String? = null,
        merchant: String? = null
    ) = FinanceTransaction(
        id = "tx-1",
        accountId = "acc-1",
        categoryId = "cat-1",
        type = TransactionType.EXPENSE,
        amount = amount,
        accountingDate = LocalDate.of(2026, 9, 15),
        occurredAt = Instant.ofEpochMilli(1_000_000L),
        note = note,
        merchant = merchant,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        deletedAtEpochMillis = null
    )

    @Test
    fun validTransaction_isCreated() {
        val t = tx()
        assertEquals(100_00L, t.amount.amountMinor)
    }

    @Test
    fun zeroAmount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            tx(amount = Money(0L, "INR"))
        }
    }

    @Test
    fun negativeAmount_isRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            tx(amount = Money(-100L, "INR"))
        }
    }

    @Test
    fun noteTooLong_isRejected() {
        val longNote = "a".repeat(FinanceTransaction.MAX_NOTE_LENGTH + 1)
        assertThrows(IllegalArgumentException::class.java) {
            tx(note = longNote)
        }
    }

    @Test
    fun noteAtMaxLength_isAccepted() {
        val maxNote = "a".repeat(FinanceTransaction.MAX_NOTE_LENGTH)
        val t = tx(note = maxNote)
        assertEquals(FinanceTransaction.MAX_NOTE_LENGTH, t.note!!.length)
    }

    @Test
    fun merchantTooLong_isRejected() {
        val longMerchant = "m".repeat(FinanceTransaction.MAX_MERCHANT_LENGTH + 1)
        assertThrows(IllegalArgumentException::class.java) {
            tx(merchant = longMerchant)
        }
    }

    @Test
    fun merchantAtMaxLength_isAccepted() {
        val maxMerchant = "m".repeat(FinanceTransaction.MAX_MERCHANT_LENGTH)
        val t = tx(merchant = maxMerchant)
        assertEquals(FinanceTransaction.MAX_MERCHANT_LENGTH, t.merchant!!.length)
    }

    @Test
    fun unicodeMerchant_isAccepted() {
        val t = tx(merchant = "कैफे ☕ दिल्ली")
        assertEquals("कैफे ☕ दिल्ली", t.merchant)
    }

    @Test
    fun nullNoteAndMerchant_isAccepted() {
        val t = tx(note = null, merchant = null)
        assertEquals(null, t.note)
        assertEquals(null, t.merchant)
    }
}
