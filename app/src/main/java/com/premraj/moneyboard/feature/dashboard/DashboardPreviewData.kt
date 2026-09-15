package com.premraj.moneyboard.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Wifi

object DashboardPreviewData {

    val expenses = listOf(
        ExpenseRowUi(
            number = 1,
            title = "Rent",
            amountText = "₹10,000",
            icon = Icons.Rounded.Home
        ),
        ExpenseRowUi(
            number = 2,
            title = "Groceries",
            amountText = "₹6,000",
            icon = Icons.Rounded.LocalGroceryStore
        ),
        ExpenseRowUi(
            number = 3,
            title = "Online food",
            amountText = "₹3,000",
            icon = Icons.Rounded.Fastfood
        ),
        ExpenseRowUi(
            number = 4,
            title = "Electricity",
            amountText = "₹1,500",
            icon = Icons.Rounded.ElectricalServices
        ),
        ExpenseRowUi(
            number = 5,
            title = "Wi-Fi",
            amountText = "₹600",
            icon = Icons.Rounded.Wifi
        ),
        ExpenseRowUi(
            number = 6,
            title = "Mobile recharge",
            amountText = "₹350",
            icon = Icons.Rounded.PhoneAndroid
        ),
        ExpenseRowUi(
            number = 7,
            title = "Cab / Rapido",
            amountText = "₹2,000",
            icon = Icons.Rounded.DirectionsCar
        ),
        ExpenseRowUi(
            number = 8,
            title = "Shopping",
            amountText = "₹2,000",
            icon = Icons.Rounded.ShoppingBag
        ),
        ExpenseRowUi(
            number = 9,
            title = "Gym",
            amountText = "₹1,200",
            icon = Icons.Rounded.FitnessCenter
        ),
        ExpenseRowUi(
            number = 10,
            title = "Medicine",
            amountText = "₹500",
            icon = Icons.Rounded.LocalHospital
        )
    )

    val salaryVsExpenses = listOf(
        SummaryLineUi("In-hand Salary", "₹70,000"),
        SummaryLineUi("Total Allocation", "₹69,350"),
        SummaryLineUi("Remaining", "₹650")
    )

    val savings = listOf(
        SummaryLineUi("SIP", "₹5,000"),
        SummaryLineUi("LIC", "₹2,000"),
        SummaryLineUi("Emergency Fund", "₹7,000"),
        SummaryLineUi("Total", "₹14,000")
    )

    val living = listOf(
        SummaryLineUi("Total Allocation", "₹69,350"),
        SummaryLineUi("Less Savings / Investments", "₹14,000"),
        SummaryLineUi("Actual Living / Family / Travel", "₹55,350")
    )

    val annual = listOf(
        SummaryLineUi("Annual Allocation", "₹8,32,200"),
        SummaryLineUi("Annual Salary", "₹8,40,000"),
        SummaryLineUi("Annual Balance", "₹7,800")
    )
}
