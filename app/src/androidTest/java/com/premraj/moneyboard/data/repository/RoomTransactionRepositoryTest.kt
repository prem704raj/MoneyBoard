package com.premraj.moneyboard.data.repository

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.premraj.moneyboard.core.common.IdProvider
import com.premraj.moneyboard.core.common.TimeProvider
import com.premraj.moneyboard.core.data.repository.RoomTransactionRepository
import com.premraj.moneyboard.core.database.DatabaseInitializer
import com.premraj.moneyboard.core.database.MoneyBoardDatabase
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.testing.assertSuspendThrows
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class RoomTransactionRepositoryTest {
    private lateinit var db: MoneyBoardDatabase
    private lateinit var repository: RoomTransactionRepository
    private var idCounter = 0
    private val now = 1_757_900_000_000L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder<MoneyBoardDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
        DatabaseInitializer(db, TimeProvider { now }).initialize()
        repository = RoomTransactionRepository(
            transactionDao = db.transactionDao(),
            accountDao = db.accountDao(),
            categoryDao = db.categoryDao(),
            idProvider = IdProvider { idCounter += 1; "generated-$idCounter" },
            timeProvider = TimeProvider { now }
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createExpense_persistsInCorrectMonth() = runTest {
        val created = repository.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_RENT,
                type = TransactionType.EXPENSE,
                amount = Money(1_000_000L, "INR"),
                accountingDate = LocalDate.of(2026, 9, 15),
                occurredAt = Instant.ofEpochMilli(now),
                note = "September rent"
            )
        )
        assertEquals("generated-1", created.id)
        val rows = repository.observeTransactions(YearMonth.of(2026, 9)).first()
        assertEquals(1, rows.size)
        assertEquals(1_000_000L, rows.single().amount.amountMinor)
    }

    @Test
    fun wrongCategoryType_isRejected() = runTest {
        assertSuspendThrows<IllegalArgumentException> {
            repository.createTransaction(
                CreateTransactionCommand(
                    accountId = SeedIds.CASH_ACCOUNT,
                    categoryId = SeedIds.INCOME_SALARY,
                    type = TransactionType.EXPENSE,
                    amount = Money(50_000L, "INR"),
                    accountingDate = LocalDate.of(2026, 9, 15),
                    occurredAt = Instant.ofEpochMilli(now)
                )
            )
        }
    }

    @Test
    fun accountCurrencyMismatch_isRejected() = runTest {
        assertSuspendThrows<IllegalArgumentException> {
            repository.createTransaction(
                CreateTransactionCommand(
                    accountId = SeedIds.CASH_ACCOUNT,
                    categoryId = SeedIds.EXPENSE_RENT,
                    type = TransactionType.EXPENSE,
                    amount = Money(10_000L, "USD"),
                    accountingDate = LocalDate.of(2026, 9, 15),
                    occurredAt = Instant.ofEpochMilli(now)
                )
            )
        }
    }

    @Test
    fun softDelete_removesFromNormalReads() = runTest {
        val created = repository.createTransaction(
            CreateTransactionCommand(
                accountId = SeedIds.CASH_ACCOUNT,
                categoryId = SeedIds.EXPENSE_GROCERIES,
                type = TransactionType.EXPENSE,
                amount = Money(75_000L, "INR"),
                accountingDate = LocalDate.of(2026, 9, 14),
                occurredAt = Instant.ofEpochMilli(now)
            )
        )
        repository.softDeleteTransaction(created.id)
        assertNull(repository.getTransaction(created.id))
        assertEquals(0, repository.observeTransactions(YearMonth.of(2026, 9)).first().size)
    }

    @Test
    fun monthlyTotal_excludesNextMonth() = runTest {
        suspend fun add(date: LocalDate, amount: Long) {
            repository.createTransaction(
                CreateTransactionCommand(
                    accountId = SeedIds.CASH_ACCOUNT,
                    categoryId = SeedIds.EXPENSE_GROCERIES,
                    type = TransactionType.EXPENSE,
                    amount = Money(amount, "INR"),
                    accountingDate = date,
                    occurredAt = Instant.ofEpochMilli(now)
                )
            )
        }

        add(LocalDate.of(2026, 9, 1), 1_000_000L)
        add(LocalDate.of(2026, 9, 30), 200_000L)
        add(LocalDate.of(2026, 10, 1), 900_000L)

        val total = repository.observeTotal(
            YearMonth.of(2026, 9),
            TransactionType.EXPENSE,
            "INR"
        ).first()
        assertEquals(1_200_000L, total.amountMinor)
    }
}
