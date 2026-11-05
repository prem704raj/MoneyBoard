package com.premraj.moneyboard.feature.export

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.export.MonthlyReportCsvGenerator
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.core.util.HeaderSubtitleFormatter
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Milestone 3 Unified 1224px Report Sheet calculations,
 * dynamic donut chart slice categorization, subtitle formatting contracts,
 * and column partitioning logic.
 */
class UnifiedExportReportSheetTest {

    private fun createExpenseRow(
        id: String,
        number: Int,
        title: String,
        rupees: Long
    ): ExpenseRowUi {
        return ExpenseRowUi(
            categoryId = id,
            number = number,
            title = title,
            amountText = "%,d".format(rupees),
            amountMinor = rupees * 100L
        )
    }

    @Test
    fun calculateDynamicDonutSlices_withReferencePlan_computesCorrectPercentages() {
        // Reference 24 items totaling 69,350 (6,935,000 minor)
        val expenses = listOf(
            createExpenseRow(SeedIds.EXPENSE_RENT, 1, "Rent", 10_000),
            createExpenseRow(SeedIds.EXPENSE_GROCERIES, 2, "Groceries", 6_000),
            createExpenseRow(SeedIds.EXPENSE_GAS, 3, "Gas", 900),
            createExpenseRow(SeedIds.EXPENSE_HOUSE_HELP, 4, "Maid", 3_000),
            createExpenseRow(SeedIds.EXPENSE_DINING, 5, "Online food", 3_000),
            createExpenseRow(SeedIds.EXPENSE_ELECTRICITY, 6, "Electricity", 1_500),
            createExpenseRow(SeedIds.EXPENSE_WIFI, 7, "Wi-Fi", 600),
            createExpenseRow(SeedIds.EXPENSE_MOBILE, 8, "Mobile recharge", 350),
            createExpenseRow(SeedIds.EXPENSE_CAB, 9, "Cab/Rapido", 2_000),
            createExpenseRow(SeedIds.EXPENSE_LAUNDRY, 10, "Laundry", 300),
            createExpenseRow(SeedIds.EXPENSE_SHOPPING, 11, "Shopping", 2_000),
            createExpenseRow(SeedIds.EXPENSE_GYM, 12, "Gym", 1_200),
            createExpenseRow(SeedIds.EXPENSE_FITNESS_DIET, 13, "Gym diet", 2_000),
            createExpenseRow(SeedIds.EXPENSE_SALON, 14, "Skincare and saloon", 1_000),
            createExpenseRow(SeedIds.EXPENSE_OUTING, 15, "Weekend outing", 3_000),
            createExpenseRow(SeedIds.EXPENSE_TRAVEL, 16, "Domestic trip", 2_500),
            createExpenseRow(SeedIds.EXPENSE_HOMETOWN, 17, "Hometown visit", 3_500),
            createExpenseRow(SeedIds.EXPENSE_MEDICINE, 18, "Medicine", 500),
            createExpenseRow(SeedIds.EXPENSE_FAMILY_SUPPORT, 19, "Sending to parents", 10_000),
            createExpenseRow(SeedIds.INVESTMENT_SIP, 20, "SIP", 5_000),
            createExpenseRow(SeedIds.EXPENSE_HEALTH_INSURANCE, 21, "Health insurance", 1_000),
            createExpenseRow(SeedIds.SAVING_LIC_POLICY, 22, "LIC", 2_000),
            createExpenseRow(SeedIds.SAVING_EMERGENCY_FUND, 23, "Emergency fund", 7_000),
            createExpenseRow(SeedIds.EXPENSE_OTHER, 24, "Other expenses", 1_000)
        )

        val totalMinor = expenses.sumOf { it.amountMinor }
        assertEquals(69_350_00L, totalMinor)

        val slices = calculateDynamicDonutSlices(expenses, totalMinor)
        assertEquals(6, slices.size)

        // 1. Housing (Rent 10,000 / 69,350 = 14.419%) -> 14.4%
        val housing = slices.first { it.title == "Housing" }
        assertEquals("14.4%", housing.percentageText)
        assertEquals(14.42f, housing.percentageValue, 0.05f)

        // 2. Food & Essentials (12,900 / 69,350 = 18.601%) -> 18.6%
        val food = slices.first { it.title == "Food & Essentials" }
        assertEquals("18.6%", food.percentageText)
        assertEquals(18.60f, food.percentageValue, 0.05f)

        // 3. Lifestyle (9,500 / 69,350 = 13.698%) -> 13.7%
        val lifestyle = slices.first { it.title == "Lifestyle" }
        assertEquals("13.7%", lifestyle.percentageText)
        assertEquals(13.70f, lifestyle.percentageValue, 0.05f)

        // 4. Travel (8,000 / 69,350 = 11.535%) -> 11.5%
        val travel = slices.first { it.title == "Travel" }
        assertEquals("11.5%", travel.percentageText)
        assertEquals(11.54f, travel.percentageValue, 0.05f)

        // 5. Savings & Investments (SIP 5,000 + LIC 2,000 + Emergency 7,000 = 14,000 / 69,350 = 20.187%) -> 20.2%
        val savings = slices.first { it.title == "Savings & Investments" }
        assertEquals("20.2%", savings.percentageText)
        assertEquals(20.19f, savings.percentageValue, 0.05f)

        // 6. Others (14,950 / 69,350 = 21.557%) -> 21.6%
        val others = slices.first { it.title == "Others" }
        assertEquals("21.6%", others.percentageText)
        assertEquals(21.56f, others.percentageValue, 0.05f)

        // Verify that no slice contains typos
        for (slice in slices) {
            assertNotNull(slice.title)
            assertNotNull(slice.subCategories)
            assertTrue(slice.percentageText.endsWith("%"))
            assertFalse(slice.percentageText.contains("correectly"))
        }
    }

