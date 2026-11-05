package com.premraj.moneyboard.core.export

import android.content.Context
import com.premraj.moneyboard.core.database.MoneyBoardDatabase
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DataBackupHelper {

    suspend fun createFullBackupFile(
        context: Context,
        database: MoneyBoardDatabase,
        settings: UserSettings
    ): File {
        val jsonString = FullDatabaseBackupManager.exportAllData(database, settings)
        val outputDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val filename = "SalaryLane_Full_Backup_${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd"))}.json"
        val file = File(outputDir, filename)

        FileOutputStream(file).use { out ->
            out.write(jsonString.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun createBackupFile(
        context: Context,
        settings: UserSettings,
        board: FinanceBoardUiModel,
        month: YearMonth
    ): File {
        val root = JSONObject().apply {
            put("version", "0.3.0")
            put("app", "SalaryLane")
            put("exportTimestamp", Instant.now().toString())
            put("targetMonth", month.toString())

            // User Profile & Settings
            put("settings", JSONObject().apply {
                put("cityName", settings.cityName)
                put("profileTag", settings.profileTag)
                put("emiTag", settings.emiTag)
                put("inHandSalaryRupees", settings.inHandSalaryRupees)
                put("currencyCode", settings.currencyCode)
                put("themeMode", settings.themeMode.name)
                put("defaultMode", settings.defaultMode.name)
            })

            // Category Expenses
            val expensesArray = JSONArray()
            board.expenseRows.forEach { row ->
                val item = JSONObject().apply {
                    put("categoryId", row.categoryId)
                    put("number", row.number)
                    put("title", row.title)
                    put("amountRupees", row.amountMinor / 100L)
                }
                expensesArray.put(item)
            }
            put("expenses", expensesArray)

            // High-level Metrics Snapshot
            put("snapshot", JSONObject().apply {
                put("totalMonthlyExpenses", board.totalAllocationText)
                put("remainingBalance", board.remainingText)
                put("totalSavings", board.totalSavingsText)
                put("savingsRate", board.savingsRateText)
                put("actualLivingSpending", board.actualSpendingText)
                put("annualBalance", board.annualBalanceText)
            })
        }

        val outputDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val filename = "MoneyBoard_Backup_${month.format(DateTimeFormatter.ofPattern("yyyy_MM"))}.json"
        val file = File(outputDir, filename)

        FileOutputStream(file).use { out ->
            out.write(root.toString(2).toByteArray(Charsets.UTF_8))
        }

        return file
    }
}

