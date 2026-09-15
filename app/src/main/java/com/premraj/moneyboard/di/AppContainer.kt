package com.premraj.moneyboard.di

import android.content.Context
import com.premraj.moneyboard.core.common.SystemTimeProvider
import com.premraj.moneyboard.core.common.UuidIdProvider
import com.premraj.moneyboard.core.dashboard.ObserveMonthlyDashboardUseCase
import com.premraj.moneyboard.core.data.repository.RoomAccountRepository
import com.premraj.moneyboard.core.data.repository.RoomCategoryRepository
import com.premraj.moneyboard.core.data.repository.RoomMonthlyPlanRepository
import com.premraj.moneyboard.core.data.repository.RoomTransactionRepository
import com.premraj.moneyboard.core.database.DatabaseInitializer
import com.premraj.moneyboard.core.database.MoneyBoardDatabase
import com.premraj.moneyboard.core.database.MoneyBoardDatabaseFactory
import com.premraj.moneyboard.core.domain.repository.AccountRepository
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import com.premraj.moneyboard.debug.ReferenceDemoPlanSeeder

class AppContainer(
    context: Context
) {
    val database: MoneyBoardDatabase =
        MoneyBoardDatabaseFactory.create(context)

    val accountRepository: AccountRepository =
        RoomAccountRepository(
            dao = database.accountDao(),
            idProvider = UuidIdProvider,
            timeProvider = SystemTimeProvider
        )

    val categoryRepository: CategoryRepository =
        RoomCategoryRepository(
            dao = database.categoryDao(),
            idProvider = UuidIdProvider,
            timeProvider = SystemTimeProvider
        )

    val transactionRepository: TransactionRepository =
        RoomTransactionRepository(
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao(),
            categoryDao = database.categoryDao(),
            idProvider = UuidIdProvider,
            timeProvider = SystemTimeProvider
        )

    val monthlyPlanRepository: MonthlyPlanRepository =
        RoomMonthlyPlanRepository(
            monthlyPlanDao = database.monthlyPlanDao(),
            categoryDao = database.categoryDao(),
            idProvider = UuidIdProvider,
            timeProvider = SystemTimeProvider
        )

    val databaseInitializer =
        DatabaseInitializer(
            database = database,
            timeProvider = SystemTimeProvider
        )

    val observeMonthlyDashboardUseCase =
        ObserveMonthlyDashboardUseCase(
            monthlyPlanRepository = monthlyPlanRepository,
            transactionRepository = transactionRepository,
            categoryRepository = categoryRepository,
            defaultCurrencyCode = "INR"
        )

    val referenceDemoPlanSeeder =
        ReferenceDemoPlanSeeder(
            monthlyPlanRepository = monthlyPlanRepository
        )
}
