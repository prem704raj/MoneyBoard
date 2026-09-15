package com.premraj.moneyboard.core.database.dao

import androidx.room3.EntityDeleteOrUpdateAdapter
import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.coroutines.createFlow
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.getTotalChangedRows
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.premraj.moneyboard.core.database.entity.AccountEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class AccountDao_Impl(
  __db: RoomDatabase,
) : AccountDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAccountEntity: EntityInsertAdapter<AccountEntity>

  private val __insertAdapterOfAccountEntity_1: EntityInsertAdapter<AccountEntity>

  private val __updateAdapterOfAccountEntity: EntityDeleteOrUpdateAdapter<AccountEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAccountEntity = object : EntityInsertAdapter<AccountEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `accounts` (`id`,`name`,`type`,`openingBalanceMinor`,`currencyCode`,`includeInNetWorth`,`archived`,`createdAtEpochMillis`,`updatedAtEpochMillis`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.type)
        statement.bindLong(4, entity.openingBalanceMinor)
        statement.bindText(5, entity.currencyCode)
        val _tmp: Int = if (entity.includeInNetWorth) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        statement.bindLong(8, entity.createdAtEpochMillis)
        statement.bindLong(9, entity.updatedAtEpochMillis)
      }
    }
    this.__insertAdapterOfAccountEntity_1 = object : EntityInsertAdapter<AccountEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `accounts` (`id`,`name`,`type`,`openingBalanceMinor`,`currencyCode`,`includeInNetWorth`,`archived`,`createdAtEpochMillis`,`updatedAtEpochMillis`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.type)
        statement.bindLong(4, entity.openingBalanceMinor)
        statement.bindText(5, entity.currencyCode)
        val _tmp: Int = if (entity.includeInNetWorth) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        statement.bindLong(8, entity.createdAtEpochMillis)
        statement.bindLong(9, entity.updatedAtEpochMillis)
      }
    }
    this.__updateAdapterOfAccountEntity = object : EntityDeleteOrUpdateAdapter<AccountEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `accounts` SET `id` = ?,`name` = ?,`type` = ?,`openingBalanceMinor` = ?,`currencyCode` = ?,`includeInNetWorth` = ?,`archived` = ?,`createdAtEpochMillis` = ?,`updatedAtEpochMillis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AccountEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.type)
        statement.bindLong(4, entity.openingBalanceMinor)
        statement.bindText(5, entity.currencyCode)
        val _tmp: Int = if (entity.includeInNetWorth) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(7, _tmp_1.toLong())
        statement.bindLong(8, entity.createdAtEpochMillis)
        statement.bindLong(9, entity.updatedAtEpochMillis)
        statement.bindText(10, entity.id)
      }
    }
  }

  public override suspend fun insert(account: AccountEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAccountEntity.insert(_connection, account)
  }

  public override suspend fun insertIfAbsent(accounts: List<AccountEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAccountEntity_1.insert(_connection, accounts)
  }

  public override suspend fun update(account: AccountEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfAccountEntity.handle(_connection, account)
  }

  public override fun observeActive(): Flow<List<AccountEntity>> {
    val _sql: String = "SELECT * FROM accounts WHERE archived = 0 ORDER BY name COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("accounts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfOpeningBalanceMinor: Int = getColumnIndexOrThrow(_stmt, "openingBalanceMinor")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfIncludeInNetWorth: Int = getColumnIndexOrThrow(_stmt, "includeInNetWorth")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _result: MutableList<AccountEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AccountEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpOpeningBalanceMinor: Long
          _tmpOpeningBalanceMinor = _stmt.getLong(_columnIndexOfOpeningBalanceMinor)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpIncludeInNetWorth: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIncludeInNetWorth).toInt()
          _tmpIncludeInNetWorth = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _item = AccountEntity(_tmpId,_tmpName,_tmpType,_tmpOpeningBalanceMinor,_tmpCurrencyCode,_tmpIncludeInNetWorth,_tmpArchived,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): AccountEntity? {
    val _sql: String = "SELECT * FROM accounts WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfOpeningBalanceMinor: Int = getColumnIndexOrThrow(_stmt, "openingBalanceMinor")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfIncludeInNetWorth: Int = getColumnIndexOrThrow(_stmt, "includeInNetWorth")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _result: AccountEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpOpeningBalanceMinor: Long
          _tmpOpeningBalanceMinor = _stmt.getLong(_columnIndexOfOpeningBalanceMinor)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpIncludeInNetWorth: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIncludeInNetWorth).toInt()
          _tmpIncludeInNetWorth = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _result = AccountEntity(_tmpId,_tmpName,_tmpType,_tmpOpeningBalanceMinor,_tmpCurrencyCode,_tmpIncludeInNetWorth,_tmpArchived,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countAll(): Long {
    val _sql: String = "SELECT COUNT(*) FROM accounts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Long
        if (_stmt.step()) {
          val _tmp: Long
          _tmp = _stmt.getLong(0)
          _result = _tmp
        } else {
          _result = 0L
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun archive(id: String, updatedAt: Long): Int {
    val _sql: String = "UPDATE accounts SET archived = 1, updatedAtEpochMillis = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredColumnConverters(): List<KClass<*>> = emptyList()

    public fun getRequiredDaoReturnTypeConverters(): List<KClass<*>> = emptyList()
  }
}
