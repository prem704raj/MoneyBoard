package com.premraj.moneyboard.core.domain.repository

import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.Money
import kotlinx.coroutines.flow.Flow

data class CreateAccountCommand(
    val name: String,
    val type: AccountType,
    val openingBalance: Money,
    val includeInNetWorth: Boolean = true
) {
    init {
        require(name.isNotBlank()) { "Account name cannot be blank" }
        require(name.length <= 60) { "Account name cannot exceed 60 characters" }
    }
}

interface AccountRepository {
    fun observeActiveAccounts(): Flow<List<Account>>
    suspend fun getAccount(accountId: String): Account?
    suspend fun createAccount(command: CreateAccountCommand): Account
    suspend fun archiveAccount(accountId: String)
}
