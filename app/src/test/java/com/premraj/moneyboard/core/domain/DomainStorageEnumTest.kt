package com.premraj.moneyboard.core.domain

import com.premraj.moneyboard.core.domain.model.AccountType
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DomainStorageEnumTest {
    @Test fun accountTypes_roundTrip() = AccountType.entries.forEach {
        assertEquals(it, AccountType.fromStorage(it.storageValue))
    }

    @Test fun categoryTypes_roundTrip() = CategoryType.entries.forEach {
        assertEquals(it, CategoryType.fromStorage(it.storageValue))
    }

    @Test fun groups_roundTrip() = CategoryGroup.entries.forEach {
        assertEquals(it, CategoryGroup.fromStorage(it.storageValue))
    }

    @Test fun transactionTypes_roundTrip() = TransactionType.entries.forEach {
        assertEquals(it, TransactionType.fromStorage(it.storageValue))
    }

    @Test fun unknownValue_failsFast() {
        assertThrows(IllegalStateException::class.java) {
            TransactionType.fromStorage("UNKNOWN")
        }
    }
}