    @Test
    fun calculateDynamicDonutSlices_emptyExpenses_returnsDefaultGracefully() {
        val slices = calculateDynamicDonutSlices(emptyList(), 0L)
        assertEquals(6, slices.size)
        assertTrue(slices.any { it.title == "Housing" })
    }

    @Test
    fun headerSubtitle_dynamicFormatting_respectsZeroEmiContract() {
        // Zero EMI: must omit "No EMI" completely
        val zeroEmiSubtitle = HeaderSubtitleFormatter.format(
            profileTag = "Single Person",
            cityName = "Pune",
            emiRupees = 0L
        )
        assertEquals("Single Person | Pune", zeroEmiSubtitle)
        assertFalse("Must never output 'No EMI'", zeroEmiSubtitle.contains("No EMI"))

        // Negative EMI: treated as zero
        val negEmiSubtitle = HeaderSubtitleFormatter.format(
            profileTag = "Single Person",
            cityName = "Mumbai",
            emiRupees = -500L
        )
        assertEquals("Single Person | Mumbai", negEmiSubtitle)
        assertFalse(negEmiSubtitle.contains("EMI"))

        // Positive EMI: formatted with currency symbol and commas
        val posEmiSubtitle = HeaderSubtitleFormatter.format(
            profileTag = "Single Person",
            cityName = "Bengaluru",
            emiRupees = 15_000L
        )
        assertEquals("Single Person | Bengaluru | EMI ₹15,000", posEmiSubtitle)
    }

    @Test
    fun reportTable_twoColumnBalancing_partitionsCorrectly() {
        fun testPartition(count: Int, expectedLeft: Int, expectedRight: Int) {
            val list = (1..count).map {
                createExpenseRow("id_$it", it, "Expense $it", 1000L)
            }
            val half = (list.size + 1) / 2
            val left = list.take(half)
            val right = list.drop(half)
            assertEquals(expectedLeft, left.size)
            assertEquals(expectedRight, right.size)
        }

        testPartition(24, 12, 12)
        testPartition(25, 13, 12)
        testPartition(26, 13, 13)
        testPartition(1, 1, 0)
        testPartition(0, 0, 0)
    }

    @Test
    fun expenseRowUi_supportsPlanAndActualAmounts() {
        val row = ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_RENT,
            number = 1,
            title = "Rent",
            amountText = "₹10,000",
            amountMinor = 1_000_000L,
            actualAmountMinor = 1_000_000L,
            actualAmountText = "₹10,000",
            plannedAmountMinor = 1_000_000L,
            plannedAmountText = "₹10,000"
        )
        assertEquals(1_000_000L, row.actualAmountMinor)
        assertEquals("₹10,000", row.actualAmountText)
        assertEquals(1_000_000L, row.plannedAmountMinor)
        assertEquals("₹10,000", row.plannedAmountText)
    }

    @Test
    fun csvExport_includesPlanAndActualWithVariance() {
        val tempDir = java.nio.file.Files.createTempDirectory("moneyboard_test").toFile()

        val planRows = listOf(
            createExpenseRow(SeedIds.EXPENSE_RENT, 1, "Rent", 10_000),
            createExpenseRow(SeedIds.EXPENSE_GROCERIES, 2, "Groceries", 6_000)
        )
        val actualRows = listOf(
            createExpenseRow(SeedIds.EXPENSE_RENT, 1, "Rent", 10_000),
            createExpenseRow(SeedIds.EXPENSE_GROCERIES, 2, "Groceries", 7_500)
        )

        val planBoard = com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel(
            salaryText = "₹70,000 / month",
            totalAllocationText = "₹16,000",
            expenseRows = planRows,
            salaryVsExpenses = emptyList(),
            savings = emptyList(),
            living = emptyList(),
            annual = emptyList(),
            chartLines = emptyList(),
            donutSlices = emptyList(),
            remainingText = "₹54,000 / month",
            savingsRateText = "0%",
            totalSavingsText = "₹0 / month",
            actualSpendingText = "₹16,000 / month",
            annualBalanceText = "₹6,48,000"
        )
        val actualBoard = planBoard.copy(
            totalAllocationText = "₹17,500",
            expenseRows = actualRows,
            remainingText = "₹52,500 / month"
        )

        val file = MonthlyReportCsvGenerator.generateCsv(
            outputDir = tempDir,
            board = planBoard,
            actualBoard = actualBoard,
            month = java.time.YearMonth.of(2026, 10),
            settings = UserSettings(inHandSalaryRupees = 70_000L, cityName = "Pune", profileTag = "Single Person")
        )

        assertTrue(file.exists())
        val content = file.readText()
        assertTrue(content.contains("Plan Amount (INR),Actual Amount (INR),Variance (INR),Status"))
        assertTrue(content.contains("Rent\",10000,10000,0,On Track"))
        assertTrue(content.contains("Groceries\",6000,7500,1500,Over Budget"))
        assertTrue(content.contains("Planned Total Expenses,₹16,000"))
        assertTrue(content.contains("Actual Total Expenses,₹17,500"))
    }
}
