package com.premraj.moneyboard.core.domain.model

data class DashboardCategoryLine(
    val categoryId: String,
    val name: String,
    val iconKey: String,
    val type: CategoryType,
    val amount: Money,
    val sortOrder: Int
)
