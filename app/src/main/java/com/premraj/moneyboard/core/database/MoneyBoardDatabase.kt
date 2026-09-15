package com.premraj.moneyboard.core.database

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.premraj.moneyboard.core.database.dao.AccountDao
import com.premraj.moneyboard.core.database.dao.CategoryDao
import com.premraj.moneyboard.core.database.dao.MonthlyPlanDao
import com.premraj.moneyboard.core.database.dao.TransactionDao
import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.database.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        MonthlyIncomePlanEntity::class,
        MonthlyCategoryPlanEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class MoneyBoardDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyPlanDao(): MonthlyPlanDao

    companion object {
        const val DATABASE_NAME = "moneyboard.db"
        const val DATABASE_VERSION = 2
    }
}
