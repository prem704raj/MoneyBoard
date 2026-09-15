package com.premraj.moneyboard.core.domain.util

import java.time.YearMonth

fun YearMonth.toStorageKey(): Int {
    return year * 100 + monthValue
}

fun yearMonthFromStorageKey(key: Int): YearMonth {
    val year = key / 100
    val month = key % 100

    require(month in 1..12) {
        "Invalid month in storage key: $key"
    }

    return YearMonth.of(year, month)
}
