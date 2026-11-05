package com.premraj.moneyboard.core.database

import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType

object SeedIds {
    const val CASH_ACCOUNT = "account.cash"
    const val INCOME_SALARY = "category.income.salary"
    const val INCOME_FREELANCE = "category.income.freelance"
    const val INCOME_OTHER = "category.income.other"
    const val EXPENSE_RENT = "category.expense.rent"
    const val EXPENSE_GROCERIES = "category.expense.groceries"
    const val EXPENSE_GAS = "category.expense.gas"
    const val EXPENSE_HOUSE_HELP = "category.expense.house_help"
    const val EXPENSE_DINING = "category.expense.dining"
    const val EXPENSE_ELECTRICITY = "category.expense.electricity"
    const val EXPENSE_WATER = "category.expense.water"
    const val EXPENSE_WIFI = "category.expense.wifi"
    const val EXPENSE_MOBILE = "category.expense.mobile"
    const val EXPENSE_FUEL = "category.expense.fuel"
    const val EXPENSE_CAB = "category.expense.cab"
    const val EXPENSE_PUBLIC_TRANSPORT = "category.expense.public_transport"
    const val EXPENSE_LAUNDRY = "category.expense.laundry"
    const val EXPENSE_SHOPPING = "category.expense.shopping"
    const val EXPENSE_GYM = "category.expense.gym"
    const val EXPENSE_FITNESS_DIET = "category.expense.fitness_diet"
    const val EXPENSE_SALON = "category.expense.salon"
    const val EXPENSE_OUTING = "category.expense.outing"
    const val EXPENSE_TRAVEL = "category.expense.travel"
    const val EXPENSE_HOMETOWN = "category.expense.hometown"
    const val EXPENSE_MEDICINE = "category.expense.medicine"
    const val EXPENSE_FAMILY_SUPPORT = "category.expense.family_support"
    const val EXPENSE_HEALTH_INSURANCE = "category.expense.health_insurance"
    const val EXPENSE_EDUCATION = "category.expense.education"
    const val EXPENSE_EMI = "category.expense.emi"
    const val EXPENSE_SUBSCRIPTIONS = "category.expense.subscriptions"
    const val EXPENSE_OTHER = "category.expense.other"
    const val SAVING_EMERGENCY_FUND = "category.saving.emergency_fund"
    const val SAVING_GENERAL = "category.saving.general"
    const val SAVING_LIC_POLICY = "category.saving.lic_policy"
    const val INVESTMENT_SIP = "category.investment.sip"
    const val INVESTMENT_OTHER = "category.investment.other"
    const val TRANSFER_INTERNAL = "category.transfer.internal"
}

