package com.premraj.moneyboard.core.database

import android.content.Context
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoneyBoardMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        file = context.getDatabasePath("migration-test.db"),
        databaseClass = MoneyBoardDatabase::class,
        driver = AndroidSQLiteDriver()
    )

    @Test
    fun migrate1To2_preservesAccountsCategoriesAndTransactions() = runTest {
        val v1Db = helper.createDatabase(1)

        v1Db.exec(
            """
            INSERT INTO accounts (id, name, type, openingBalanceMinor, currencyCode, archived, includeInNetWorth, createdAtEpochMillis, updatedAtEpochMillis)
            VALUES ('acc-1', 'Main Checking', 'BANK', 500000, 'INR', 0, 1, 1700000000000, 1700000000000)
            """.trimIndent()
        )

        v1Db.exec(
            """
            INSERT INTO categories (id, name, iconKey, type, groupName, sortOrder, isSystem, archived, createdAtEpochMillis, updatedAtEpochMillis)
            VALUES ('cat-1', 'Groceries', 'cart', 'EXPENSE', 'FOOD_AND_DINING', 1, 1, 0, 1700000000000, 1700000000000)
            """.trimIndent()
        )

        v1Db.exec(
            """
            INSERT INTO transactions (id, accountId, categoryId, type, amountMinor, currencyCode, localDateEpochDay, occurredAtEpochMillis, note, merchant, createdAtEpochMillis, updatedAtEpochMillis, deletedAtEpochMillis)
            VALUES ('tx-1', 'acc-1', 'cat-1', 'EXPENSE', 125000, 'INR', 19800, 1700000050000, 'Weekly grocery', 'Supermarket', 1700000050000, 1700000050000, NULL)
            """.trimIndent()
        )

        v1Db.close()

        val v2Db = helper.runMigrationsAndValidate(2)

        val accStmt = v2Db.prepare("SELECT id, name, openingBalanceMinor, currencyCode FROM accounts WHERE id = 'acc-1'")
        assertTrue(accStmt.step())
        assertEquals("acc-1", accStmt.getText(0))
        assertEquals("Main Checking", accStmt.getText(1))
        assertEquals(500000L, accStmt.getLong(2))
        assertEquals("INR", accStmt.getText(3))
        accStmt.close()

        val catStmt = v2Db.prepare("SELECT id, name, groupName FROM categories WHERE id = 'cat-1'")
        assertTrue(catStmt.step())
        assertEquals("cat-1", catStmt.getText(0))
        assertEquals("Groceries", catStmt.getText(1))
        assertEquals("FOOD_AND_DINING", catStmt.getText(2))
        catStmt.close()

        val txStmt = v2Db.prepare("SELECT id, amountMinor, note, merchant FROM transactions WHERE id = 'tx-1'")
        assertTrue(txStmt.step())
        assertEquals("tx-1", txStmt.getText(0))
        assertEquals(125000L, txStmt.getLong(1))
        assertEquals("Weekly grocery", txStmt.getText(2))
        assertEquals("Supermarket", txStmt.getText(3))
        txStmt.close()

        // Verify v2 tables exist and can take inserts
        v2Db.exec(
            """
            INSERT INTO monthly_income_plans (id, monthKey, name, plannedAmountMinor, currencyCode, expectedDateEpochDay, sortOrder, createdAtEpochMillis, updatedAtEpochMillis)
            VALUES ('plan-inc-1', 202609, 'Salary', 10000000, 'INR', NULL, 100, 1700000100000, 1700000100000)
            """.trimIndent()
        )

        val planStmt = v2Db.prepare("SELECT id, plannedAmountMinor FROM monthly_income_plans WHERE id = 'plan-inc-1'")
        assertTrue(planStmt.step())
        assertEquals("plan-inc-1", planStmt.getText(0))
        assertEquals(10000000L, planStmt.getLong(1))
        planStmt.close()

        v2Db.close()
    }

    private fun SQLiteConnection.exec(sql: String) {
        val stmt = prepare(sql)
        try {
            stmt.step()
        } finally {
            stmt.close()
        }
    }
}
