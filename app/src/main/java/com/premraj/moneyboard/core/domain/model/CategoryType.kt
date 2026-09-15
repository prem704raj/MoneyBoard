package com.premraj.moneyboard.core.domain.model

enum class CategoryType(val storageValue: String) {
    INCOME("INCOME"),
    EXPENSE("EXPENSE"),
    SAVING("SAVING"),
    INVESTMENT("INVESTMENT"),
    TRANSFER("TRANSFER");

    fun accepts(transactionType: TransactionType): Boolean =
        storageValue == transactionType.storageValue

    companion object {
        fun fromStorage(value: String): CategoryType =
            entries.firstOrNull { it.storageValue == value }
                ?: error("Unknown CategoryType storage value: $value")
    }
}
