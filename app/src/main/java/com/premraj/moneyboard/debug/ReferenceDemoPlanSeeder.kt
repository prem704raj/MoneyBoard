package com.premraj.moneyboard.debug

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import java.time.YearMonth

class ReferenceDemoPlanSeeder(
    private val monthlyPlanRepository: MonthlyPlanRepository
) {
    suspend fun seedIfEmpty(month: YearMonth) {
        if (monthlyPlanRepository.hasAnyPlan(month)) return

        monthlyPlanRepository.setIncomePlan(
            SetIncomePlanCommand(
                id = "debug.salary.${month.year}.${month.monthValue}",
                month = month,
                name = "In-hand Salary",
                plannedAmount = inr(70_000),
                expectedDate = month.atDay(1),
                sortOrder = 10
            )
        )

        val plans = listOf(
            SeedIds.EXPENSE_RENT to 10_000L,
            SeedIds.EXPENSE_GROCERIES to 6_000L,
            SeedIds.EXPENSE_GAS to 800L,
            SeedIds.EXPENSE_HOUSE_HELP to 2_500L,
            SeedIds.EXPENSE_DINING to 3_000L,
            SeedIds.EXPENSE_ELECTRICITY to 1_500L,
            SeedIds.EXPENSE_WATER to 500L,
            SeedIds.EXPENSE_WIFI to 600L,
            SeedIds.EXPENSE_MOBILE to 350L,
            SeedIds.EXPENSE_FUEL to 2_000L,
            SeedIds.EXPENSE_CAB to 2_000L,
            SeedIds.EXPENSE_LAUNDRY to 800L,
            SeedIds.EXPENSE_SHOPPING to 2_000L,
            SeedIds.EXPENSE_GYM to 1_200L,
            SeedIds.EXPENSE_FITNESS_DIET to 2_000L,
            SeedIds.EXPENSE_SALON to 1_000L,
            SeedIds.EXPENSE_OUTING to 2_000L,
            SeedIds.EXPENSE_TRAVEL to 2_500L,
            SeedIds.EXPENSE_HOMETOWN to 3_500L,
            SeedIds.EXPENSE_MEDICINE to 500L,
            SeedIds.EXPENSE_FAMILY_SUPPORT to 8_000L,
            SeedIds.EXPENSE_EDUCATION to 1_000L,
            SeedIds.EXPENSE_SUBSCRIPTIONS to 600L,
            SeedIds.EXPENSE_OTHER to 1_000L,

            SeedIds.SAVING_EMERGENCY_FUND to 7_000L,
            SeedIds.SAVING_LIC_POLICY to 2_000L,

            SeedIds.INVESTMENT_SIP to 5_000L
        )

        plans.forEach { (categoryId, rupees) ->
            monthlyPlanRepository.setCategoryPlan(
                SetCategoryPlanCommand(
                    month = month,
                    categoryId = categoryId,
                    plannedAmount = inr(rupees)
                )
            )
        }
    }

    private fun inr(rupees: Long): Money {
        return Money(
            amountMinor = Math.multiplyExact(rupees, 100L),
            currencyCode = "INR"
        )
    }
}
