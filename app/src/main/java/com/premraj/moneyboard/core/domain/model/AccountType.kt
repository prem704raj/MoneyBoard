package com.premraj.moneyboard.core.domain.model

enum class AccountType(val storageValue: String) {
    CASH("CASH"),
    BANK("BANK"),
    CREDIT_CARD("CREDIT_CARD"),
    WALLET("WALLET"),
    INVESTMENT("INVESTMENT"),
    OTHER("OTHER");

    companion object {
        fun fromStorage(value: String): AccountType =
            entries.firstOrNull { it.storageValue == value }
                ?: error("Unknown AccountType storage value: $value")
    }
}
