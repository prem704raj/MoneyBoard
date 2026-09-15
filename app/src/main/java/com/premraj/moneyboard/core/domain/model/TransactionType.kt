package com.premraj.moneyboard.core.domain.model

enum class TransactionType(val storageValue: String) {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    SAVING("SAVING"),
    INVESTMENT("INVESTMENT"),
    TRANSFER("TRANSFER");

    companion object {
        fun fromStorage(value: String): TransactionType =
            entries.firstOrNull { it.storageValue == value }
                ?: error("Unknown TransactionType storage value: $value")
    }
}
