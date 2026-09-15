package com.premraj.moneyboard.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun categoryIcon(iconKey: String): ImageVector =
    when (iconKey) {
        "home", "house_help" -> Icons.Rounded.Home
        "groceries" -> Icons.Rounded.LocalGroceryStore
        "food" -> Icons.Rounded.Fastfood
        "electricity" -> Icons.Rounded.ElectricalServices
        "water" -> Icons.Rounded.WaterDrop
        "wifi" -> Icons.Rounded.Wifi
        "phone" -> Icons.Rounded.PhoneAndroid
        "fuel", "gas" -> Icons.Rounded.LocalGasStation
        "cab" -> Icons.Rounded.DirectionsCar
        "bus" -> Icons.Rounded.DirectionsBus
        "shopping", "salon", "laundry", "outing" -> Icons.Rounded.ShoppingBag
        "fitness", "fitness_diet" -> Icons.Rounded.FitnessCenter
        "medicine" -> Icons.Rounded.LocalHospital
        "education" -> Icons.Rounded.School
        "subscriptions" -> Icons.Rounded.Subscriptions
        "travel", "home_travel" -> Icons.Rounded.TravelExplore
        "salary", "work", "income_other" -> Icons.Rounded.Work
        "savings", "emergency_fund", "sip", "investment", "insurance" ->
            Icons.Rounded.Savings
        else -> Icons.Rounded.AccountBalance
    }
