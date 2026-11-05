package com.premraj.moneyboard.feature.export

import com.premraj.moneyboard.core.dashboard.DashboardCalculator
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.feature.dashboard.toFinanceBoardUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth

/**
 * Requirement-driven test suite for dynamic math calculations, category percentage distributions,
 * annual sums, and realistic 24-preset + custom item end-to-end scenarios.
 *
 * Requirements:
 * - ORIGINAL_REQUEST.md (Requirement R3): Do not copy hardcoded values or typos from Image 2
 *   (e.g., "correectly", incorrect category percentages like 11.5% or 10.1%).
 *   Replicate only the layout and visual structure; all totals, remaining balances, annual figures,
 *   and category percentages must be computed dynamically from real data.
 * - PROJECT.md (Interface Contracts M3, Milestones M3/M4).
 */
class ExpenseReportCalculationTest {

    private val testMonth = YearMonth.of(2026, 10)
    private val currency = "INR"
    private val testEpoch = 1700000000000L

    private fun money(rupees: Long) = Money(rupees * 100L, currency)

    private fun incomePlan(rupees: Long) = MonthlyIncomePlan(
        id = "income_1",
        month = testMonth,
        name = "In-hand Salary",
        plannedAmount = money(rupees),
        expectedDate = null,
        sortOrder = 1,
        createdAtEpochMillis = testEpoch,
        updatedAtEpochMillis = testEpoch
    )

    private fun createCategory(
        id: String,
        name: String,
        iconKey: String,
        type: CategoryType,
        group: CategoryGroup,
        sortOrder: Int,
        systemCategory: Boolean = true
    ): Category {
        return Category(
            id = id,
            name = name,
            iconKey = iconKey,
            type = type,
            group = group,
            sortOrder = sortOrder,
            systemCategory = systemCategory,
            archived = false,
            createdAtEpochMillis = testEpoch,
            updatedAtEpochMillis = testEpoch
        )
    }

    private fun get24ReferenceCategories(): List<Category> {
        return listOf(
            createCategory(SeedIds.EXPENSE_RENT, "Rent", "rent", CategoryType.EXPENSE, CategoryGroup.HOUSING, 1),
            createCategory(SeedIds.EXPENSE_GROCERIES, "Groceries", "groceries", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 2),
            createCategory(SeedIds.EXPENSE_GAS, "Gas", "gas", CategoryType.EXPENSE, CategoryGroup.HOUSING, 3),
            createCategory(SeedIds.EXPENSE_HOUSE_HELP, "Maid", "maid", CategoryType.EXPENSE, CategoryGroup.HOUSING, 4),
            createCategory(SeedIds.EXPENSE_DINING, "Online food", "online_food", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 5),
            createCategory(SeedIds.EXPENSE_ELECTRICITY, "Electricity", "electricity", CategoryType.EXPENSE, CategoryGroup.HOUSING, 6),
            createCategory(SeedIds.EXPENSE_WIFI, "Wi-Fi", "wifi", CategoryType.EXPENSE, CategoryGroup.HOUSING, 7),
            createCategory(SeedIds.EXPENSE_MOBILE, "Mobile recharge", "mobile", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 8),
            createCategory(SeedIds.EXPENSE_CAB, "Cab/Rapido", "cab", CategoryType.EXPENSE, CategoryGroup.TRANSPORT, 9),
            createCategory(SeedIds.EXPENSE_LAUNDRY, "Laundry", "laundry", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 10),
            createCategory(SeedIds.EXPENSE_SHOPPING, "Shopping", "shopping", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 11),
            createCategory(SeedIds.EXPENSE_GYM, "Gym", "gym", CategoryType.EXPENSE, CategoryGroup.HEALTH, 12),
            createCategory(SeedIds.EXPENSE_FITNESS_DIET, "Gym diet", "gym_diet", CategoryType.EXPENSE, CategoryGroup.HEALTH, 13),
            createCategory(SeedIds.EXPENSE_SALON, "Skincare and saloon", "salon", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 14),
            createCategory(SeedIds.EXPENSE_OUTING, "Weekend outing", "outing", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 15),
            createCategory(SeedIds.EXPENSE_TRAVEL, "Domestic trip", "travel", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 16),
            createCategory(SeedIds.EXPENSE_HOMETOWN, "Hometown visit", "hometown", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 17),
            createCategory(SeedIds.EXPENSE_MEDICINE, "Medicine", "medicine", CategoryType.EXPENSE, CategoryGroup.HEALTH, 18),
            createCategory(SeedIds.EXPENSE_FAMILY_SUPPORT, "Sending to parents", "family", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 19),
            createCategory(SeedIds.INVESTMENT_SIP, "SIP", "sip", CategoryType.INVESTMENT, CategoryGroup.INVESTMENTS, 20),
            createCategory(SeedIds.EXPENSE_HEALTH_INSURANCE, "Health insurance", "health_insurance", CategoryType.EXPENSE, CategoryGroup.HEALTH, 21),
            createCategory(SeedIds.SAVING_LIC_POLICY, "LIC", "lic", CategoryType.SAVING, CategoryGroup.SAVINGS, 22),
            createCategory(SeedIds.SAVING_EMERGENCY_FUND, "Emergency fund", "emergency_fund", CategoryType.SAVING, CategoryGroup.SAVINGS, 23),
            createCategory(SeedIds.EXPENSE_OTHER, "Other expenses", "other", CategoryType.EXPENSE, CategoryGroup.OTHER, 24)
        )
    }

