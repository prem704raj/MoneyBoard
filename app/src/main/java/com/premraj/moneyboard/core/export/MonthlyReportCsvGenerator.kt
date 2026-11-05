package com.premraj.moneyboard.core.export

import android.content.Context
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import java.io.File
import java.io.FileWriter
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object MonthlyReportCsvGenerator {

    fun generateCsv(
        context: Context,
        board: FinanceBoardUiModel,
        actualBoard: FinanceBoardUiModel? = null,
        month: YearMonth,
        settings: UserSettings
    ): File {
        val outputDir = File(context.cacheDir, "reports")
        return generateCsv(outputDir, board, actualBoard, month, settings)
    }

    fun generateCsv(
        outputDir: File,
        board: FinanceBoardUiModel,
        actualBoard: FinanceBoardUiModel? = null,
        month: YearMonth,
        settings: UserSettings
    ): File {
        outputDir.mkdirs()
        val monthName = month.format(DateTimeFormatter.ofPattern("MMM_yyyy"))
        val file = File(outputDir, "MoneyBoard_Expenses_${monthName}.csv")

        val actualMap = actualBoard?.expenseRows?.associateBy { it.categoryId.ifBlank { it.title.lowercase().trim() } } ?: emptyMap()

        FileWriter(file).use { writer ->
            writer.append("MoneyBoard Monthly Expense Report\n")
            writer.append("Month,${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}\n")
            writer.append("City,${settings.cityName}\n")
            writer.append("Profile,${settings.profileTag}\n")
            writer.append("Status,${settings.emiTag}\n")
            writer.append("In-hand Salary,${board.salaryText}\n")
            writer.append("Planned Total Expenses,${board.totalAllocationText}\n")
            val actualTotalText = actualBoard?.totalAllocationText ?: "₹0"
            writer.append("Actual Total Expenses,$actualTotalText\n")
            writer.append("Monthly Remaining (Plan),${board.remainingText}\n")
            if (actualBoard != null) {
                writer.append("Monthly Remaining (Actual),${actualBoard.remainingText}\n")
            }
            writer.append("\n")

            // Ledger with Plan vs Actual
            writer.append("No,Expense Name,Plan Amount (INR),Actual Amount (INR),Variance (INR),Status\n")
            board.expenseRows.forEach { row ->
                val titleClean = row.title.replace("\n", " ")
                val planAmt = row.amountMinor / 100L
                val key = row.categoryId.ifBlank { row.title.lowercase().trim() }
                val actualAmt = (actualMap[key]?.amountMinor ?: row.actualAmountMinor) / 100L
                val diff = actualAmt - planAmt
                val status = when {
                    actualAmt == 0L -> "Not Spent"
                    diff > 0L -> "Over Budget"
                    diff < 0L -> "Under Budget"
                    else -> "On Track"
                }
                writer.append("${row.number},\"$titleClean\",$planAmt,$actualAmt,$diff,$status\n")
            }

            // Summary
            writer.append("\nSummary Breakdown\n")
            writer.append("Total Allocation (Plan),${board.totalAllocationText}\n")
            if (actualBoard != null) {
                writer.append("Total Allocation (Actual),${actualBoard.totalAllocationText}\n")
            }
            writer.append("Total Savings & Investments,${board.totalSavingsText}\n")
            writer.append("Actual Spending (Living / Family / Travel),${board.actualSpendingText}\n")
            writer.append("Annual Balance,${board.annualBalanceText}\n")
        }

        return file
    }
}

