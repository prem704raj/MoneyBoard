package com.premraj.moneyboard.core.domain.model

enum class CategoryGroup(val storageValue: String) {
    INCOME("INCOME"),
    HOUSING("HOUSING"),
    FOOD("FOOD"),
    UTILITIES("UTILITIES"),
    TRANSPORT("TRANSPORT"),
    LIFESTYLE("LIFESTYLE"),
    HEALTH("HEALTH"),
    EDUCATION("EDUCATION"),
    FAMILY_SUPPORT("FAMILY_SUPPORT"),
    TRAVEL("TRAVEL"),
    SAVINGS("SAVINGS"),
    INVESTMENTS("INVESTMENTS"),
    INSURANCE("INSURANCE"),
    DEBT("DEBT"),
    TRANSFER("TRANSFER"),
    OTHER("OTHER");

    companion object {
        fun fromStorage(value: String): CategoryGroup =
            entries.firstOrNull { it.storageValue == value }
                ?: error("Unknown CategoryGroup storage value: $value")
    }
}
