package com.premraj.moneyboard.core.data.repository

import com.premraj.moneyboard.core.common.IdProvider
import com.premraj.moneyboard.core.common.TimeProvider
import com.premraj.moneyboard.core.data.mapper.toDomain
import com.premraj.moneyboard.core.database.dao.AccountDao
import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.domain.model.Account
import com.premraj.moneyboard.core.domain.repository.AccountRepository
import com.premraj.moneyboard.core.domain.repository.CreateAccountCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomAccountRepository(
    private val dao: AccountDao,
    private val idProvider: IdProvider,
    private val timeProvider: TimeProvider
) : AccountRepository {
    override fun observeActiveAccounts(): Flow<List<Account>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun getAccount(accountId: String): Account? =
        dao.getById(accountId)?.toDomain()

    override suspend fun createAccount(command: CreateAccountCommand): Account {
        val now = timeProvider.nowEpochMillis()
        val entity = AccountEntity(
            id = idProvider.newId(),
            name = command.name.trim(),
            type = command.type.storageValue,
            openingBalanceMinor = command.openingBalance.amountMinor,
            currencyCode = command.openingBalance.currencyCode,
            includeInNetWorth = command.includeInNetWorth,
            archived = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )
        dao.insert(entity)
        return entity.toDomain()
    }

    override suspend fun archiveAccount(accountId: String) {
        require(dao.archive(accountId, timeProvider.nowEpochMillis()) == 1) {
            "Account does not exist or could not be archived: $accountId"
        }
    }
}
