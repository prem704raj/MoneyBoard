package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.dashboard.DashboardCalculator
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

/**
 * Empirical Adversarial Challenger Test Suite for Milestone 5.
 * Focus areas:
 * 1. Month date projection edge cases (`withMonth(targetMonth)`):
 *    - Leap years (Feb 29 -> Feb 28 on non-leap years, Feb 29 on leap years, century leap rule)
 *    - 31-day to 30-day transitions (Oct 31 -> Nov 30, Jan 31 -> Feb 28, etc.)
 *    - Large forward and backward leaps (+36 months, -36 months, +120 months, -120 months)
 *    - Null expectedDate preservation
 *    - MonthlyIncomePlanEntity.toDomain() clamping on legacy outlier dates
 * 2. Zero amount plans:
 *    - MonthlyCategoryPlan with plannedAmount = 0L accepted; negative rejected; blank ID rejected
 *    - MonthlyIncomePlan with plannedAmount = 0L accepted; negative rejected; blank name rejected
 *    - DashboardCalculator.calculate with all or some expenses ₹0:
 *      * remaining = income - totalPlanned
 *      * division-by-zero safety in savingsRateBasisPoints
 *      * deterministic sort orders (sortOrder ASC, name ASC)
 *      * defensive category lookup (mapNotNull resiliency)
 * 3. Actual mode with zero transactions:
 *    - Plans present, 0 transactions: active categories generated with amount = 0, expenses = 0
 *    - Zero plans & zero transactions: falls back to active categories (excluding archived and income)
 *    - Completely empty categories: toFinanceBoardUi returns emptyList(), never leaks DashboardPreviewData
 *    - Contrast with PLAN mode: empty plan falls back to DashboardPreviewData as designed
 */
class Milestone5AdversarialChallengerTest {

    private val currency = "INR"

    private fun createCategory(
        id: String,
        name: String,
        type: CategoryType = CategoryType.EXPENSE,
        sortOrder: Int = 10,
        archived: Boolean = false
    ): Category = Category(
        id = id,
        name = name,
        iconKey = "ic_$id",
        type = type,
        group = CategoryGroup.LIFESTYLE,
        sortOrder = sortOrder,
        systemCategory = true,
        archived = archived,
        createdAtEpochMillis = 1000L,
        updatedAtEpochMillis = 1000L
    )

    // =========================================================================
    // FOCUS AREA 1: MONTH DATE PROJECTION EDGE CASES
    // =========================================================================

