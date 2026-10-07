package com.premraj.moneyboard.core.dashboard

import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class DashboardCalculatorTest {

    private val month = YearMonth.of(2026, 9)
    private val currency = "INR"

    private val salaryCategory = category(
        "cat-salary", "Salary", CategoryType.INCOME, CategoryGroup.INCOME, 10
    )
    private val rentCategory = category(
        "cat-rent", "Rent", CategoryType.EXPENSE, CategoryGroup.HOUSING, 100
    )
    private val groceriesCategory = category(
        "cat-groceries", "Groceries", CategoryType.EXPENSE, CategoryGroup.FOOD, 110
    )
    private val emergencyFundCategory = category(
        "cat-emergency", "Emergency Fund", CategoryType.SAVING, CategoryGroup.SAVINGS, 400
    )
    private val sipCategory = category(
        "cat-sip", "SIP", CategoryType.INVESTMENT, CategoryGroup.INVESTMENTS, 500
    )

    private val allCategories = listOf(
        salaryCategory, rentCategory, groceriesCategory,
        emergencyFundCategory, sipCategory
    )

    // --- Normal month with plan and transactions ---

    @Test
    fun normalMonth_calculatesCorrectPlannedMetrics() {
        val incomePlans = listOf(
            incomePlan("In-hand Salary", inr(70_000_00))
        )
        val categoryPlans = listOf(
            categoryPlan(rentCategory.id, inr(10_000_00)),
            categoryPlan(groceriesCategory.id, inr(6_000_00)),
            categoryPlan(emergencyFundCategory.id, inr(7_000_00)),
            categoryPlan(sipCategory.id, inr(5_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = allCategories
        )

        val planned = snapshot.planned
        assertEquals(70_000_00L, planned.income.amountMinor)
        assertEquals(16_000_00L, planned.expenses.amountMinor)  // rent + groceries
        assertEquals(7_000_00L, planned.savings.amountMinor)
        assertEquals(5_000_00L, planned.investments.amountMinor)
        assertEquals(28_000_00L, planned.totalAllocation.amountMinor) // 16000+7000+5000
        assertEquals(42_000_00L, planned.remaining.amountMinor) // 70000-28000
        assertTrue(snapshot.hasPlan)
    }

    @Test
    fun normalMonth_calculatesCorrectActualMetrics() {
        val transactions = listOf(
            transaction("tx1", salaryCategory.id, TransactionType.INCOME, inr(68_000_00)),
            transaction("tx2", rentCategory.id, TransactionType.EXPENSE, inr(10_000_00)),
            transaction("tx3", groceriesCategory.id, TransactionType.EXPENSE, inr(5_500_00)),
            transaction("tx4", emergencyFundCategory.id, TransactionType.SAVING, inr(7_000_00)),
            transaction("tx5", sipCategory.id, TransactionType.INVESTMENT, inr(5_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = transactions,
            categories = allCategories
        )

        val actual = snapshot.actual
        assertEquals(68_000_00L, actual.income.amountMinor)
        assertEquals(15_500_00L, actual.expenses.amountMinor)
        assertEquals(7_000_00L, actual.savings.amountMinor)
        assertEquals(5_000_00L, actual.investments.amountMinor)
        assertEquals(27_500_00L, actual.totalAllocation.amountMinor)
        assertEquals(40_500_00L, actual.remaining.amountMinor)
        assertTrue(snapshot.hasActualActivity)
    }

    // --- Overspending (negative remaining) ---

    @Test
    fun overspending_producesNegativeRemaining() {
        val incomePlans = listOf(
            incomePlan("Salary", inr(50_000_00))
        )
        val categoryPlans = listOf(
            categoryPlan(rentCategory.id, inr(30_000_00)),
            categoryPlan(groceriesCategory.id, inr(15_000_00)),
            categoryPlan(emergencyFundCategory.id, inr(10_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = allCategories
        )

        assertEquals(-5_000_00L, snapshot.planned.remaining.amountMinor)
    }

    // --- Zero income ---

    @Test
    fun zeroIncome_savingsRateIsNull() {
        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = emptyList(),
            categories = allCategories
        )

        assertNull(snapshot.planned.savingsRateBasisPoints)
        assertNull(snapshot.actual.savingsRateBasisPoints)
    }

    // --- No plan, no activity ---

    @Test
    fun emptyMonth_flagsNoPlanNoActivity() {
        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = emptyList(),
            categories = allCategories
        )

        assertFalse(snapshot.hasPlan)
        assertFalse(snapshot.hasActualActivity)
        assertEquals(0L, snapshot.planned.income.amountMinor)
        assertEquals(0L, snapshot.actual.income.amountMinor)
    }

    // --- Deleted transactions excluded ---

    @Test
    fun deletedTransactions_areExcludedFromActuals() {
        val transactions = listOf(
            transaction("tx1", rentCategory.id, TransactionType.EXPENSE, inr(10_000_00)),
            transaction(
                "tx2", groceriesCategory.id, TransactionType.EXPENSE, inr(5_000_00),
                deletedAt = 999L
            )
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = transactions,
            categories = allCategories
        )

        assertEquals(10_000_00L, snapshot.actual.expenses.amountMinor)
    }

    // --- Wrong month transactions excluded ---

    @Test
    fun wrongMonth_transactionsAreExcluded() {
        val transactions = listOf(
            transaction("tx1", rentCategory.id, TransactionType.EXPENSE, inr(10_000_00)),
            transaction(
                "tx2", groceriesCategory.id, TransactionType.EXPENSE, inr(5_000_00),
                date = LocalDate.of(2026, 10, 1) // next month
            )
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = transactions,
            categories = allCategories
        )

        assertEquals(10_000_00L, snapshot.actual.expenses.amountMinor)
    }

    // --- Wrong currency excluded ---

    @Test
    fun wrongCurrency_transactionsAreExcluded() {
        val usdCategory = category(
            "cat-usd", "USD Expense", CategoryType.EXPENSE, CategoryGroup.OTHER, 999
        )
        val transactions = listOf(
            transaction("tx1", rentCategory.id, TransactionType.EXPENSE, inr(10_000_00)),
            transaction(
                "tx2", usdCategory.id, TransactionType.EXPENSE,
                Money(500_00L, "USD")
            )
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = emptyList(),
            transactions = transactions,
            categories = allCategories + usdCategory
        )

        assertEquals(10_000_00L, snapshot.actual.expenses.amountMinor)
    }

    // --- Savings rate calculation ---

    @Test
    fun savingsRate_isCorrectInBasisPoints() {
        val incomePlans = listOf(
            incomePlan("Salary", inr(100_000_00))
        )
        val categoryPlans = listOf(
            categoryPlan(emergencyFundCategory.id, inr(10_000_00)),
            categoryPlan(sipCategory.id, inr(10_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = allCategories
        )

        // saved = 10000 + 10000 = 20000, income = 100000 → 20%
        assertEquals(2000, snapshot.planned.savingsRateBasisPoints)
    }

    // --- Category lines sorted correctly ---

    @Test
    fun categoryLines_areSortedBySortOrderThenName() {
        val categoryPlans = listOf(
            categoryPlan(groceriesCategory.id, inr(6_000_00)),
            categoryPlan(rentCategory.id, inr(10_000_00)),
            categoryPlan(emergencyFundCategory.id, inr(7_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = emptyList(),
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = allCategories
        )

        val names = snapshot.plannedCategoryLines.map { it.name }
        assertEquals(listOf("Rent", "Groceries", "Emergency Fund"), names)
    }

    // --- Annualization ---

    @Test
    fun annualized_multipliesBy12() {
        val incomePlans = listOf(
            incomePlan("Salary", inr(70_000_00))
        )
        val categoryPlans = listOf(
            categoryPlan(rentCategory.id, inr(10_000_00))
        )

        val snapshot = DashboardCalculator.calculate(
            month = month,
            currencyCode = currency,
            incomePlans = incomePlans,
            categoryPlans = categoryPlans,
            transactions = emptyList(),
            categories = allCategories
        )

        val annual = snapshot.planned.annualized()
        assertEquals(840_000_00L, annual.income.amountMinor)
        assertEquals(120_000_00L, annual.expenses.amountMinor)
        assertEquals(720_000_00L, annual.remaining.amountMinor)
    }

    // --- Helpers ---

    private fun inr(minor: Long) = Money(minor, currency)

    private fun category(
        id: String, name: String, type: CategoryType,
        group: CategoryGroup, sortOrder: Int
    ) = Category(
        id = id, name = name, iconKey = "test",
        type = type, group = group, sortOrder = sortOrder,
        systemCategory = true, archived = false,
        createdAtEpochMillis = 1L, updatedAtEpochMillis = 1L
    )

    private fun incomePlan(name: String, amount: Money) = MonthlyIncomePlan(
        id = "plan-$name",
        month = month,
        name = name,
        plannedAmount = amount,
        expectedDate = null,
        sortOrder = 10,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L
    )

    private fun categoryPlan(categoryId: String, amount: Money) = MonthlyCategoryPlan(
        month = month,
        categoryId = categoryId,
        plannedAmount = amount,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L
    )

    private fun transaction(
        id: String,
        categoryId: String,
        type: TransactionType,
        amount: Money,
        date: LocalDate = LocalDate.of(2026, 9, 15),
        deletedAt: Long? = null
    ) = FinanceTransaction(
        id = id,
        accountId = "account-test",
        categoryId = categoryId,
        type = type,
        amount = amount,
        accountingDate = date,
        occurredAt = Instant.ofEpochMilli(1_757_900_000_000L),
        note = null,
        merchant = null,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
        deletedAtEpochMillis = deletedAt
    )
}
