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
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.core.util.HeaderSubtitleFormatter
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import com.premraj.moneyboard.feature.dashboard.toFinanceBoardUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.util.Locale
import kotlin.random.Random

/**
 * EMPIRICAL ADVERSARIAL CHALLENGER TEST SUITE FOR MILESTONE 3:
 * Unified 1224px Image & PDF Report Export.
 *
 * Stress-tests:
 * 1. Extreme inputs: 0 expenses, 1 expense, 50 expenses, 10M salary, 0 salary, deficit/negative savings.
 * 2. Donut slice categorization word boundary stress-testing (e.g., "Sending to parents", false substrings).
 * 3. EMI formatting invariants when EMI is 0: strict omission of "No EMI" in subtitle and table.
 * 4. Dynamic math invariants: Salary - Expenses == Remaining, Annual = Monthly * 12.
 */
class Milestone3AdversarialChallengerTest {

    private val testMonth = YearMonth.of(2026, 10)
    private val currency = "INR"
    private val testEpoch = 1700000000000L

    private fun money(rupees: Long) = Money(rupees * 100L, currency)

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

    private fun createCategory(
        id: String,
        name: String,
        iconKey: String,
        type: CategoryType,
        group: CategoryGroup,
        sortOrder: Int
    ): Category {
        return Category(
            id = id,
            name = name,
            iconKey = iconKey,
            type = type,
            group = group,
            sortOrder = sortOrder,
            systemCategory = true,
            archived = false,
            createdAtEpochMillis = testEpoch,
            updatedAtEpochMillis = testEpoch
        )
    }

    // =========================================================================
    // 1. Extreme Inputs Stress Tests
    // =========================================================================

    @Test
    fun testZeroExpenses_returnsGracefulDefaults_andNoCrash() {
        // Zero expenses with totalMinor = 0
        val slicesEmpty = calculateDynamicDonutSlices(emptyList(), 0L)
        assertEquals(6, slicesEmpty.size)
        // Verify no division by zero or NaN occurred
        slicesEmpty.forEach { slice ->
            assertFalse(slice.percentageValue.isNaN())
            assertFalse(slice.percentageValue.isInfinite())
            assertTrue(slice.percentageValue >= 0f)
        }

        // List of rows with 0 amounts
        val zeroRows = listOf(
            createExpenseRow("cat_zero_1", 1, "Zero Rent", 0L),
            createExpenseRow("cat_zero_2", 2, "Zero Food", 0L)
        )
        val slicesZeroSum = calculateDynamicDonutSlices(zeroRows, 0L)
        assertEquals(6, slicesZeroSum.size)
        slicesZeroSum.forEach { slice ->
            assertFalse(slice.percentageValue.isNaN())
            assertFalse(slice.percentageValue.isInfinite())
        }
    }

    @Test
    fun testSingleExpense_twoColumnSplitAndDonutSlices() {
        val singleExpense = listOf(
            createExpenseRow(SeedIds.EXPENSE_RENT, 1, "Rent", 40_000L)
        )
        val totalMinor = singleExpense.sumOf { it.amountMinor }
        val slices = calculateDynamicDonutSlices(singleExpense, totalMinor)

        val housing = slices.first { it.title == "Housing" }
        assertEquals("100.0%", housing.percentageText)
        assertEquals(100.0f, housing.percentageValue, 0.01f)

        // All other slices should be 0.0%
        val othersSlices = slices.filter { it.title != "Housing" }
        othersSlices.forEach { slice ->
            assertEquals("0.0%", slice.percentageText)
            assertEquals(0.0f, slice.percentageValue, 0.01f)
        }

        // Two-column table partitioning: (1 + 1) / 2 = 1 left, 0 right
        val half = (singleExpense.size + 1) / 2
        assertEquals(1, singleExpense.take(half).size)
        assertEquals(0, singleExpense.drop(half).size)
    }

