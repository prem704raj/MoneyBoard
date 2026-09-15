package com.premraj.moneyboard.core.database

import com.premraj.moneyboard.core.common.TimeProvider

class DatabaseInitializer(
    private val database: MoneyBoardDatabase,
    private val timeProvider: TimeProvider
) {
    suspend fun initialize() {
        val now = timeProvider.nowEpochMillis()
        database.accountDao().insertIfAbsent(DefaultSeedData.defaultAccounts(now))
        database.categoryDao().insertIfAbsent(DefaultSeedData.defaultCategories(now))
    }
}
