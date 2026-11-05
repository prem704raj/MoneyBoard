package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds

object DashboardPreviewData {

    val expenses = listOf(
        // Left Column (Essential Expenses): Items 1 to 12
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_RENT,
            number = 1,
            title = "Rent",
            amountText = "10,000",
            amountMinor = 10_000_00L,
            icon = categoryBadge("rent").icon,
            iconDrawableRes = R.drawable.ic_rent,
            badgeColor = categoryBadge("rent").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GROCERIES,
            number = 2,
            title = "Groceries",
            amountText = "6,000",
            amountMinor = 6_000_00L,
            icon = categoryBadge("groceries").icon,
            iconDrawableRes = R.drawable.ic_groceries,
            badgeColor = categoryBadge("groceries").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GAS,
            number = 3,
            title = "Gas",
            amountText = "900",
            amountMinor = 900_00L,
            icon = categoryBadge("gas").icon,
            iconDrawableRes = R.drawable.ic_gas,
            badgeColor = categoryBadge("gas").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HOUSE_HELP,
            number = 4,
            title = "Maid",
            amountText = "3,000",
            amountMinor = 3_000_00L,
            icon = categoryBadge("maid").icon,
            iconDrawableRes = R.drawable.ic_maid,
            badgeColor = categoryBadge("maid").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_DINING,
            number = 5,
            title = "Online food",
            amountText = "3,000",
            amountMinor = 3_000_00L,
            icon = categoryBadge("online_food").icon,
            iconDrawableRes = R.drawable.ic_online_food,
            badgeColor = categoryBadge("online_food").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_ELECTRICITY,
            number = 6,
            title = "Electricity",
            amountText = "1,500",
            amountMinor = 1_500_00L,
            icon = categoryBadge("electricity").icon,
            iconDrawableRes = R.drawable.ic_electricity,
            badgeColor = categoryBadge("electricity").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_WIFI,
            number = 7,
            title = "Wi-Fi",
            amountText = "600",
            amountMinor = 600_00L,
            icon = categoryBadge("wifi").icon,
            iconDrawableRes = R.drawable.ic_wifi,
            badgeColor = categoryBadge("wifi").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_MOBILE,
            number = 8,
            title = "Mobile recharge",
            amountText = "350",
            amountMinor = 350_00L,
            icon = categoryBadge("mobile").icon,
            iconDrawableRes = R.drawable.ic_mobile_recharge,
            badgeColor = categoryBadge("mobile").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_CAB,
            number = 9,
            title = "Cab/Rapido",
            amountText = "2,000",
            amountMinor = 2_000_00L,
            icon = categoryBadge("cab").icon,
            iconDrawableRes = R.drawable.ic_cab_rapido,
            badgeColor = categoryBadge("cab").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_LAUNDRY,
            number = 10,
            title = "Laundry",
            amountText = "300",
            amountMinor = 300_00L,
            icon = categoryBadge("laundry").icon,
            iconDrawableRes = R.drawable.ic_laundry,
            badgeColor = categoryBadge("laundry").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_SHOPPING,
            number = 11,
            title = "Shopping",
            amountText = "2,000",
            amountMinor = 2_000_00L,
            icon = categoryBadge("shopping").icon,
            iconDrawableRes = R.drawable.ic_shopping,
            badgeColor = categoryBadge("shopping").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GYM,
            number = 12,
            title = "Gym",
            amountText = "1,200",
            amountMinor = 1_200_00L,
            icon = categoryBadge("gym").icon,
            iconDrawableRes = R.drawable.ic_gym,
            badgeColor = categoryBadge("gym").backgroundColor,
            isEssential = true
        ),

