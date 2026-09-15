package com.premraj.moneyboard.core.domain.model

data class DashboardMetrics(
    val income: Money,
    val expenses: Money,
    val savings: Money,
    val investments: Money,
    val totalAllocation: Money,
    val remaining: Money,
    val savingsRateBasisPoints: Int?
) {
    fun annualized(): DashboardMetrics {
        return copy(
            income = income * 12,
            expenses = expenses * 12,
            savings = savings * 12,
            investments = investments * 12,
            totalAllocation = totalAllocation * 12,
            remaining = remaining * 12
        )
    }
}