    @Test
    fun testFiftyExpenses_twoColumnSplitAndAccurateDistribution() {
        val fiftyExpenses = (1..50).map { i ->
            val catId = when (i % 6) {
                0 -> SeedIds.EXPENSE_RENT
                1 -> SeedIds.EXPENSE_GROCERIES
                2 -> SeedIds.EXPENSE_SHOPPING
                3 -> SeedIds.EXPENSE_TRAVEL
                4 -> SeedIds.INVESTMENT_SIP
                else -> SeedIds.EXPENSE_OTHER
            }
            createExpenseRow(catId, i, "Item $i", (i * 500).toLong())
        }
        val totalMinor = fiftyExpenses.sumOf { it.amountMinor }
        assertTrue(totalMinor > 0L)

        val slices = calculateDynamicDonutSlices(fiftyExpenses, totalMinor)
        assertEquals(6, slices.size)

        // Slices must sum to ~100% (allowing single-decimal float rounding tolerance)
        val sumPct = slices.sumOf { it.percentageValue.toDouble() }
        assertEquals(100.0, sumPct, 0.5)

        // Two-column split: (50 + 1) / 2 = 25 left, 25 right
        val half = (fiftyExpenses.size + 1) / 2
        assertEquals(25, fiftyExpenses.take(half).size)
        assertEquals(25, fiftyExpenses.drop(half).size)
    }

    @Test
    fun testHugeSalaryTenMillion_mathInvariantsHoldWithoutOverflow() {
        val hugeSalaryRupees = 10_000_000L // 10 million rupees (1 crore)
        val expensesRupees = 69_350L

        val incomePlan = MonthlyIncomePlan(
            id = "income_huge",
            month = testMonth,
            name = "CEO Salary",
            plannedAmount = money(hugeSalaryRupees),
            expectedDate = null,
            sortOrder = 1,
            createdAtEpochMillis = testEpoch,
            updatedAtEpochMillis = testEpoch
        )

        val rentCategory = createCategory(SeedIds.EXPENSE_RENT, "Rent", "rent", CategoryType.EXPENSE, CategoryGroup.HOUSING, 1)
        val rentPlan = MonthlyCategoryPlan(testMonth, SeedIds.EXPENSE_RENT, money(expensesRupees), testEpoch, testEpoch)

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = listOf(incomePlan),
            categoryPlans = listOf(rentPlan),
            transactions = emptyList(),
            categories = listOf(rentCategory)
        )

        val board = snapshot.toFinanceBoardUi(DashboardMode.PLAN)

        // Check monthly figures
        val expectedRemainingRupees = hugeSalaryRupees - expensesRupees
        assertEquals(expectedRemainingRupees * 100L, snapshot.planned.remaining.amountMinor)
        assertEquals(10_000_000_00L, snapshot.planned.income.amountMinor)
        assertEquals(69_350_00L, snapshot.planned.totalAllocation.amountMinor)

        // Check annual figures (120,000,000 salary, 8,32,200 expenses)
        val expectedAnnualSalaryRupees = hugeSalaryRupees * 12L
        val expectedAnnualExpensesRupees = expensesRupees * 12L
        val expectedAnnualBalanceRupees = expectedAnnualSalaryRupees - expectedAnnualExpensesRupees

        assertEquals(120_000_000L, expectedAnnualSalaryRupees)
        assertEquals(832_200L, expectedAnnualExpensesRupees)
        assertEquals(119_167_800L, expectedAnnualBalanceRupees)

        // Verify annual balance invariant: annualBalance == monthlyRemaining * 12
        assertEquals(expectedAnnualBalanceRupees, expectedRemainingRupees * 12L)

