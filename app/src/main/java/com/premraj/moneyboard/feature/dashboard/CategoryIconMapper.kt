package com.premraj.moneyboard.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Commute
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

data class CategoryBadgeUi(
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color = Color.White
)

fun categoryBadge(iconKey: String): CategoryBadgeUi =
    when (iconKey) {
        "home", "rent" -> CategoryBadgeUi(Icons.Rounded.Home, MoneyBoardColors.BadgeTeal)
        "groceries" -> CategoryBadgeUi(Icons.Rounded.LocalGroceryStore, MoneyBoardColors.BadgeGreen)
        "gas", "fuel" -> CategoryBadgeUi(Icons.Rounded.LocalGasStation, MoneyBoardColors.BadgeRed)
        "maid", "house_help" -> CategoryBadgeUi(Icons.Rounded.CleaningServices, MoneyBoardColors.BadgeTeal)
        "online_food", "food" -> CategoryBadgeUi(Icons.Rounded.Restaurant, MoneyBoardColors.BadgeOrange)
        "electricity" -> CategoryBadgeUi(Icons.Rounded.ElectricalServices, MoneyBoardColors.BadgeAmber)
        "wifi" -> CategoryBadgeUi(Icons.Rounded.Wifi, MoneyBoardColors.BadgeSkyBlue)
        "mobile", "phone" -> CategoryBadgeUi(Icons.Rounded.PhoneAndroid, MoneyBoardColors.BadgeBlue)
        "cab" -> CategoryBadgeUi(Icons.Rounded.DirectionsCar, MoneyBoardColors.BadgeAmber)
        "laundry" -> CategoryBadgeUi(Icons.Rounded.LocalLaundryService, MoneyBoardColors.BadgeBlue)
        "shopping" -> CategoryBadgeUi(Icons.Rounded.ShoppingBag, MoneyBoardColors.BadgePink)
        "gym", "fitness" -> CategoryBadgeUi(Icons.Rounded.FitnessCenter, MoneyBoardColors.BadgeBlue)
        "gym_diet", "fitness_diet" -> CategoryBadgeUi(Icons.Rounded.Spa, MoneyBoardColors.BadgeGreen)
        "salon" -> CategoryBadgeUi(Icons.Rounded.AutoAwesome, MoneyBoardColors.BadgeDeepPink)
        "outing" -> CategoryBadgeUi(Icons.Rounded.Landscape, MoneyBoardColors.BadgeTeal)
        "travel", "domestic_trip" -> CategoryBadgeUi(Icons.Rounded.Luggage, MoneyBoardColors.BadgeBlue)
        "hometown", "home_travel" -> CategoryBadgeUi(Icons.Rounded.Commute, MoneyBoardColors.BadgeBlue)
        "medicine" -> CategoryBadgeUi(Icons.Rounded.LocalHospital, MoneyBoardColors.BadgeBlue)
        "family", "sending_to_parents", "family_support" -> CategoryBadgeUi(Icons.Rounded.FamilyRestroom, MoneyBoardColors.BadgeOrange)
        "sip", "investment" -> CategoryBadgeUi(Icons.Rounded.TrendingUp, MoneyBoardColors.BadgeGreen)
        "health_insurance", "insurance" -> CategoryBadgeUi(Icons.Rounded.HealthAndSafety, MoneyBoardColors.BadgeBlue)
        "lic" -> CategoryBadgeUi(Icons.Rounded.Security, MoneyBoardColors.BadgeDarkNavy)
        "emergency_fund", "savings" -> CategoryBadgeUi(Icons.Rounded.Savings, MoneyBoardColors.BadgePink)
        "other", "other_expenses" -> CategoryBadgeUi(Icons.Rounded.MoreHoriz, MoneyBoardColors.BadgeSlate)
        "salary", "work", "income_other" -> CategoryBadgeUi(Icons.Rounded.Work, MoneyBoardColors.BadgeTeal)
        "water" -> CategoryBadgeUi(Icons.Rounded.WaterDrop, MoneyBoardColors.BadgeSkyBlue)
        "bus" -> CategoryBadgeUi(Icons.Rounded.DirectionsBus, MoneyBoardColors.BadgeBlue)
        "education" -> CategoryBadgeUi(Icons.Rounded.School, MoneyBoardColors.BadgeBlue)
        "subscriptions" -> CategoryBadgeUi(Icons.Rounded.Subscriptions, MoneyBoardColors.BadgePurple)
        else -> CategoryBadgeUi(Icons.Rounded.AccountBalance, MoneyBoardColors.BadgeBlue)
    }

fun categoryIcon(iconKey: String): ImageVector = categoryBadge(iconKey).icon

