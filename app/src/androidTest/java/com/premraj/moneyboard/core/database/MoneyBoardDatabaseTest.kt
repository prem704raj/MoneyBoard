package com.premraj.moneyboard.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class MoneyBoardDatabaseTest {
    private lateinit var db: MoneyBoardDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<MoneyBoardDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertReadAndAggregate_areCorrect() = runTest {
        db.accountDao().insert(testAccount())
        db.categoryDao().insert(testCategory())
        val date = LocalDate.of(2026, 9, 15)
        db.transactionDao().insert(
            TransactionEntity(
                id = "tx-1",
                accountId = "account-test",
                categoryId = "category-test",
                type = TransactionType.EXPENSE.storageValue,
                amountMinor = 12_345L,
                currencyCode = "INR",
                localDateEpochDay = date.toEpochDay(),
                occurredAtEpochMillis = 1_757_900_000_000L,
                note = "Test expense",
                merchant = null,
                createdAtEpochMillis = 1_757_900_000_000L,
                updatedAtEpochMillis = 1_757_900_000_000L,
                deletedAtEpochMillis = null
            )
        )

        val rows = db.transactionDao().observeActiveBetween(
            LocalDate.of(2026, 9, 1).toEpochDay(),
            LocalDate.of(2026, 9, 30).toEpochDay()
        ).first()
        assertEquals(1, rows.size)
        assertEquals(12_345L, rows.single().amountMinor)

        val total = db.transactionDao().observeTotalForTypeBetween(
            LocalDate.of(2026, 9, 1).toEpochDay(),
            LocalDate.of(2026, 9, 30).toEpochDay(),
            TransactionType.EXPENSE.storageValue,
            "INR"
        ).first()
        assertEquals(12_345L, total)
    }

    @Test
    fun softDelete_excludesNormalReads() = runTest {
        db.accountDao().insert(testAccount())
        db.categoryDao().insert(testCategory())
        val date = LocalDate.of(2026, 9, 15)
        db.transactionDao().insert(
            TransactionEntity(
                id = "tx-delete",
                accountId = "account-test",
                categoryId = "category-test",
                type = TransactionType.EXPENSE.storageValue,
                amountMinor = 500L,
                currencyCode = "INR",
                localDateEpochDay = date.toEpochDay(),
                occurredAtEpochMillis = 1000L,
                note = null,
                merchant = null,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L,
                deletedAtEpochMillis = null
            )
        )
        db.transactionDao().softDelete("tx-delete", 2000L)
        assertNull(db.transactionDao().getActiveById("tx-delete"))
        assertEquals(0L, db.transactionDao().countActive())
    }

    private fun testAccount() = AccountEntity(
        id = "account-test",
        name = "Test Account",
        type = AccountType.BANK.storageValue,
        openingBalanceMinor = 0L,
        currencyCode = "INR",
        includeInNetWorth = true,
        archived = false,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L
    )

    private fun testCategory() = CategoryEntity(
        id = "category-test",
        name = "Test Expense",
        iconKey = "test",
        type = CategoryType.EXPENSE.storageValue,
        groupKey = CategoryGroup.OTHER.storageValue,
        sortOrder = 1,
        systemCategory = false,
        archived = false,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L
    )
}