object DefaultSeedData {
    fun defaultAccounts(now: Long) = listOf(
        AccountEntity(
            id = SeedIds.CASH_ACCOUNT,
            name = "Cash",
            type = AccountType.CASH.storageValue,
            openingBalanceMinor = 0L,
            currencyCode = "INR",
            includeInNetWorth = true,
            archived = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
    )

    fun defaultCategories(now: Long): List<CategoryEntity> = listOf(
        c(SeedIds.INCOME_SALARY, "In-hand Salary", "salary", CategoryType.INCOME, CategoryGroup.INCOME, 10, now),
        c(SeedIds.INCOME_FREELANCE, "Freelance", "work", CategoryType.INCOME, CategoryGroup.INCOME, 20, now),
        c(SeedIds.INCOME_OTHER, "Other Income", "income_other", CategoryType.INCOME, CategoryGroup.INCOME, 30, now),

        // Reference 24 items in exact order
        c(SeedIds.EXPENSE_RENT, "Rent", "home", CategoryType.EXPENSE, CategoryGroup.HOUSING, 100, now),
        c(SeedIds.EXPENSE_GROCERIES, "Groceries", "groceries", CategoryType.EXPENSE, CategoryGroup.FOOD, 110, now),
        c(SeedIds.EXPENSE_GAS, "Gas", "gas", CategoryType.EXPENSE, CategoryGroup.UTILITIES, 120, now),
        c(SeedIds.EXPENSE_HOUSE_HELP, "Maid", "maid", CategoryType.EXPENSE, CategoryGroup.HOUSING, 130, now),
        c(SeedIds.EXPENSE_DINING, "Online food", "food", CategoryType.EXPENSE, CategoryGroup.FOOD, 140, now),
        c(SeedIds.EXPENSE_ELECTRICITY, "Electricity", "electricity", CategoryType.EXPENSE, CategoryGroup.UTILITIES, 150, now),
        c(SeedIds.EXPENSE_WIFI, "Wi-Fi", "wifi", CategoryType.EXPENSE, CategoryGroup.UTILITIES, 160, now),
        c(SeedIds.EXPENSE_MOBILE, "Mobile recharge", "phone", CategoryType.EXPENSE, CategoryGroup.UTILITIES, 170, now),
        c(SeedIds.EXPENSE_CAB, "Cab/Rapido", "cab", CategoryType.EXPENSE, CategoryGroup.TRANSPORT, 180, now),
        c(SeedIds.EXPENSE_LAUNDRY, "Laundry", "laundry", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 190, now),
        c(SeedIds.EXPENSE_SHOPPING, "Shopping", "shopping", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 200, now),
        c(SeedIds.EXPENSE_GYM, "Gym", "gym", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 210, now),

        c(SeedIds.EXPENSE_FITNESS_DIET, "Gym diet", "gym_diet", CategoryType.EXPENSE, CategoryGroup.HEALTH, 220, now),
        c(SeedIds.EXPENSE_SALON, "Skincare and saloon", "salon", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 230, now),
        c(SeedIds.EXPENSE_OUTING, "Weekend outing", "outing", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 240, now),
        c(SeedIds.EXPENSE_TRAVEL, "Yearly one Domestic trip\n(30,000 ÷ 12)", "travel", CategoryType.EXPENSE, CategoryGroup.TRAVEL, 250, now),
        c(SeedIds.EXPENSE_HOMETOWN, "Yearly 2 times Hometown visit\n(42,000 ÷ 12)", "hometown", CategoryType.EXPENSE, CategoryGroup.TRAVEL, 260, now),
        c(SeedIds.EXPENSE_MEDICINE, "Medicine", "medicine", CategoryType.EXPENSE, CategoryGroup.HEALTH, 270, now),
        c(SeedIds.EXPENSE_FAMILY_SUPPORT, "Sending to parents", "family", CategoryType.EXPENSE, CategoryGroup.FAMILY_SUPPORT, 280, now),
        c(SeedIds.INVESTMENT_SIP, "SIP", "sip", CategoryType.INVESTMENT, CategoryGroup.INVESTMENTS, 290, now),
        c(SeedIds.EXPENSE_HEALTH_INSURANCE, "Health insurance", "insurance", CategoryType.EXPENSE, CategoryGroup.INSURANCE, 300, now),
        c(SeedIds.SAVING_LIC_POLICY, "LIC", "lic", CategoryType.SAVING, CategoryGroup.INSURANCE, 310, now),
        c(SeedIds.SAVING_EMERGENCY_FUND, "Emergency fund", "emergency_fund", CategoryType.SAVING, CategoryGroup.SAVINGS, 320, now),
        c(SeedIds.EXPENSE_OTHER, "Other expenses", "other", CategoryType.EXPENSE, CategoryGroup.OTHER, 330, now),

        // Additional supporting categories
        c(SeedIds.EXPENSE_WATER, "Water", "water", CategoryType.EXPENSE, CategoryGroup.UTILITIES, 400, now),
        c(SeedIds.EXPENSE_FUEL, "Fuel", "fuel", CategoryType.EXPENSE, CategoryGroup.TRANSPORT, 410, now),
        c(SeedIds.EXPENSE_PUBLIC_TRANSPORT, "Public Transport", "bus", CategoryType.EXPENSE, CategoryGroup.TRANSPORT, 420, now),
        c(SeedIds.EXPENSE_EDUCATION, "Education", "education", CategoryType.EXPENSE, CategoryGroup.EDUCATION, 430, now),
        c(SeedIds.EXPENSE_EMI, "EMI", "debt", CategoryType.EXPENSE, CategoryGroup.DEBT, 440, now),
        c(SeedIds.EXPENSE_SUBSCRIPTIONS, "Subscriptions", "subscriptions", CategoryType.EXPENSE, CategoryGroup.LIFESTYLE, 450, now),
        c(SeedIds.SAVING_GENERAL, "General Savings", "savings", CategoryType.SAVING, CategoryGroup.SAVINGS, 460, now),
        c(SeedIds.INVESTMENT_OTHER, "Other Investment", "investment", CategoryType.INVESTMENT, CategoryGroup.INVESTMENTS, 470, now),
        c(SeedIds.TRANSFER_INTERNAL, "Internal Transfer", "transfer", CategoryType.TRANSFER, CategoryGroup.TRANSFER, 480, now)
    )

    private fun c(
        id: String,
        name: String,
        iconKey: String,
        type: CategoryType,
        group: CategoryGroup,
        sort: Int,
        now: Long
    ) = CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        type = type.storageValue,
        groupKey = group.storageValue,
        sortOrder = sort,
        systemCategory = true,
        archived = false,
        createdAtEpochMillis = now,
        updatedAtEpochMillis = now
    )
}