    @Test
    fun monthDateProjection_leapYear_feb29ToNonLeapYearFeb28() {
        val leapYearMonth = YearMonth.of(2024, 2)
        val leapDay = LocalDate.of(2024, 2, 29)

        val plan = MonthlyIncomePlan(
            id = "plan-leap",
            month = leapYearMonth,
            name = "Salary",
            plannedAmount = Money(5_000_000L, currency),
            expectedDate = leapDay,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        // Project to 2025-02 (non-leap year: 28 days)
        val proj2025 = plan.withMonth(YearMonth.of(2025, 2))
        assertEquals(YearMonth.of(2025, 2), proj2025.month)
        assertEquals(LocalDate.of(2025, 2, 28), proj2025.expectedDate)
        assertEquals(YearMonth.of(2025, 2), YearMonth.from(proj2025.expectedDate!!))

        // Project to 2028-02 (leap year: 29 days) -> preserves day 29
        val proj2028 = plan.withMonth(YearMonth.of(2028, 2))
        assertEquals(YearMonth.of(2028, 2), proj2028.month)
        assertEquals(LocalDate.of(2028, 2, 29), proj2028.expectedDate)
        assertEquals(YearMonth.of(2028, 2), YearMonth.from(proj2028.expectedDate!!))

        // Project to 2100-02 (century non-leap year: 28 days)
        val proj2100 = plan.withMonth(YearMonth.of(2100, 2))
        assertEquals(LocalDate.of(2100, 2, 28), proj2100.expectedDate)

        // Project to 2000-02 (century leap year: 29 days)
        val proj2000 = plan.withMonth(YearMonth.of(2000, 2))
        assertEquals(LocalDate.of(2000, 2, 29), proj2000.expectedDate)
    }

    @Test
    fun monthDateProjection_31DayTo30DayMonths_clampedCorrectly() {
        val octPlan = MonthlyIncomePlan(
            id = "plan-31",
            month = YearMonth.of(2026, 10),
            name = "Salary",
            plannedAmount = Money(5_000_000L, currency),
            expectedDate = LocalDate.of(2026, 10, 31),
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        // Oct 31 -> Nov (30 days) -> Nov 30
        val novPlan = octPlan.withMonth(YearMonth.of(2026, 11))
        assertEquals(LocalDate.of(2026, 11, 30), novPlan.expectedDate)
        assertEquals(YearMonth.of(2026, 11), YearMonth.from(novPlan.expectedDate!!))

        // Oct 31 -> Feb 2026 (28 days) -> Feb 28
        val febPlan = octPlan.withMonth(YearMonth.of(2026, 2))
        assertEquals(LocalDate.of(2026, 2, 28), febPlan.expectedDate)

        // Oct 31 -> Apr (30 days) -> Apr 30
        val aprPlan = octPlan.withMonth(YearMonth.of(2026, 4))
        assertEquals(LocalDate.of(2026, 4, 30), aprPlan.expectedDate)

        // Oct 31 -> Jun (30 days) -> Jun 30
        val junPlan = octPlan.withMonth(YearMonth.of(2026, 6))
        assertEquals(LocalDate.of(2026, 6, 30), junPlan.expectedDate)

        // Oct 31 -> Sep (30 days) -> Sep 30
        val sepPlan = octPlan.withMonth(YearMonth.of(2026, 9))
        assertEquals(LocalDate.of(2026, 9, 30), sepPlan.expectedDate)

        // Oct 31 -> Dec (31 days) -> Dec 31
        val decPlan = octPlan.withMonth(YearMonth.of(2026, 12))
        assertEquals(LocalDate.of(2026, 12, 31), decPlan.expectedDate)
    }

    @Test
    fun monthDateProjection_largeForwardAndBackwardLeaps_invariantsPreserved() {
        val startMonth = YearMonth.of(2026, 1)
        val startDate = LocalDate.of(2026, 1, 31)

        val plan = MonthlyIncomePlan(
            id = "plan-leap-test",
            month = startMonth,
            name = "Salary",
            plannedAmount = Money(5_000_000L, currency),
            expectedDate = startDate,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        val leapOffsetsMonths = listOf(
            36L, -36L, // 3 years forward and backward
            48L, -48L, // 4 years forward and backward
            120L, -120L, // 10 years forward and backward
            360L, -360L // 30 years forward and backward
        )

        for (offset in leapOffsetsMonths) {
            val targetMonth = startMonth.plusMonths(offset)
            val projected = plan.withMonth(targetMonth)

            assertEquals("Target month must match", targetMonth, projected.month)
            assertNotNull("expectedDate must not be null", projected.expectedDate)
            assertEquals(
                "YearMonth of expectedDate must strictly equal targetMonth for offset $offset",
                targetMonth,
                YearMonth.from(projected.expectedDate!!)
            )
            val expectedDay = startDate.dayOfMonth.coerceAtMost(targetMonth.lengthOfMonth())
            assertEquals(
                "Day of month must be clamped to month length",
                expectedDay,
                projected.expectedDate.dayOfMonth
            )
        }
    }

    @Test
    fun monthDateProjection_nullExpectedDate_preservedAcrossMonths() {
        val plan = MonthlyIncomePlan(
            id = "plan-null-date",
            month = YearMonth.of(2026, 5),
            name = "Freelance",
            plannedAmount = Money(2_000_000L, currency),
            expectedDate = null,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        val projected = plan.withMonth(YearMonth.of(2026, 11))
        assertEquals(YearMonth.of(2026, 11), projected.month)
        assertNull(projected.expectedDate)
    }

    @Test
    fun monthDateProjection_entityToDomainClamping_legacyDatabaseOutliers() {
        // Outlier 1: entity has monthKey = 202611 (30 days), but expectedDateEpochDay is 31st
        val oct31EpochDay = LocalDate.of(2026, 10, 31).toEpochDay()
        val outlierEntity1 = MonthlyIncomePlanEntity(
            id = "outlier-1",
            monthKey = 202611,
            name = "Salary",
            plannedAmountMinor = 5_000_000L,
            currencyCode = "INR",
            expectedDateEpochDay = oct31EpochDay,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        val domain1 = outlierEntity1.toDomain()
        assertEquals(YearMonth.of(2026, 11), domain1.month)
        assertEquals(LocalDate.of(2026, 11, 30), domain1.expectedDate)
        assertEquals(YearMonth.of(2026, 11), YearMonth.from(domain1.expectedDate!!))

        // Outlier 2: entity has monthKey = 202502 (28 days), but expectedDateEpochDay is leap day 2024-02-29
        val leapDayEpochDay = LocalDate.of(2024, 2, 29).toEpochDay()
        val outlierEntity2 = MonthlyIncomePlanEntity(
            id = "outlier-2",
            monthKey = 202502,
            name = "Salary",
            plannedAmountMinor = 5_000_000L,
            currencyCode = "INR",
            expectedDateEpochDay = leapDayEpochDay,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )

        val domain2 = outlierEntity2.toDomain()
        assertEquals(YearMonth.of(2025, 2), domain2.month)
        assertEquals(LocalDate.of(2025, 2, 28), domain2.expectedDate)
        assertEquals(YearMonth.of(2025, 2), YearMonth.from(domain2.expectedDate!!))

        // Outlier 3: null expectedDateEpochDay
        val outlierEntity3 = outlierEntity1.copy(expectedDateEpochDay = null)
        val domain3 = outlierEntity3.toDomain()
        assertNull(domain3.expectedDate)
    }

    // =========================================================================
    // FOCUS AREA 2: ZERO AMOUNT PLANS & ARITHMETIC SAFETY
    // =========================================================================

    @Test
    fun zeroAmountPlans_monthlyCategoryPlan_zeroAllowedAndNegativeRejected() {
        val month = YearMonth.of(2026, 10)

        // 1. Zero amount accepted
        val zeroPlan = MonthlyCategoryPlan(
            month = month,
            categoryId = "rent",
            plannedAmount = Money(0L, currency),
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        assertEquals(0L, zeroPlan.plannedAmount.amountMinor)

        // 2. Positive amount accepted
        val posPlan = MonthlyCategoryPlan(
            month = month,
            categoryId = "rent",
            plannedAmount = Money(15_000_00L, currency),
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        assertEquals(15_000_00L, posPlan.plannedAmount.amountMinor)

        // 3. Negative amount throws IllegalArgumentException
        var threwNegative = false
        try {
            MonthlyCategoryPlan(
                month = month,
                categoryId = "rent",
                plannedAmount = Money(-1L, currency),
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
        } catch (e: IllegalArgumentException) {
            threwNegative = true
        }
        assertTrue("Negative planned amount must throw IllegalArgumentException", threwNegative)

        // 4. Blank categoryId throws IllegalArgumentException
        var threwBlankCategory = false
        try {
            MonthlyCategoryPlan(
                month = month,
                categoryId = "   ",
                plannedAmount = Money(0L, currency),
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
        } catch (e: IllegalArgumentException) {
            threwBlankCategory = true
        }
        assertTrue("Blank categoryId must throw IllegalArgumentException", threwBlankCategory)
    }

    @Test
    fun zeroAmountPlans_monthlyIncomePlan_zeroAllowedAndNegativeRejected() {
        val month = YearMonth.of(2026, 10)

        // 1. Zero amount accepted
        val zeroIncome = MonthlyIncomePlan(
            id = "inc-0",
            month = month,
            name = "Stipend",
            plannedAmount = Money(0L, currency),
            expectedDate = null,
            sortOrder = 1,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        assertEquals(0L, zeroIncome.plannedAmount.amountMinor)

        // 2. Negative amount throws
        var threwNegative = false
        try {
            MonthlyIncomePlan(
                id = "inc-neg",
                month = month,
                name = "Salary",
                plannedAmount = Money(-500L, currency),
                expectedDate = null,
                sortOrder = 1,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
        } catch (e: IllegalArgumentException) {
            threwNegative = true
        }
        assertTrue("Negative planned amount must throw IllegalArgumentException", threwNegative)

        // 3. Blank name throws
        var threwBlankName = false
        try {
            MonthlyIncomePlan(
                id = "inc-blank",
                month = month,
                name = "",
                plannedAmount = Money(0L, currency),
                expectedDate = null,
                sortOrder = 1,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
        } catch (e: IllegalArgumentException) {
            threwBlankName = true
        }
        assertTrue("Blank income name must throw IllegalArgumentException", threwBlankName)
    }

    @Test
    fun zeroAmountPlans_dashboardCalculator_allExpensesZero() {
        val month = YearMonth.of(2026, 10)
        val incomeAmount = Money(5_000_000L, currency) // ₹50,000

        val incomePlans = listOf(
            MonthlyIncomePlan(
                id = "inc-1",
                month = month,
                name = "Salary",
                plannedAmount = incomeAmount,
                expectedDate = null,
                sortOrder = 1,
                createdAtEpochMillis = 1000L,
                updatedAtEpochMillis = 1000L
            )
        )

        val cat1 = createCategory("rent", "Rent", CategoryType.EXPENSE, sortOrder = 1)
        val cat2 = createCategory("groceries", "Groceries", CategoryType.EXPENSE, sortOrder = 2)
        val cat3 = createCategory("wifi", "Wi-Fi", CategoryType.EXPENSE, sortOrder = 3)
        val categories = listOf(cat1, cat2, cat3)

        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "rent", Money(0L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "groceries", Money(0L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "wifi", Money(0L, currency), 1000L, 1000L)
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = categories
        )

        // Check metrics:
        assertEquals(incomeAmount, snapshot.planned.income)
        assertEquals(Money(0L, currency), snapshot.planned.expenses)
        assertEquals(Money(0L, currency), snapshot.planned.savings)
        assertEquals(Money(0L, currency), snapshot.planned.investments)
        assertEquals(Money(0L, currency), snapshot.planned.totalAllocation)
        // remaining = income - totalPlanned
        assertEquals(incomeAmount, snapshot.planned.remaining)
        // savings rate basis points = 0 (saved = 0, income > 0)
        assertEquals(0, snapshot.planned.savingsRateBasisPoints)

        // Verify all 3 ₹0 planned lines exist in plannedCategoryLines
        assertEquals(3, snapshot.plannedCategoryLines.size)
        assertEquals(listOf("rent", "groceries", "wifi"), snapshot.plannedCategoryLines.map { it.categoryId })
        assertTrue(snapshot.plannedCategoryLines.all { it.amount.amountMinor == 0L })

        // Check UI model mapping
        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals(3, uiModel.expenseRows.size)
        assertEquals("Rent", uiModel.expenseRows[0].title)
        assertEquals("0", uiModel.expenseRows[0].amountText)
        assertEquals("₹50,000 / month", uiModel.remainingText)
        assertEquals("0.00%", uiModel.savingsRateText)
    }

    @Test
    fun zeroAmountPlans_dashboardCalculator_someExpensesZeroSomePositive() {
        val month = YearMonth.of(2026, 10)
        val incomeAmount = Money(10_000_000L, currency) // ₹1,00,000

        val incomePlans = listOf(
            MonthlyIncomePlan("inc-1", month, "Salary", incomeAmount, null, 1, 1000L, 1000L)
        )

        val rentCat = createCategory("rent", "Rent", CategoryType.EXPENSE, sortOrder = 1)
        val grocCat = createCategory("groceries", "Groceries", CategoryType.EXPENSE, sortOrder = 2)
        val elecCat = createCategory("electricity", "Electricity", CategoryType.EXPENSE, sortOrder = 3)
        val sipCat = createCategory("sip", "Mutual Fund SIP", CategoryType.INVESTMENT, sortOrder = 4)
        val categories = listOf(rentCat, grocCat, elecCat, sipCat)

        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "rent", Money(3_000_000L, currency), 1000L, 1000L), // ₹30,000
            MonthlyCategoryPlan(month, "groceries", Money(0L, currency), 1000L, 1000L), // ₹0
            MonthlyCategoryPlan(month, "electricity", Money(200_000L, currency), 1000L, 1000L), // ₹2,000
            MonthlyCategoryPlan(month, "sip", Money(1_000_000L, currency), 1000L, 1000L) // ₹10,000
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = categories
        )

        assertEquals(Money(3_200_000L, currency), snapshot.planned.expenses) // 30k + 2k
        assertEquals(Money(1_000_000L, currency), snapshot.planned.investments) // 10k
        assertEquals(Money(4_200_000L, currency), snapshot.planned.totalAllocation) // 42k
        // remaining = 100k - 42k = 58k
        assertEquals(Money(5_800_000L, currency), snapshot.planned.remaining)
        // savings rate = 10k / 100k = 10.00% = 1000 basis points
        assertEquals(1000, snapshot.planned.savingsRateBasisPoints)

        // All 4 lines preserved
        assertEquals(4, snapshot.plannedCategoryLines.size)
        val grocLine = snapshot.plannedCategoryLines.first { it.categoryId == "groceries" }
        assertEquals(0L, grocLine.amount.amountMinor)
    }

    @Test
    fun zeroAmountPlans_dashboardCalculator_zeroIncomeDivisionByZeroSafety() {
        val month = YearMonth.of(2026, 10)
        val zeroIncome = Money(0L, currency)

        val incomePlans = listOf(
            MonthlyIncomePlan("inc-0", month, "No Income", zeroIncome, null, 1, 1000L, 1000L)
        )

        val cat = createCategory("rent", "Rent", CategoryType.EXPENSE, sortOrder = 1)
        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "rent", Money(0L, currency), 1000L, 1000L)
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = listOf(cat)
        )

        assertEquals(zeroIncome, snapshot.planned.income)
        assertEquals(zeroIncome, snapshot.planned.expenses)
        assertEquals(zeroIncome, snapshot.planned.totalAllocation)
        assertEquals(zeroIncome, snapshot.planned.remaining)
        // When denominator <= 0, savingsRateBasisPoints MUST be null (no division by zero crash)
        assertNull(snapshot.planned.savingsRateBasisPoints)

        // UI mapping formatting safety
        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals("—", uiModel.savingsRateText)
        assertEquals("₹0 / month", uiModel.remainingText)
    }

    @Test
    fun zeroAmountPlans_dashboardCalculator_zeroIncomePositiveExpensesDeficit() {
        val month = YearMonth.of(2026, 10)
        val zeroIncome = Money(0L, currency)

        val cat = createCategory("rent", "Rent", CategoryType.EXPENSE, sortOrder = 1)
        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "rent", Money(1_500_000L, currency), 1000L, 1000L) // ₹15,000
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = listOf(cat)
        )

        assertEquals(zeroIncome, snapshot.planned.income)
        assertEquals(Money(1_500_000L, currency), snapshot.planned.expenses)
        // remaining = 0 - 15,000 = -15,000
        assertEquals(Money(-1_500_000L, currency), snapshot.planned.remaining)
        assertNull(snapshot.planned.savingsRateBasisPoints)

        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals("₹-15,000 / month", uiModel.remainingText)
        assertEquals("—", uiModel.savingsRateText)
    }

    @Test
    fun zeroAmountPlans_dashboardCalculator_sortOrderPreservedWithZeroAmounts() {
        val month = YearMonth.of(2026, 10)
        val catA = createCategory("cat-a", "Zebra", CategoryType.EXPENSE, sortOrder = 20)
        val catB = createCategory("cat-b", "Apple", CategoryType.EXPENSE, sortOrder = 20)
        val catC = createCategory("cat-c", "Banana", CategoryType.EXPENSE, sortOrder = 5)
        val catD = createCategory("cat-d", "Mango", CategoryType.EXPENSE, sortOrder = 50)

        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "cat-a", Money(0L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "cat-b", Money(50_000L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "cat-c", Money(0L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "cat-d", Money(100_000L, currency), 1000L, 1000L)
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = listOf(catA, catB, catC, catD)
        )

        // Expected sort order:
        // 1. cat-c (sortOrder 5)
        // 2. cat-b (sortOrder 20, name "Apple")
        // 3. cat-a (sortOrder 20, name "Zebra")
        // 4. cat-d (sortOrder 50)
        val orderedIds = snapshot.plannedCategoryLines.map { it.categoryId }
        assertEquals(listOf("cat-c", "cat-b", "cat-a", "cat-d"), orderedIds)
    }

    @Test
    fun defensiveMapping_missingCategorySkippedWithoutException() {
        val month = YearMonth.of(2026, 10)
        val existingCat = createCategory("cat-1", "Existing", CategoryType.EXPENSE, sortOrder = 1)

        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "cat-1", Money(50_000L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "ghost-cat", Money(20_000L, currency), 1000L, 1000L) // Not in categories
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = listOf(existingCat)
        )

        // ghost-cat gracefully ignored by mapNotNull
        assertEquals(1, snapshot.plannedCategoryLines.size)
        assertEquals("cat-1", snapshot.plannedCategoryLines[0].categoryId)
    }

    // =========================================================================
    // FOCUS AREA 3: ACTUAL MODE WITH ZERO TRANSACTIONS & MOCK LEAKAGE PREVENTION
    // =========================================================================

    @Test
    fun actualMode_plansExistZeroTransactions_allActiveCategoriesWithZeroAmount() {
        val month = YearMonth.of(2026, 10)
        val income = Money(6_000_000L, currency) // ₹60,000

        val incomePlans = listOf(
            MonthlyIncomePlan("inc-1", month, "Salary", income, null, 1, 1000L, 1000L)
        )

        val cat1 = createCategory("rent", "Rent", CategoryType.EXPENSE, sortOrder = 1)
        val cat2 = createCategory("groceries", "Groceries", CategoryType.EXPENSE, sortOrder = 2)

        val categoryPlans = listOf(
            MonthlyCategoryPlan(month, "rent", Money(2_000_000L, currency), 1000L, 1000L),
            MonthlyCategoryPlan(month, "groceries", Money(500_000L, currency), 1000L, 1000L)
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(), // ZERO TRANSACTIONS
            categories = listOf(cat1, cat2)
        )

        // In Actual mode:
        // Truthful accounting (P0-D): when zero actual income transactions exist, actual received income is ₹0, not fake planned income
        assertEquals(Money(0L, currency), snapshot.actual.income)
        // Expenses must be ₹0
        assertEquals(Money(0L, currency), snapshot.actual.expenses)
        assertEquals(Money(0L, currency), snapshot.actual.totalAllocation)
        // Remaining = 0 - 0 = 0
        assertEquals(Money(0L, currency), snapshot.actual.remaining)

        // All planned categories exist in actualCategoryLines with amount 0
        assertEquals(2, snapshot.actualCategoryLines.size)
        assertTrue(snapshot.actualCategoryLines.all { it.amount.amountMinor == 0L })

        // Verify toFinanceBoardUi DOES NOT leak mock preview data
        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.ACTUAL)
        assertFalse(
            "Actual mode expenseRows must NOT equal mock preview data",
            uiModel.expenseRows == DashboardPreviewData.expenses
        )
        assertEquals(2, uiModel.expenseRows.size)
        assertEquals("Rent", uiModel.expenseRows[0].title)
        assertEquals("0", uiModel.expenseRows[0].amountText)
        assertEquals("₹0 / month", uiModel.actualSpendingText)
        assertEquals("₹0 / month", uiModel.remainingText)
    }

    @Test
    fun actualMode_zeroPlansZeroTransactions_fallsBackToActiveCategoriesWithZeroSpend() {
        val month = YearMonth.of(2026, 10)

        val activeExpense = createCategory("active-exp", "Active Expense", CategoryType.EXPENSE, sortOrder = 1)
        val activeSaving = createCategory("active-sav", "Active Saving", CategoryType.SAVING, sortOrder = 2)
        val activeInvest = createCategory("active-inv", "Active Investment", CategoryType.INVESTMENT, sortOrder = 3)
        val archivedExpense = createCategory("arch-exp", "Archived Expense", CategoryType.EXPENSE, sortOrder = 4, archived = true)
        val activeIncome = createCategory("active-inc", "Active Income", CategoryType.INCOME, sortOrder = 5)

        val allCategories = listOf(activeExpense, activeSaving, activeInvest, archivedExpense, activeIncome)

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = emptyList(),
            categories = allCategories
        )

        // Actual mode lines should contain the 3 active non-income categories, excluding archived and income
        assertEquals(3, snapshot.actualCategoryLines.size)
        val lineIds = snapshot.actualCategoryLines.map { it.categoryId }
        assertTrue(lineIds.contains("active-exp"))
        assertTrue(lineIds.contains("active-sav"))
        assertTrue(lineIds.contains("active-inv"))
        assertFalse(lineIds.contains("arch-exp"))
        assertFalse(lineIds.contains("active-inc"))

        // All amounts must be 0
        assertTrue(snapshot.actualCategoryLines.all { it.amount.amountMinor == 0L })
        assertEquals(Money(0L, currency), snapshot.actual.expenses)

        // UI model check:
        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.ACTUAL)
        assertFalse(
            "Actual mode expenseRows must NOT equal mock preview data",
            uiModel.expenseRows == DashboardPreviewData.expenses
        )
        assertEquals(3, uiModel.expenseRows.size)
        assertEquals("₹0 / month", uiModel.actualSpendingText)
    }

    @Test
    fun actualMode_completelyEmptyCategories_toFinanceBoardUiReturnsEmptyListNotMockData() {
        val month = YearMonth.of(2026, 10)

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = emptyList(),
            categories = emptyList() // No categories at all
        )

        assertTrue(snapshot.actualCategoryLines.isEmpty())

        val actualUi = snapshot.toFinanceBoardUi(DashboardMode.ACTUAL)
        // MUST be emptyList(), NOT DashboardPreviewData.expenses!
        assertTrue(
            "When completely empty, actual mode expenseRows must be emptyList()",
            actualUi.expenseRows.isEmpty()
        )
        assertTrue(
            "When completely empty, actual mode savings must be emptyList()",
            actualUi.savings.isEmpty()
        )
        assertFalse(actualUi.expenseRows == DashboardPreviewData.expenses)
        assertFalse(actualUi.savings == DashboardPreviewData.savings)

        // Contrast: in PLAN mode, empty plan deliberately yields preview mock data for onboarding
        val planUi = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals(DashboardPreviewData.expenses, planUi.expenseRows)
        assertEquals(DashboardPreviewData.savings, planUi.savings)
    }

    @Test
    fun actualMode_withTransactions_sumsAmountsCorrectly() {
        val month = YearMonth.of(2026, 10)
        val cat = createCategory("dining", "Dining", CategoryType.EXPENSE, sortOrder = 1)

        val tx1 = FinanceTransaction(
            id = "tx-1",
            accountId = "acc-1",
            categoryId = "dining",
            type = TransactionType.EXPENSE,
            amount = Money(45_000L, currency), // ₹450
            accountingDate = LocalDate.of(2026, 10, 5),
            occurredAt = Instant.now(),
            note = null,
            merchant = "Swiggy",
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L,
            deletedAtEpochMillis = null
        )

        val tx2 = FinanceTransaction(
            id = "tx-2",
            accountId = "acc-1",
            categoryId = "dining",
            type = TransactionType.EXPENSE,
            amount = Money(55_000L, currency), // ₹550
            accountingDate = LocalDate.of(2026, 10, 12),
            occurredAt = Instant.now(),
            note = null,
            merchant = "Zomato",
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L,
            deletedAtEpochMillis = null
        )

        // Soft-deleted transaction must be ignored
        val txDeleted = tx2.copy(
            id = "tx-del",
            amount = Money(100_000L, currency),
            deletedAtEpochMillis = 2000L
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = listOf(tx1, tx2, txDeleted),
            categories = listOf(cat)
        )

        assertEquals(1, snapshot.actualCategoryLines.size)
        assertEquals(Money(100_000L, currency), snapshot.actualCategoryLines[0].amount) // ₹1,000
        assertEquals(Money(100_000L, currency), snapshot.actual.expenses)

        val uiModel = snapshot.toFinanceBoardUi(DashboardMode.ACTUAL)
        assertEquals(1, uiModel.expenseRows.size)
        assertEquals("1,000", uiModel.expenseRows[0].amountText)
    }

    // =========================================================================
    // FOCUS AREA 4: DAO INHERITANCE ALGORITHM & CALENDAR DISTANCE FORMULA
    // =========================================================================

    /**
     * Simulates the exact SQL COALESCE inheritance query used in MonthlyPlanDao:
     * WHERE monthKey = COALESCE(
     *     (SELECT monthKey FROM monthly_category_plans WHERE monthKey = :target LIMIT 1),
     *     (SELECT monthKey FROM monthly_category_plans WHERE monthKey <= :target ORDER BY monthKey DESC LIMIT 1),
     *     (SELECT monthKey FROM monthly_category_plans ORDER BY ABS(((monthKey / 100) * 12 + (monthKey % 100)) - ((:target / 100) * 12 + (:target % 100))) ASC LIMIT 1)
     * )
     */
    private fun resolveInheritedMonthKey(existingKeys: List<Int>, targetKey: Int): Int? {
        if (existingKeys.isEmpty()) return null

        // 1. Exact match
        val exact = existingKeys.firstOrNull { it == targetKey }
        if (exact != null) return exact

        // 2. Latest plan <= targetKey
        val latestPrior = existingKeys.filter { it <= targetKey }.maxOrNull()
        if (latestPrior != null) return latestPrior

        // 3. Closest plan by calendar month distance
        fun calendarMonths(key: Int): Int = (key / 100) * 12 + (key % 100)
        val targetMonths = calendarMonths(targetKey)

        return existingKeys.minByOrNull { key ->
            kotlin.math.abs(calendarMonths(key) - targetMonths)
        }
    }

    @Test
    fun daoInheritance_sqlDistanceFormulaAndCoalesceHierarchy_verified() {
        // Test 1: Single existing plan (202610 - October 2026)
        val singlePlan = listOf(202610)

        // Exact match
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202610))

        // Future months inherit latest plan
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202611))
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202701))
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 203012))

        // Past months before the plan inherit the closest plan
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202609))
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202601))
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202512))
        assertEquals(202610, resolveInheritedMonthKey(singlePlan, 202001))

        // Test 2: Multiple plans (202603 - March 2026, 202610 - October 2026)
        val multiPlans = listOf(202603, 202610)

        // Past months before earliest plan (e.g. 202601 -> distance to 202603 is 2, to 202610 is 9)
        assertEquals(202603, resolveInheritedMonthKey(multiPlans, 202601))
        assertEquals(202603, resolveInheritedMonthKey(multiPlans, 202512))

        // Between plans: 202605 inherits 202603 (latest prior)
        assertEquals(202603, resolveInheritedMonthKey(multiPlans, 202605))
        assertEquals(202603, resolveInheritedMonthKey(multiPlans, 202609))

        // Exact matches
        assertEquals(202603, resolveInheritedMonthKey(multiPlans, 202603))
        assertEquals(202610, resolveInheritedMonthKey(multiPlans, 202610))

        // Future months inherit latest (202610)
        assertEquals(202610, resolveInheritedMonthKey(multiPlans, 202611))
        assertEquals(202610, resolveInheritedMonthKey(multiPlans, 202701))

        // Test 3: Calendar month distance across year boundary
        fun dist(k1: Int, k2: Int): Int {
            fun m(k: Int) = (k / 100) * 12 + (k % 100)
            return kotlin.math.abs(m(k1) - m(k2))
        }
        assertEquals(1, dist(202612, 202701))
        assertEquals(2, dist(202611, 202701))
        assertEquals(12, dist(202605, 202705))
    }
}