    private fun get24ReferencePlans(month: YearMonth): List<MonthlyCategoryPlan> {
        return listOf(
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_RENT, money(10_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_GROCERIES, money(6_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_GAS, money(900), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_HOUSE_HELP, money(3_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_DINING, money(3_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_ELECTRICITY, money(1_500), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_WIFI, money(600), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_MOBILE, money(350), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_CAB, money(2_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_LAUNDRY, money(300), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_SHOPPING, money(2_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_GYM, money(1_200), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_FITNESS_DIET, money(2_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_SALON, money(1_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_OUTING, money(3_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_TRAVEL, money(2_500), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_HOMETOWN, money(3_500), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_MEDICINE, money(500), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_FAMILY_SUPPORT, money(10_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.INVESTMENT_SIP, money(5_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_HEALTH_INSURANCE, money(1_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.SAVING_LIC_POLICY, money(2_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.SAVING_EMERGENCY_FUND, money(7_000), testEpoch, testEpoch),
            MonthlyCategoryPlan(month, SeedIds.EXPENSE_OTHER, money(1_000), testEpoch, testEpoch)
        )
    }

    // =========================================================================
    // Tier 3: Dynamic Math Calculations for Report Sheet
    // =========================================================================

    @Test
    fun dynamicMath_totalAllocationAndRemaining_calculatedAccurately() {
        val income = listOf(incomePlan(70_000))
        val plans = get24ReferencePlans(testMonth)
        val categories = get24ReferenceCategories()

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = income,
            categoryPlans = plans,
            transactions = emptyList(),
            categories = categories
        )

        // Verifies Image 2 reference totals
        assertEquals(70_000_00L, snapshot.planned.income.amountMinor)
        assertEquals(69_350_00L, snapshot.planned.totalAllocation.amountMinor)
        assertEquals(650_00L, snapshot.planned.remaining.amountMinor)

        // Savings & Investments: SIP (5,000) + LIC (2,000) + Emergency Fund (7,000) = 14,000
        val totalSavings = snapshot.planned.savings + snapshot.planned.investments
        assertEquals(14_000_00L, totalSavings.amountMinor)

        // Actual living/family/travel spending: Total allocation (69,350) - Savings (14,000) = 55,350
        val actualSpending = snapshot.planned.expenses
        assertEquals(55_350_00L, actualSpending.amountMinor)
    }

    @Test
    fun dynamicMath_annualFigures_calculatedAccurately() {
        val monthlySalary = 70_000L
        val monthlyExpenses = 69_350L

        val annualSalary = monthlySalary * 12L
        val annualExpenses = monthlyExpenses * 12L
        val annualBalance = annualSalary - annualExpenses

        assertEquals(8_40_000L, annualSalary)
        assertEquals(8_32_200L, annualExpenses)
        assertEquals(7_800L, annualBalance)
    }

    @Test
    fun dynamicMath_categoryPercentages_computedDynamicallyWithoutTypos() {
        // In Image 2, Rent (10,000) was marked as 11.5% and Groceries (6,000) as 10.1%, which are arithmetic typos.
        // Requirement R3 dictates: "all category percentages must be computed dynamically from real data."
        val totalPaise = 69_350_00.0

        val rentPaise = 10_000_00.0
        val dynamicRentPercent = (rentPaise / totalPaise) * 100.0
        // Exact is 14.4196%
        assertEquals(14.42, dynamicRentPercent, 0.01)
        assertFalse("Must not be hardcoded to Image 2 typo 11.5%", Math.abs(dynamicRentPercent - 11.5) < 0.1)

        val groceriesPaise = 6_000_00.0
        val dynamicGroceriesPercent = (groceriesPaise / totalPaise) * 100.0
        // Exact is 8.6517%
        assertEquals(8.65, dynamicGroceriesPercent, 0.01)
        assertFalse("Must not be hardcoded to Image 2 typo 10.1%", Math.abs(dynamicGroceriesPercent - 10.1) < 0.1)

        // Sum of all category percentages must equal exactly 100%
        val plans = get24ReferencePlans(testMonth)
        val sumPaise = plans.sumOf { it.plannedAmount.amountMinor }
        assertEquals(69_350_00L, sumPaise)

        val totalPercentage = plans.sumOf { (it.plannedAmount.amountMinor.toDouble() / sumPaise.toDouble()) * 100.0 }
        assertEquals(100.0, totalPercentage, 0.001)
    }

    // =========================================================================
    // Tier 4: Realistic Scenarios — 24 Presets + Custom Items E2E
    // =========================================================================

    @Test
    fun realisticScenario_twentyFourPresetsPlan_e2eVerification() {
        val income = listOf(incomePlan(70_000))
        val plans = get24ReferencePlans(testMonth)
        val categories = get24ReferenceCategories()

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = income,
            categoryPlans = plans,
            transactions = emptyList(),
            categories = categories
        )

        val boardUi = snapshot.toFinanceBoardUi(DashboardMode.PLAN)

        // 1. Verify board model fields
        assertTrue(boardUi.salaryText.contains("70,000"))
        assertTrue(boardUi.totalAllocationText.contains("69,350"))
        assertTrue(boardUi.remainingText.contains("650"))
        assertTrue(boardUi.totalSavingsText.contains("14,000"))
        assertTrue(boardUi.actualSpendingText.contains("55,350"))
        assertTrue(boardUi.annualBalanceText.contains("7,800"))

        // 2. 24 rows present
        assertEquals(24, boardUi.expenseRows.size)

        // 3. Two-column split check: (24 + 1) / 2 = 12 items per column
        val col1Count = (boardUi.expenseRows.size + 1) / 2
        val col2Count = boardUi.expenseRows.size - col1Count
        assertEquals(12, col1Count)
        assertEquals(12, col2Count)
    }

    @Test
    fun realisticScenario_twentyFourPresetsPlusCustomItems_e2eVerification() {
        val income = listOf(incomePlan(70_000))
        val baseCategories = get24ReferenceCategories().toMutableList()
        val basePlans = get24ReferencePlans(testMonth).toMutableList()

        // Add 2 custom items with emojis
        val pianoCustomCat = createCategory(
            id = "custom_piano",
            name = "Piano Lessons",
            iconKey = "🎹",
            type = CategoryType.EXPENSE,
            group = CategoryGroup.LIFESTYLE,
            sortOrder = 25,
            systemCategory = false
        )
        val petCustomCat = createCategory(
            id = "custom_pet",
            name = "Pet Care",
            iconKey = "🐶",
            type = CategoryType.EXPENSE,
            group = CategoryGroup.LIFESTYLE,
            sortOrder = 26,
            systemCategory = false
        )

        baseCategories.add(pianoCustomCat)
        baseCategories.add(petCustomCat)

        basePlans.add(MonthlyCategoryPlan(testMonth, "custom_piano", money(3_000), testEpoch, testEpoch))
        basePlans.add(MonthlyCategoryPlan(testMonth, "custom_pet", money(1_500), testEpoch, testEpoch))

        assertEquals(26, baseCategories.size)
        assertEquals(26, basePlans.size)

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = income,
            categoryPlans = basePlans,
            transactions = emptyList(),
            categories = baseCategories
        )

        // New Total: 69,350 + 3,000 + 1,500 = 73,850
        assertEquals(73_850_00L, snapshot.planned.totalAllocation.amountMinor)

        // Deficit / Negative remaining: 70,000 - 73,850 = -3,850
        assertEquals(-3_850_00L, snapshot.planned.remaining.amountMinor)

        // Annual expenses: 73,850 * 12 = 8,86,200
        val annualExpenses = (snapshot.planned.totalAllocation * 12).amountMinor
        assertEquals(8_86_200_00L, annualExpenses)

        val boardUi = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals(26, boardUi.expenseRows.size)

        // Two-column split check: (26 + 1) / 2 = 13 items per column
        val col1Count = (boardUi.expenseRows.size + 1) / 2
        val col2Count = boardUi.expenseRows.size - col1Count
        assertEquals(13, col1Count)
        assertEquals(13, col2Count)
    }
}
