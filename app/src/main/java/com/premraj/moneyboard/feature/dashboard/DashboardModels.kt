package com.premraj.moneyboard.feature.dashboard

import androidx.compose.ui.graphics.vector.ImageVector

data class ExpenseRowUi(
    val number: Int,
    val title: String,
    val amountText: String,
    val icon: ImageVector
)

data class SummaryLineUi(
    val label: String,
    val value: String
)