        // Right Column (Other Expenses): Items 13 to 24
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_FITNESS_DIET,
            number = 13,
            title = "Gym diet",
            amountText = "2,000",
            amountMinor = 2_000_00L,
            icon = categoryBadge("gym_diet").icon,
            iconDrawableRes = R.drawable.ic_gym_diet,
            badgeColor = categoryBadge("gym_diet").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_SALON,
            number = 14,
            title = "Skincare and saloon",
            amountText = "1,000",
            amountMinor = 1_000_00L,
            icon = categoryBadge("salon").icon,
            iconDrawableRes = R.drawable.ic_skincare_saloon,
            badgeColor = categoryBadge("salon").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_OUTING,
            number = 15,
            title = "Weekend outing",
            amountText = "3,000",
            amountMinor = 3_000_00L,
            icon = categoryBadge("outing").icon,
            iconDrawableRes = R.drawable.ic_weekend_outing,
            badgeColor = categoryBadge("outing").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_TRAVEL,
            number = 16,
            title = "Yearly one Domestic trip\n(30,000 ÷ 12)",
            amountText = "2,500",
            amountMinor = 2_500_00L,
            icon = categoryBadge("travel").icon,
            iconDrawableRes = R.drawable.ic_domestic_trip,
            badgeColor = categoryBadge("travel").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HOMETOWN,
            number = 17,
            title = "Yearly 2 times Hometown visit\n(42,000 ÷ 12)",
            amountText = "3,500",
            amountMinor = 3_500_00L,
            icon = categoryBadge("hometown").icon,
            iconDrawableRes = R.drawable.ic_hometown_visit,
            badgeColor = categoryBadge("hometown").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_MEDICINE,
            number = 18,
            title = "Medicine",
            amountText = "500",
            amountMinor = 500_00L,
            icon = categoryBadge("medicine").icon,
            iconDrawableRes = R.drawable.ic_medicine,
            badgeColor = categoryBadge("medicine").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_FAMILY_SUPPORT,
            number = 19,
            title = "Sending to parents",
            amountText = "10,000",
            amountMinor = 10_000_00L,
            icon = categoryBadge("family").icon,
            iconDrawableRes = R.drawable.ic_sending_to_parents,
            badgeColor = categoryBadge("family").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.INVESTMENT_SIP,
            number = 20,
            title = "SIP",
            amountText = "5,000",
            amountMinor = 5_000_00L,
            icon = categoryBadge("sip").icon,
            iconDrawableRes = R.drawable.ic_sip,
            badgeColor = categoryBadge("sip").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HEALTH_INSURANCE,
            number = 21,
            title = "Health insurance",
            amountText = "1,000",
            amountMinor = 1_000_00L,
            icon = categoryBadge("health_insurance").icon,
            iconDrawableRes = R.drawable.ic_health_insurance,
            badgeColor = categoryBadge("health_insurance").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.SAVING_LIC_POLICY,
            number = 22,
            title = "LIC",
            amountText = "2,000",
            amountMinor = 2_000_00L,
            icon = categoryBadge("lic").icon,
            iconDrawableRes = R.drawable.ic_lic,
            badgeColor = categoryBadge("lic").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.SAVING_EMERGENCY_FUND,
            number = 23,
            title = "Emergency fund",
            amountText = "7,000",
            amountMinor = 7_000_00L,
            icon = categoryBadge("emergency_fund").icon,
            iconDrawableRes = R.drawable.ic_emergency_fund,
            badgeColor = categoryBadge("emergency_fund").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_OTHER,
            number = 24,
            title = "Other expenses",
            amountText = "1,000",
            amountMinor = 1_000_00L,
            icon = categoryBadge("other").icon,
            iconDrawableRes = R.drawable.ic_other_expenses,
            badgeColor = categoryBadge("other").backgroundColor,
            isEssential = false
        )
    )

    val salaryVsExpenses = listOf(
        SummaryLineUi(label = "In-hand Salary", value = "₹70,000"),
        SummaryLineUi(label = "Total Expenses", value = "₹69,350")
    )

    val savings = listOf(
        SummaryLineUi(label = "SIP", value = "₹5,000"),
        SummaryLineUi(label = "LIC", value = "₹2,000"),
        SummaryLineUi(label = "Emergency Fund", value = "₹7,000")
    )

    val living = listOf(
        SummaryLineUi(label = "Total Expenses", value = "₹69,350"),
        SummaryLineUi(label = "Less Savings/Investments", value = "(₹14,000)")
    )

    val annual = listOf(
        SummaryLineUi(
            label = "Total Allocation",
            value = "₹8,32,200",
            subLabel = "₹69,350 × 12"
        ),
        SummaryLineUi(
            label = "Salary",
            value = "₹8,40,000",
            subLabel = "₹70,000 × 12"
        )
    )
}
