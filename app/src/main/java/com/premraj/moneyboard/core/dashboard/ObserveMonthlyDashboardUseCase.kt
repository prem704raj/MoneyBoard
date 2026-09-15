package com.premraj.moneyboard.core.dashboard

import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth

class ObserveMonthlyDashboardUseCase(
    private val monthlyPlanRepository: MonthlyPlanRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val defaultCurrencyCode: String = "INR"
) {
    operator fun invoke(
        month: YearMonth
    ): Flow<MonthlyDashboardSnapshot> {
        return combine(
            monthlyPlanRepository.observeIncomePlans(month),
            monthlyPlanRepository.observeCategoryPlans(month),
            transactionRepository.observeTransactions(month),
            categoryRepository.observeAllCategories()
        ) { income, plans, transactions, categories ->
            DashboardCalculator.calculate(
                month = month,
                currencyCode = defaultCurrencyCode,
                incomePlans = income,
                categoryPlans = plans,
                transactions = transactions,
                categories = categories
            )
        }
    }
}
