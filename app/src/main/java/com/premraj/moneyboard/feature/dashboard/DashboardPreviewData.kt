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
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("rent").icon,
            iconDrawableRes = R.drawable.ic_rent,
            badgeColor = categoryBadge("rent").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GROCERIES,
            number = 2,
            title = "Groceries",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("groceries").icon,
            iconDrawableRes = R.drawable.ic_groceries,
            badgeColor = categoryBadge("groceries").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GAS,
            number = 3,
            title = "Gas",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("gas").icon,
            iconDrawableRes = R.drawable.ic_gas,
            badgeColor = categoryBadge("gas").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HOUSE_HELP,
            number = 4,
            title = "Maid",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("maid").icon,
            iconDrawableRes = R.drawable.ic_maid,
            badgeColor = categoryBadge("maid").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_DINING,
            number = 5,
            title = "Online food",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("online_food").icon,
            iconDrawableRes = R.drawable.ic_online_food,
            badgeColor = categoryBadge("online_food").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_ELECTRICITY,
            number = 6,
            title = "Electricity",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("electricity").icon,
            iconDrawableRes = R.drawable.ic_electricity,
            badgeColor = categoryBadge("electricity").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_WIFI,
            number = 7,
            title = "Wi-Fi",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("wifi").icon,
            iconDrawableRes = R.drawable.ic_wifi,
            badgeColor = categoryBadge("wifi").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_MOBILE,
            number = 8,
            title = "Mobile recharge",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("mobile").icon,
            iconDrawableRes = R.drawable.ic_mobile_recharge,
            badgeColor = categoryBadge("mobile").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_CAB,
            number = 9,
            title = "Cab/Rapido",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("cab").icon,
            iconDrawableRes = R.drawable.ic_cab_rapido,
            badgeColor = categoryBadge("cab").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_LAUNDRY,
            number = 10,
            title = "Laundry",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("laundry").icon,
            iconDrawableRes = R.drawable.ic_laundry,
            badgeColor = categoryBadge("laundry").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_SHOPPING,
            number = 11,
            title = "Shopping",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("shopping").icon,
            iconDrawableRes = R.drawable.ic_shopping,
            badgeColor = categoryBadge("shopping").backgroundColor,
            isEssential = true
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_GYM,
            number = 12,
            title = "Gym",
            amountText = "0",
            amountMinor = 0L,
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
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("gym_diet").icon,
            iconDrawableRes = R.drawable.ic_gym_diet,
            badgeColor = categoryBadge("gym_diet").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_SALON,
            number = 14,
            title = "Skincare and saloon",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("salon").icon,
            iconDrawableRes = R.drawable.ic_skincare_saloon,
            badgeColor = categoryBadge("salon").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_OUTING,
            number = 15,
            title = "Weekend outing",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("outing").icon,
            iconDrawableRes = R.drawable.ic_weekend_outing,
            badgeColor = categoryBadge("outing").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_TRAVEL,
            number = 16,
            title = "Yearly one Domestic trip\n(30,000 ÷ 12)",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("travel").icon,
            iconDrawableRes = R.drawable.ic_domestic_trip,
            badgeColor = categoryBadge("travel").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HOMETOWN,
            number = 17,
            title = "Yearly 2 times Hometown visit\n(42,000 ÷ 12)",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("hometown").icon,
            iconDrawableRes = R.drawable.ic_hometown_visit,
            badgeColor = categoryBadge("hometown").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_MEDICINE,
            number = 18,
            title = "Medicine",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("medicine").icon,
            iconDrawableRes = R.drawable.ic_medicine,
            badgeColor = categoryBadge("medicine").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_FAMILY_SUPPORT,
            number = 19,
            title = "Sending to parents",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("family").icon,
            iconDrawableRes = R.drawable.ic_sending_to_parents,
            badgeColor = categoryBadge("family").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.INVESTMENT_SIP,
            number = 20,
            title = "SIP",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("sip").icon,
            iconDrawableRes = R.drawable.ic_sip,
            badgeColor = categoryBadge("sip").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HEALTH_INSURANCE,
            number = 21,
            title = "Health insurance",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("health_insurance").icon,
            iconDrawableRes = R.drawable.ic_health_insurance,
            badgeColor = categoryBadge("health_insurance").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.SAVING_LIC_POLICY,
            number = 22,
            title = "LIC",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("lic").icon,
            iconDrawableRes = R.drawable.ic_lic,
            badgeColor = categoryBadge("lic").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.SAVING_EMERGENCY_FUND,
            number = 23,
            title = "Emergency fund",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("emergency_fund").icon,
            iconDrawableRes = R.drawable.ic_emergency_fund,
            badgeColor = categoryBadge("emergency_fund").backgroundColor,
            isEssential = false
        ),
        ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_OTHER,
            number = 24,
            title = "Other expenses",
            amountText = "0",
            amountMinor = 0L,
            icon = categoryBadge("other").icon,
            iconDrawableRes = R.drawable.ic_other_expenses,
            badgeColor = categoryBadge("other").backgroundColor,
            isEssential = false
        )
    )

    val salaryVsExpenses = listOf(
        SummaryLineUi(label = "In-hand Salary", value = "₹0"),
        SummaryLineUi(label = "Total Expenses", value = "₹0")
    )

    val savings = listOf(
        SummaryLineUi(label = "SIP", value = "₹0"),
        SummaryLineUi(label = "LIC", value = "₹0"),
        SummaryLineUi(label = "Emergency Fund", value = "₹0")
    )

    val living = listOf(
        SummaryLineUi(label = "Total Expenses", value = "₹0"),
        SummaryLineUi(label = "Less Savings/Investments", value = "(₹0)")
    )

    val annual = listOf(
        SummaryLineUi(
            label = "Total Allocation",
            value = "₹0",
            subLabel = "₹0 × 12"
        ),
        SummaryLineUi(
            label = "Salary",
            value = "₹0",
            subLabel = "₹0 × 12"
        )
    )
}

