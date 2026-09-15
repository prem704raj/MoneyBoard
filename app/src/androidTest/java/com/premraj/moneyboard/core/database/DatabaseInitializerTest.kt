package com.premraj.moneyboard.core.database

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.premraj.moneyboard.core.common.TimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInitializerTest {
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
    fun initialize_isIdempotent() = runTest {
        val initializer = DatabaseInitializer(db, TimeProvider { 123456L })
        initializer.initialize()
        val firstAccounts = db.accountDao().countAll()
        val firstCategories = db.categoryDao().countAll()
        initializer.initialize()
        assertEquals(firstAccounts, db.accountDao().countAll())
        assertEquals(firstCategories, db.categoryDao().countAll())
        assertEquals(1L, firstAccounts)
        assertTrue(firstCategories >= 30L)
    }
}
