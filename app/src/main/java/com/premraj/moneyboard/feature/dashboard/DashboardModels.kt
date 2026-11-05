package com.premraj.moneyboard.feature.dashboard

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors

data class ExpenseRowUi(
    val categoryId: String = "",
    val number: Int,
    val title: String,
    val amountText: String,
    val amountMinor: Long = 0L,
    val actualAmountMinor: Long = 0L,
    val actualAmountText: String = "₹0",
    val plannedAmountMinor: Long = 0L,
    val plannedAmountText: String = "₹0",
    val icon: ImageVector = Icons.Rounded.AccountBalance,
    @param:DrawableRes val iconDrawableRes: Int? = null,
    val iconEmoji: String? = null,
    val badgeColor: Color = MoneyBoardColors.BadgeBlue,
    val badgeIconTint: Color = Color.White,
    val isCustom: Boolean = false,
    val isEssential: Boolean = true
)

data class SummaryLineUi(
    val label: String,
    val value: String,
    val subLabel: String? = null
)
