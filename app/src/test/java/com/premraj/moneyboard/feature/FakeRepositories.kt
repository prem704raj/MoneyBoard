package com.premraj.moneyboard.feature

import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.FinanceTransaction
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyCategoryPlan
import com.premraj.moneyboard.core.domain.model.MonthlyIncomePlan
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.AccountRepository
import com.premraj.moneyboard.core.domain.repository.CategoryRepository
import com.premraj.moneyboard.core.domain.repository.CreateAccountCommand
import com.premraj.moneyboard.core.domain.repository.CreateCategoryCommand
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.MonthlyPlanRepository
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import com.premraj.moneyboard.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.YearMonth
import java.util.UUID

class FakeTransactionRepository : TransactionRepository {
    val transactionsFlow = MutableStateFlow<List<FinanceTransaction>>(emptyList())

    override fun observeTransactions(month: YearMonth): Flow<List<FinanceTransaction>> {
        return transactionsFlow.map { list ->
            list.filter {
                YearMonth.from(it.accountingDate) == month && it.deletedAtEpochMillis == null
            }
        }
    }

    override fun observeAllTransactions(): Flow<List<FinanceTransaction>> {
        return transactionsFlow.map { list ->
            list.filter { it.deletedAtEpochMillis == null }
        }
    }

    override fun observeTotal(month: YearMonth, type: TransactionType, currencyCode: String): Flow<Money> {
        return observeTransactions(month).map { list ->
            Money.sum(list.filter { it.type == type }.map { it.amount }, currencyCode)
        }
    }

    override fun observeCategoryTotals(
        month: YearMonth,
        type: TransactionType,
        currencyCode: String
    ): Flow<List<CategoryTotalRow>> {
        return observeTransactions(month).map { emptyList() }
    }

    override suspend fun getTransaction(transactionId: String): FinanceTransaction? {
        return transactionsFlow.value.find { it.id == transactionId }
    }

    override suspend fun createTransaction(command: CreateTransactionCommand): FinanceTransaction {
        val tx = FinanceTransaction(
            id = UUID.randomUUID().toString(),
            accountId = command.accountId,
            categoryId = command.categoryId,
            type = command.type,
            amount = command.amount,
            accountingDate = command.accountingDate,
            occurredAt = command.occurredAt,
            note = command.note,
            merchant = command.merchant,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L,
            deletedAtEpochMillis = null
        )
        transactionsFlow.value = transactionsFlow.value + tx
        return tx
    }

    override suspend fun updateTransaction(transaction: FinanceTransaction): FinanceTransaction {
        transactionsFlow.value = transactionsFlow.value.map {
            if (it.id == transaction.id) transaction else it
        }
        return transaction
    }

    override suspend fun softDeleteTransaction(transactionId: String) {
        transactionsFlow.value = transactionsFlow.value.map {
            if (it.id == transactionId) it.copy(deletedAtEpochMillis = 2000L) else it
        }
    }

    override suspend fun restoreTransaction(transactionId: String) {
        transactionsFlow.value = transactionsFlow.value.map {
            if (it.id == transactionId) it.copy(deletedAtEpochMillis = null) else it
        }
    }
}

class FakeAccountRepository : AccountRepository {
    val accountsFlow = MutableStateFlow<List<Account>>(emptyList())

    override fun observeActiveAccounts(): Flow<List<Account>> {
        return accountsFlow.map { list -> list.filter { !it.archived } }
    }

    override suspend fun getAccount(accountId: String): Account? {
        return accountsFlow.value.find { it.id == accountId }
    }

    override suspend fun createAccount(command: CreateAccountCommand): Account {
        val acc = Account(
            id = UUID.randomUUID().toString(),
            name = command.name,
            type = command.type,
            openingBalance = command.openingBalance,
            includeInNetWorth = command.includeInNetWorth,
            archived = false,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        accountsFlow.value = accountsFlow.value + acc
        return acc
    }

    override suspend fun archiveAccount(accountId: String) {
        accountsFlow.value = accountsFlow.value.map {
            if (it.id == accountId) it.copy(archived = true) else it
        }
    }
}

class FakeCategoryRepository : CategoryRepository {
    val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    override fun observeActiveCategories(): Flow<List<Category>> {
        return categoriesFlow.map { list -> list.filter { !it.archived } }
    }

    override fun observeActiveCategories(type: CategoryType): Flow<List<Category>> {
        return categoriesFlow.map { list -> list.filter { !it.archived && it.type == type } }
    }

    override fun observeAllCategories(): Flow<List<Category>> {
        return categoriesFlow
    }

    override suspend fun getCategory(categoryId: String): Category? {
        return categoriesFlow.value.find { it.id == categoryId }
    }

    override suspend fun createCategory(command: CreateCategoryCommand): Category {
        TODO("Not needed for tests")
    }

    override suspend fun archiveCustomCategory(categoryId: String) {
        categoriesFlow.value = categoriesFlow.value.map {
            if (it.id == categoryId) it.copy(archived = true) else it
        }
    }
}

class FakeMonthlyPlanRepository : MonthlyPlanRepository {
    val incomePlansFlow = MutableStateFlow<List<MonthlyIncomePlan>>(emptyList())
    val categoryPlansFlow = MutableStateFlow<List<MonthlyCategoryPlan>>(emptyList())

    override fun observeIncomePlans(month: YearMonth): Flow<List<MonthlyIncomePlan>> {
        return incomePlansFlow.map { list -> list.filter { it.month == month } }
    }

    override fun observeCategoryPlans(month: YearMonth): Flow<List<MonthlyCategoryPlan>> {
        return categoryPlansFlow.map { list -> list.filter { it.month == month } }
    }

    override suspend fun setIncomePlan(command: SetIncomePlanCommand): MonthlyIncomePlan {
        val plan = MonthlyIncomePlan(
            id = command.id ?: UUID.randomUUID().toString(),
            month = command.month,
            name = command.name,
            plannedAmount = command.plannedAmount,
            expectedDate = command.expectedDate,
            sortOrder = command.sortOrder,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        val filtered = incomePlansFlow.value.filter { it.id != plan.id }
        incomePlansFlow.value = filtered + plan
        return plan
    }

    override suspend fun deleteIncomePlan(id: String) {
        incomePlansFlow.value = incomePlansFlow.value.filter { it.id != id }
    }

    override suspend fun setCategoryPlan(command: SetCategoryPlanCommand): MonthlyCategoryPlan? {
        if (command.plannedAmount.amountMinor == 0L) {
            categoryPlansFlow.value = categoryPlansFlow.value.filterNot {
                it.month == command.month && it.categoryId == command.categoryId
            }
            return null
        }
        val plan = MonthlyCategoryPlan(
            month = command.month,
            categoryId = command.categoryId,
            plannedAmount = command.plannedAmount,
            createdAtEpochMillis = 1000L,
            updatedAtEpochMillis = 1000L
        )
        val filtered = categoryPlansFlow.value.filterNot {
            it.month == command.month && it.categoryId == command.categoryId
        }
        categoryPlansFlow.value = filtered + plan
        return plan
    }

    override suspend fun hasAnyPlan(month: YearMonth): Boolean {
        return incomePlansFlow.value.any { it.month == month } ||
            categoryPlansFlow.value.any { it.month == month }
    }
}
