package com.premraj.moneyboard.core.database.dao

import androidx.room3.EntityDeleteOrUpdateAdapter
import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.coroutines.createFlow
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.getTotalChangedRows
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.database.projection.CategoryTotalRow
import javax.`annotation`.processing.Generated
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
internal class TransactionDao_Impl(
  __db: RoomDatabase,
) : TransactionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTransactionEntity: EntityInsertAdapter<TransactionEntity>

  private val __updateAdapterOfTransactionEntity: EntityDeleteOrUpdateAdapter<TransactionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTransactionEntity = object : EntityInsertAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `transactions` (`id`,`accountId`,`categoryId`,`type`,`amountMinor`,`currencyCode`,`localDateEpochDay`,`occurredAtEpochMillis`,`note`,`merchant`,`createdAtEpochMillis`,`updatedAtEpochMillis`,`deletedAtEpochMillis`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.accountId)
        statement.bindText(3, entity.categoryId)
        statement.bindText(4, entity.type)
        statement.bindLong(5, entity.amountMinor)
        statement.bindText(6, entity.currencyCode)
        statement.bindLong(7, entity.localDateEpochDay)
        statement.bindLong(8, entity.occurredAtEpochMillis)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpNote)
        }
        val _tmpMerchant: String? = entity.merchant
        if (_tmpMerchant == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpMerchant)
        }
        statement.bindLong(11, entity.createdAtEpochMillis)
        statement.bindLong(12, entity.updatedAtEpochMillis)
        val _tmpDeletedAtEpochMillis: Long? = entity.deletedAtEpochMillis
        if (_tmpDeletedAtEpochMillis == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpDeletedAtEpochMillis)
        }
      }
    }
    this.__updateAdapterOfTransactionEntity = object : EntityDeleteOrUpdateAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `transactions` SET `id` = ?,`accountId` = ?,`categoryId` = ?,`type` = ?,`amountMinor` = ?,`currencyCode` = ?,`localDateEpochDay` = ?,`occurredAtEpochMillis` = ?,`note` = ?,`merchant` = ?,`createdAtEpochMillis` = ?,`updatedAtEpochMillis` = ?,`deletedAtEpochMillis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.accountId)
        statement.bindText(3, entity.categoryId)
        statement.bindText(4, entity.type)
        statement.bindLong(5, entity.amountMinor)
        statement.bindText(6, entity.currencyCode)
        statement.bindLong(7, entity.localDateEpochDay)
        statement.bindLong(8, entity.occurredAtEpochMillis)
        val _tmpNote: String? = entity.note
        if (_tmpNote == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpNote)
        }
        val _tmpMerchant: String? = entity.merchant
        if (_tmpMerchant == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpMerchant)
        }
        statement.bindLong(11, entity.createdAtEpochMillis)
        statement.bindLong(12, entity.updatedAtEpochMillis)
        val _tmpDeletedAtEpochMillis: Long? = entity.deletedAtEpochMillis
        if (_tmpDeletedAtEpochMillis == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpDeletedAtEpochMillis)
        }
        statement.bindText(14, entity.id)
      }
    }
  }

  public override suspend fun insert(transaction: TransactionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTransactionEntity.insert(_connection, transaction)
  }

  public override suspend fun update(transaction: TransactionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTransactionEntity.handle(_connection, transaction)
  }

  public override suspend fun getByIdIncludingDeleted(id: String): TransactionEntity? {
    val _sql: String = "SELECT * FROM transactions WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfLocalDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "localDateEpochDay")
        val _columnIndexOfOccurredAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "occurredAtEpochMillis")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMerchant: Int = getColumnIndexOrThrow(_stmt, "merchant")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _columnIndexOfDeletedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "deletedAtEpochMillis")
        val _result: TransactionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAccountId: String
          _tmpAccountId = _stmt.getText(_columnIndexOfAccountId)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpLocalDateEpochDay: Long
          _tmpLocalDateEpochDay = _stmt.getLong(_columnIndexOfLocalDateEpochDay)
          val _tmpOccurredAtEpochMillis: Long
          _tmpOccurredAtEpochMillis = _stmt.getLong(_columnIndexOfOccurredAtEpochMillis)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMerchant: String?
          if (_stmt.isNull(_columnIndexOfMerchant)) {
            _tmpMerchant = null
          } else {
            _tmpMerchant = _stmt.getText(_columnIndexOfMerchant)
          }
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          val _tmpDeletedAtEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAtEpochMillis)) {
            _tmpDeletedAtEpochMillis = null
          } else {
            _tmpDeletedAtEpochMillis = _stmt.getLong(_columnIndexOfDeletedAtEpochMillis)
          }
          _result = TransactionEntity(_tmpId,_tmpAccountId,_tmpCategoryId,_tmpType,_tmpAmountMinor,_tmpCurrencyCode,_tmpLocalDateEpochDay,_tmpOccurredAtEpochMillis,_tmpNote,_tmpMerchant,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis,_tmpDeletedAtEpochMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveById(id: String): TransactionEntity? {
    val _sql: String = "SELECT * FROM transactions WHERE id = ? AND deletedAtEpochMillis IS NULL LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfLocalDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "localDateEpochDay")
        val _columnIndexOfOccurredAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "occurredAtEpochMillis")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMerchant: Int = getColumnIndexOrThrow(_stmt, "merchant")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _columnIndexOfDeletedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "deletedAtEpochMillis")
        val _result: TransactionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAccountId: String
          _tmpAccountId = _stmt.getText(_columnIndexOfAccountId)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpLocalDateEpochDay: Long
          _tmpLocalDateEpochDay = _stmt.getLong(_columnIndexOfLocalDateEpochDay)
          val _tmpOccurredAtEpochMillis: Long
          _tmpOccurredAtEpochMillis = _stmt.getLong(_columnIndexOfOccurredAtEpochMillis)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMerchant: String?
          if (_stmt.isNull(_columnIndexOfMerchant)) {
            _tmpMerchant = null
          } else {
            _tmpMerchant = _stmt.getText(_columnIndexOfMerchant)
          }
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          val _tmpDeletedAtEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAtEpochMillis)) {
            _tmpDeletedAtEpochMillis = null
          } else {
            _tmpDeletedAtEpochMillis = _stmt.getLong(_columnIndexOfDeletedAtEpochMillis)
          }
          _result = TransactionEntity(_tmpId,_tmpAccountId,_tmpCategoryId,_tmpType,_tmpAmountMinor,_tmpCurrencyCode,_tmpLocalDateEpochDay,_tmpOccurredAtEpochMillis,_tmpNote,_tmpMerchant,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis,_tmpDeletedAtEpochMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActiveBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<TransactionEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM transactions
        |        WHERE localDateEpochDay BETWEEN ? AND ?
        |          AND deletedAtEpochMillis IS NULL
        |        ORDER BY localDateEpochDay DESC, occurredAtEpochMillis DESC, createdAtEpochMillis DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startEpochDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endEpochDay)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAccountId: Int = getColumnIndexOrThrow(_stmt, "accountId")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfCurrencyCode: Int = getColumnIndexOrThrow(_stmt, "currencyCode")
        val _columnIndexOfLocalDateEpochDay: Int = getColumnIndexOrThrow(_stmt, "localDateEpochDay")
        val _columnIndexOfOccurredAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "occurredAtEpochMillis")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfMerchant: Int = getColumnIndexOrThrow(_stmt, "merchant")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _columnIndexOfDeletedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "deletedAtEpochMillis")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpAccountId: String
          _tmpAccountId = _stmt.getText(_columnIndexOfAccountId)
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpCurrencyCode: String
          _tmpCurrencyCode = _stmt.getText(_columnIndexOfCurrencyCode)
          val _tmpLocalDateEpochDay: Long
          _tmpLocalDateEpochDay = _stmt.getLong(_columnIndexOfLocalDateEpochDay)
          val _tmpOccurredAtEpochMillis: Long
          _tmpOccurredAtEpochMillis = _stmt.getLong(_columnIndexOfOccurredAtEpochMillis)
          val _tmpNote: String?
          if (_stmt.isNull(_columnIndexOfNote)) {
            _tmpNote = null
          } else {
            _tmpNote = _stmt.getText(_columnIndexOfNote)
          }
          val _tmpMerchant: String?
          if (_stmt.isNull(_columnIndexOfMerchant)) {
            _tmpMerchant = null
          } else {
            _tmpMerchant = _stmt.getText(_columnIndexOfMerchant)
          }
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          val _tmpDeletedAtEpochMillis: Long?
          if (_stmt.isNull(_columnIndexOfDeletedAtEpochMillis)) {
            _tmpDeletedAtEpochMillis = null
          } else {
            _tmpDeletedAtEpochMillis = _stmt.getLong(_columnIndexOfDeletedAtEpochMillis)
          }
          _item = TransactionEntity(_tmpId,_tmpAccountId,_tmpCategoryId,_tmpType,_tmpAmountMinor,_tmpCurrencyCode,_tmpLocalDateEpochDay,_tmpOccurredAtEpochMillis,_tmpNote,_tmpMerchant,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis,_tmpDeletedAtEpochMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTotalForTypeBetween(
    startEpochDay: Long,
    endEpochDay: Long,
    type: String,
    currencyCode: String,
  ): Flow<Long> {
    val _sql: String = """
        |
        |        SELECT COALESCE(SUM(amountMinor), 0)
        |        FROM transactions
        |        WHERE localDateEpochDay BETWEEN ? AND ?
        |          AND type = ?
        |          AND currencyCode = ?
        |          AND deletedAtEpochMillis IS NULL
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startEpochDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endEpochDay)
        _argIndex = 3
        _stmt.bindText(_argIndex, type)
        _argIndex = 4
        _stmt.bindText(_argIndex, currencyCode)
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

  public override fun observeCategoryTotalsBetween(
    startEpochDay: Long,
    endEpochDay: Long,
    type: String,
    currencyCode: String,
  ): Flow<List<CategoryTotalRow>> {
    val _sql: String = """
        |
        |        SELECT categoryId, COALESCE(SUM(amountMinor), 0) AS totalMinor
        |        FROM transactions
        |        WHERE localDateEpochDay BETWEEN ? AND ?
        |          AND type = ?
        |          AND currencyCode = ?
        |          AND deletedAtEpochMillis IS NULL
        |        GROUP BY categoryId
        |        ORDER BY totalMinor DESC
        |        
        """.trimMargin()
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, startEpochDay)
        _argIndex = 2
        _stmt.bindLong(_argIndex, endEpochDay)
        _argIndex = 3
        _stmt.bindText(_argIndex, type)
        _argIndex = 4
        _stmt.bindText(_argIndex, currencyCode)
        val _columnIndexOfCategoryId: Int = 0
        val _columnIndexOfTotalMinor: Int = 1
        val _result: MutableList<CategoryTotalRow> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryTotalRow
          val _tmpCategoryId: String
          _tmpCategoryId = _stmt.getText(_columnIndexOfCategoryId)
          val _tmpTotalMinor: Long
          _tmpTotalMinor = _stmt.getLong(_columnIndexOfTotalMinor)
          _item = CategoryTotalRow(_tmpCategoryId,_tmpTotalMinor)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countActive(): Long {
    val _sql: String = "SELECT COUNT(*) FROM transactions WHERE deletedAtEpochMillis IS NULL"
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

  public override suspend fun softDelete(transactionId: String, deletedAt: Long): Int {
    val _sql: String = """
        |
        |        UPDATE transactions
        |        SET deletedAtEpochMillis = ?, updatedAtEpochMillis = ?
        |        WHERE id = ? AND deletedAtEpochMillis IS NULL
        |        
        """.trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, deletedAt)
        _argIndex = 2
        _stmt.bindLong(_argIndex, deletedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, transactionId)
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