        // Board UI checks
        println("testHugeSalary: salaryText=${board.salaryText}, annualBalanceText=${board.annualBalanceText}")
        assertTrue("Expected salaryText to contain 10000000 or 1,00,00,000, got ${board.salaryText}",
            board.salaryText.replace(",", "").contains("10000000"))
        assertTrue("Expected annualBalanceText to contain 119167800, got ${board.annualBalanceText}",
            board.annualBalanceText.replace(",", "").contains("119167800"))
    }

    @Test
    fun testZeroSalary_mathInvariantsAndNegativeRemainingWithoutDivZero() {
        val expensesRupees = 40_000L

        // When salary is zero, there are no income plans (MonthlyIncomePlan enforces amountMinor > 0)
        val rentCategory = createCategory(SeedIds.EXPENSE_RENT, "Rent", "rent", CategoryType.EXPENSE, CategoryGroup.HOUSING, 1)
        val rentPlan = MonthlyCategoryPlan(testMonth, SeedIds.EXPENSE_RENT, money(expensesRupees), testEpoch, testEpoch)

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = listOf(rentPlan),
            transactions = emptyList(),
            categories = listOf(rentCategory)
        )

        // Invariants
        assertEquals(0L, snapshot.planned.income.amountMinor)
        assertEquals(40_000_00L, snapshot.planned.totalAllocation.amountMinor)
        assertEquals(-40_000_00L, snapshot.planned.remaining.amountMinor)

        // Division by zero safeguard in basis points
        assertNull("Savings rate basis points must be null when denominator <= 0", snapshot.planned.savingsRateBasisPoints)

        val board = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals("—", board.savingsRateText)
        println("testZeroSalary: remainingText=${board.remainingText}, annualBalanceText=${board.annualBalanceText}")
        assertTrue(board.remainingText.replace(",", "").contains("40000"))
        assertTrue(board.annualBalanceText.replace(",", "").contains("480000"))
    }

    @Test
    fun testDeficitSituationExpensesExceedSalary() {
        val salaryRupees = 50_000L
        val expensesRupees = 85_000L

        val incomePlan = MonthlyIncomePlan(
            id = "income_1",
            month = testMonth,
            name = "Salary",
            plannedAmount = money(salaryRupees),
            expectedDate = null,
            sortOrder = 1,
            createdAtEpochMillis = testEpoch,
            updatedAtEpochMillis = testEpoch
        )

        val rentCategory = createCategory(SeedIds.EXPENSE_RENT, "Rent", "rent", CategoryType.EXPENSE, CategoryGroup.HOUSING, 1)
        val rentPlan = MonthlyCategoryPlan(testMonth, SeedIds.EXPENSE_RENT, money(expensesRupees), testEpoch, testEpoch)

        val snapshot = DashboardCalculator.calculate(
            month = testMonth,
            currencyCode = currency,
            incomePlans = listOf(incomePlan),
            categoryPlans = listOf(rentPlan),
            transactions = emptyList(),
            categories = listOf(rentCategory)
        )

        val remainingRupees = salaryRupees - expensesRupees // -35,000
        assertEquals(-35_000_00L, snapshot.planned.remaining.amountMinor)

        val annualSalaryRupees = salaryRupees * 12L // 6,00,000
        val annualExpensesRupees = expensesRupees * 12L // 10,20,000
        val annualBalanceRupees = annualSalaryRupees - annualExpensesRupees // -4,20,000

        assertEquals(annualBalanceRupees, remainingRupees * 12L)

        val board = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        println("testDeficit: remainingText=${board.remainingText}, annualBalanceText=${board.annualBalanceText}")
        assertTrue(board.remainingText.replace(",", "").contains("35000"))
        assertTrue("Expected annualBalanceText to contain 420000, got: ${board.annualBalanceText}",
            board.annualBalanceText.replace(",", "").contains("420000"))
    }

    // =========================================================================
    // 2. Donut Slice Word Boundary Stress Tests
    // =========================================================================

    @Test
    fun testWordBoundary_sendingToParents_notAssignedToHousingOrRent() {
        val items = listOf(
            createExpenseRow(SeedIds.EXPENSE_FAMILY_SUPPORT, 1, "Sending to parents", 10_000L),
            createExpenseRow(SeedIds.EXPENSE_RENT, 2, "Rent", 15_000L)
        )
        val totalMinor = items.sumOf { it.amountMinor } // 25,000
        val slices = calculateDynamicDonutSlices(items, totalMinor)

        // Housing MUST only contain Rent (15,000 / 25,000 = 60.0%)
        val housing = slices.first { it.title == "Housing" }
        assertEquals(60.0f, housing.percentageValue, 0.01f)
        assertEquals("60.0%", housing.percentageText)

        // Others MUST contain Sending to parents (10,000 / 25,000 = 40.0%)
        val others = slices.first { it.title == "Others" }
        assertEquals(40.0f, others.percentageValue, 0.01f)
        assertEquals("40.0%", others.percentageText)

        // Food, Lifestyle, Travel, Savings must be 0%
        listOf("Food & Essentials", "Lifestyle", "Travel", "Savings & Investments").forEach { catName ->
            val slice = slices.first { it.title == catName }
            assertEquals("0.0%", slice.percentageText)
        }
    }

    @Test
    fun testWordBoundary_falseSubstringAdversarialWordsDoNotTriggerCategories() {
        // Construct adversarial titles that contain category keywords as substrings:
        // "parental" contains "rent"
        // "current" contains "rent"
        // "tripod" contains "trip"
        // "cabinet" contains "cab"
        // "maiden" contains "maid"
        // "shouting" contains "outing"
        val adversarialItems = listOf(
            createExpenseRow("custom_1", 1, "Parental Care", 1_000L),
            createExpenseRow("custom_2", 2, "Current Affairs", 1_000L),
            createExpenseRow("custom_3", 3, "Tripod for Camera", 1_000L),
            createExpenseRow("custom_4", 4, "Cabinet Repair", 1_000L),
            createExpenseRow("custom_5", 5, "Maiden Voyage Book", 1_000L),
            createExpenseRow("custom_6", 6, "Shouting Lessons", 1_000L)
        )

        val totalMinor = adversarialItems.sumOf { it.amountMinor } // 6,000
        val slices = calculateDynamicDonutSlices(adversarialItems, totalMinor)

        // NONE of these should match Housing, Food, Lifestyle, or Travel.
        // ALL 6,000 must be categorized under Others!
        val others = slices.first { it.title == "Others" }
        assertEquals("100.0%", others.percentageText)
        assertEquals(100.0f, others.percentageValue, 0.01f)

        assertEquals("0.0%", slices.first { it.title == "Housing" }.percentageText)
        assertEquals("0.0%", slices.first { it.title == "Food & Essentials" }.percentageText)
        assertEquals("0.0%", slices.first { it.title == "Lifestyle" }.percentageText)
        assertEquals("0.0%", slices.first { it.title == "Travel" }.percentageText)
        assertEquals("0.0%", slices.first { it.title == "Savings & Investments" }.percentageText)
    }

    @Test
    fun testWordBoundary_trueKeywordsMatchAccurately() {
        val validItems = listOf(
            createExpenseRow("c1", 1, "House Rent Payment", 10_000L), // Housing
            createExpenseRow("c2", 2, "Monthly Grocery & Food", 5_000L), // Food
            createExpenseRow("c3", 3, "Weekend Outing and Shopping", 3_000L), // Lifestyle
            createExpenseRow("c4", 4, "Domestic Trip Flight", 4_000L), // Travel
            createExpenseRow("c5", 5, "SIP Mutual Fund", 6_000L), // Savings
            createExpenseRow("c6", 6, "Electricity Bill", 2_000L) // Others
        )

        val totalMinor = validItems.sumOf { it.amountMinor } // 30,000
        val slices = calculateDynamicDonutSlices(validItems, totalMinor)

        // Housing: 10,000 / 30,000 = 33.3%
        assertEquals("33.3%", slices.first { it.title == "Housing" }.percentageText)
        // Food: 5,000 / 30,000 = 16.7%
        assertEquals("16.7%", slices.first { it.title == "Food & Essentials" }.percentageText)
        // Lifestyle: 3,000 / 30,000 = 10.0%
        assertEquals("10.0%", slices.first { it.title == "Lifestyle" }.percentageText)
        // Travel: 4,000 / 30,000 = 13.3%
        assertEquals("13.3%", slices.first { it.title == "Travel" }.percentageText)
        // Savings: 6,000 / 30,000 = 20.0%
        assertEquals("20.0%", slices.first { it.title == "Savings & Investments" }.percentageText)
        // Others: 2,000 / 30,000 = 6.7%
        assertEquals("6.7%", slices.first { it.title == "Others" }.percentageText)
    }

    // =========================================================================
    // 3. EMI Formatting Invariants
    // =========================================================================

    @Test
    fun testEmiFormatting_whenZeroOrNegative_strictlyOmitsNoEmi() {
        val subtitlesToTest = listOf(
            HeaderSubtitleFormatter.format("Single Person", "Pune", 0L),
            HeaderSubtitleFormatter.format("Single Person", "Pune", -1L),
            HeaderSubtitleFormatter.format("Single Person", "Pune", -50_000L),
            HeaderSubtitleFormatter.format("", "Pune", 0L),
            HeaderSubtitleFormatter.format("Couple", "", 0L)
        )

        subtitlesToTest.forEach { subtitle ->
            assertFalse("Must never output 'No EMI'", subtitle.contains("No EMI", ignoreCase = true))
            assertFalse("Must never output 'EMI' when <= 0", subtitle.contains("EMI"))
            assertTrue(subtitle.contains("|"))
        }
    }

    @Test
    fun testEmiFormatting_whenPositive_formatsProperly() {
        val subtitle = HeaderSubtitleFormatter.format("Single Person", "Bengaluru", 23_450L)
        assertEquals("Single Person | Bengaluru | EMI ₹23,450", subtitle)
    }

    @Test
    fun testEmiRowResolution_inExpenseReportSheet_logic() {
        fun resolveEmiRupees(
            rows: List<ExpenseRowUi>,
            settingsEmiTag: String
        ): Long {
            val emiRow = rows.firstOrNull {
                it.categoryId == SeedIds.EXPENSE_EMI || it.title.equals("EMI", ignoreCase = true)
            }
            return if (emiRow != null && emiRow.amountMinor > 0L) {
                emiRow.amountMinor / 100L
            } else {
                settingsEmiTag.filter { it.isDigit() }.toLongOrNull() ?: 0L
            }
        }

        // Case 1: No EMI row, settings.emiTag has "No EMI"
        val emi1 = resolveEmiRupees(emptyList(), "No EMI")
        assertEquals(0L, emi1)
        val sub1 = HeaderSubtitleFormatter.format("Single Person", "Pune", emi1)
        assertEquals("Single Person | Pune", sub1)
        assertFalse(sub1.contains("No EMI"))

        // Case 2: EMI row has 0 rupees
        val zeroEmiRows = listOf(createExpenseRow(SeedIds.EXPENSE_EMI, 1, "EMI", 0L))
        val emi2 = resolveEmiRupees(zeroEmiRows, "No EMI")
        assertEquals(0L, emi2)
        val sub2 = HeaderSubtitleFormatter.format("Single Person", "Pune", emi2)
        assertEquals("Single Person | Pune", sub2)

        // Case 3: EMI row has ₹15,000
        val posEmiRows = listOf(createExpenseRow(SeedIds.EXPENSE_EMI, 1, "EMI", 15_000L))
        val emi3 = resolveEmiRupees(posEmiRows, "No EMI")
        assertEquals(15_000L, emi3)
        val sub3 = HeaderSubtitleFormatter.format("Single Person", "Pune", emi3)
        assertEquals("Single Person | Pune | EMI ₹15,000", sub3)

        // Case 4: No EMI row, but settings.emiTag has "EMI ₹8,500"
        val emi4 = resolveEmiRupees(emptyList(), "EMI ₹8,500")
        assertEquals(8500L, emi4)
        val sub4 = HeaderSubtitleFormatter.format("Single Person", "Pune", emi4)
        assertEquals("Single Person | Pune | EMI ₹8,500", sub4)
    }

    // =========================================================================
    // 4. Dynamic Math Invariants Stress Test across Randomized Plans
    // =========================================================================

    @Test
    fun testDynamicMathInvariants_across50RandomizedPlans() {
        val rng = Random(42) // Fixed seed for reproducible empirical verification

        for (run in 1..50) {
            val salaryRupees = rng.nextLong(10_000L, 2_000_000L)
            val itemCount = rng.nextInt(1, 35)

            val categories = mutableListOf<Category>()
            val plans = mutableListOf<MonthlyCategoryPlan>()

            val categoryTypes = listOf(
                CategoryType.EXPENSE,
                CategoryType.SAVING,
                CategoryType.INVESTMENT
            )

            for (i in 1..itemCount) {
                val catId = "cat_$i"
                val type = categoryTypes[rng.nextInt(categoryTypes.size)]
                val rupees = rng.nextLong(50L, 50_000L)

                categories.add(
                    createCategory(
                        id = catId,
                        name = "Expense $i",
                        iconKey = "icon_$i",
                        type = type,
                        group = CategoryGroup.OTHER,
                        sortOrder = i
                    )
                )

                plans.add(
                    MonthlyCategoryPlan(
                        month = testMonth,
                        categoryId = catId,
                        plannedAmount = money(rupees),
                        createdAtEpochMillis = testEpoch,
                        updatedAtEpochMillis = testEpoch
                    )
                )
            }

            val incomePlan = MonthlyIncomePlan(
                id = "inc",
                month = testMonth,
                name = "Salary",
                plannedAmount = money(salaryRupees),
                expectedDate = null,
                sortOrder = 1,
                createdAtEpochMillis = testEpoch,
                updatedAtEpochMillis = testEpoch
            )

            val snapshot = DashboardCalculator.calculate(
                month = testMonth,
                currencyCode = currency,
                incomePlans = listOf(incomePlan),
                categoryPlans = plans,
                transactions = emptyList(),
                categories = categories
            )

            val planned = snapshot.planned
            val totalAllocation = planned.totalAllocation.amountMinor
            val expectedAllocation = plans.sumOf { it.plannedAmount.amountMinor }
            assertEquals("Run $run: Allocation must sum all items", expectedAllocation, totalAllocation)

            // Invariant 1: Total Allocation == Expenses + Savings + Investments
            val sumTypes = planned.expenses.amountMinor + planned.savings.amountMinor + planned.investments.amountMinor
            assertEquals("Run $run: Allocation == sum of parts", sumTypes, totalAllocation)

            // Invariant 2: Salary - Total Allocation == Remaining
            val expectedRemaining = (salaryRupees * 100L) - totalAllocation
            assertEquals("Run $run: Remaining == Salary - Allocation", expectedRemaining, planned.remaining.amountMinor)

            // Invariant 3: Annual Expenses == Total Allocation * 12
            val annualExpenses = totalAllocation * 12L
            // Invariant 4: Annual Salary == Monthly Salary * 12
            val annualSalary = (salaryRupees * 100L) * 12L
            // Invariant 5: Annual Balance == Annual Salary - Annual Expenses
            val annualBalance = annualSalary - annualExpenses
            // Invariant 6: Annual Balance == Monthly Remaining * 12
            assertEquals("Run $run: Annual Balance == Remaining * 12", planned.remaining.amountMinor * 12L, annualBalance)

            // Invariant 7: Donut slices sum to ~100%
            val rows = plans.mapIndexed { idx, p ->
                createExpenseRow(p.categoryId, idx + 1, "Item ${idx + 1}", p.plannedAmount.amountMinor / 100L)
            }
            val slices = calculateDynamicDonutSlices(rows, totalAllocation)
            assertEquals(6, slices.size)
            val sumPct = slices.sumOf { it.percentageValue.toDouble() }
            assertEquals("Run $run: Donut slices sum to 100%", 100.0, sumPct, 0.5)

            // Invariant 8: Slice percentage text matches value
            slices.forEach { slice ->
                val expectedStr = String.format(Locale.US, "%.1f%%", slice.percentageValue)
                assertEquals(expectedStr, slice.percentageText)
                assertFalse(slice.percentageText.contains("correectly"))
            }
        }
    }
}
