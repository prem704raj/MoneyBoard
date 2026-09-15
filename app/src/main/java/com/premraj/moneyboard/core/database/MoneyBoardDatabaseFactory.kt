package com.premraj.moneyboard.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver

object MoneyBoardDatabaseFactory {

    fun create(
        context: Context,
        databaseName: String = MoneyBoardDatabase.DATABASE_NAME
    ): MoneyBoardDatabase {
        return Room.databaseBuilder<MoneyBoardDatabase>(
            context = context.applicationContext,
            name = databaseName
        )
            .setDriver(AndroidSQLiteDriver())
            .build()
    }
}
