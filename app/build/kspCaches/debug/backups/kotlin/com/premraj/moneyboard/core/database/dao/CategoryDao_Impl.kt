package com.premraj.moneyboard.core.database.dao

import androidx.room3.EntityDeleteOrUpdateAdapter
import androidx.room3.EntityInsertAdapter
import androidx.room3.RoomDatabase
import androidx.room3.coroutines.createFlow
import androidx.room3.util.getColumnIndexOrThrow
import androidx.room3.util.getTotalChangedRows
import androidx.room3.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.premraj.moneyboard.core.database.entity.CategoryEntity
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
internal class CategoryDao_Impl(
  __db: RoomDatabase,
) : CategoryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCategoryEntity: EntityInsertAdapter<CategoryEntity>

  private val __insertAdapterOfCategoryEntity_1: EntityInsertAdapter<CategoryEntity>

  private val __updateAdapterOfCategoryEntity: EntityDeleteOrUpdateAdapter<CategoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCategoryEntity = object : EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `categories` (`id`,`name`,`iconKey`,`type`,`groupKey`,`sortOrder`,`systemCategory`,`archived`,`createdAtEpochMillis`,`updatedAtEpochMillis`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.iconKey)
        statement.bindText(4, entity.type)
        statement.bindText(5, entity.groupKey)
        statement.bindLong(6, entity.sortOrder.toLong())
        val _tmp: Int = if (entity.systemCategory) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        statement.bindLong(9, entity.createdAtEpochMillis)
        statement.bindLong(10, entity.updatedAtEpochMillis)
      }
    }
    this.__insertAdapterOfCategoryEntity_1 = object : EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `categories` (`id`,`name`,`iconKey`,`type`,`groupKey`,`sortOrder`,`systemCategory`,`archived`,`createdAtEpochMillis`,`updatedAtEpochMillis`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.iconKey)
        statement.bindText(4, entity.type)
        statement.bindText(5, entity.groupKey)
        statement.bindLong(6, entity.sortOrder.toLong())
        val _tmp: Int = if (entity.systemCategory) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        statement.bindLong(9, entity.createdAtEpochMillis)
        statement.bindLong(10, entity.updatedAtEpochMillis)
      }
    }
    this.__updateAdapterOfCategoryEntity = object : EntityDeleteOrUpdateAdapter<CategoryEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `categories` SET `id` = ?,`name` = ?,`iconKey` = ?,`type` = ?,`groupKey` = ?,`sortOrder` = ?,`systemCategory` = ?,`archived` = ?,`createdAtEpochMillis` = ?,`updatedAtEpochMillis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.iconKey)
        statement.bindText(4, entity.type)
        statement.bindText(5, entity.groupKey)
        statement.bindLong(6, entity.sortOrder.toLong())
        val _tmp: Int = if (entity.systemCategory) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(8, _tmp_1.toLong())
        statement.bindLong(9, entity.createdAtEpochMillis)
        statement.bindLong(10, entity.updatedAtEpochMillis)
        statement.bindText(11, entity.id)
      }
    }
  }

  public override suspend fun insert(category: CategoryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity.insert(_connection, category)
  }

  public override suspend fun insertIfAbsent(categories: List<CategoryEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity_1.insert(_connection, categories)
  }

  public override suspend fun update(category: CategoryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCategoryEntity.handle(_connection, category)
  }

  public override fun observeActive(): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM categories WHERE archived = 0 ORDER BY type ASC, sortOrder ASC, name COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIconKey: Int = getColumnIndexOrThrow(_stmt, "iconKey")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfGroupKey: Int = getColumnIndexOrThrow(_stmt, "groupKey")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _columnIndexOfSystemCategory: Int = getColumnIndexOrThrow(_stmt, "systemCategory")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIconKey: String
          _tmpIconKey = _stmt.getText(_columnIndexOfIconKey)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpGroupKey: String
          _tmpGroupKey = _stmt.getText(_columnIndexOfGroupKey)
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          val _tmpSystemCategory: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSystemCategory).toInt()
          _tmpSystemCategory = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _item = CategoryEntity(_tmpId,_tmpName,_tmpIconKey,_tmpType,_tmpGroupKey,_tmpSortOrder,_tmpSystemCategory,_tmpArchived,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActiveByType(type: String): Flow<List<CategoryEntity>> {
    val _sql: String = "SELECT * FROM categories WHERE type = ? AND archived = 0 ORDER BY sortOrder ASC, name COLLATE NOCASE ASC"
    return createFlow(__db, false, arrayOf("categories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIconKey: Int = getColumnIndexOrThrow(_stmt, "iconKey")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfGroupKey: Int = getColumnIndexOrThrow(_stmt, "groupKey")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _columnIndexOfSystemCategory: Int = getColumnIndexOrThrow(_stmt, "systemCategory")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _result: MutableList<CategoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIconKey: String
          _tmpIconKey = _stmt.getText(_columnIndexOfIconKey)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpGroupKey: String
          _tmpGroupKey = _stmt.getText(_columnIndexOfGroupKey)
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          val _tmpSystemCategory: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSystemCategory).toInt()
          _tmpSystemCategory = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _item = CategoryEntity(_tmpId,_tmpName,_tmpIconKey,_tmpType,_tmpGroupKey,_tmpSortOrder,_tmpSystemCategory,_tmpArchived,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: String): CategoryEntity? {
    val _sql: String = "SELECT * FROM categories WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfIconKey: Int = getColumnIndexOrThrow(_stmt, "iconKey")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfGroupKey: Int = getColumnIndexOrThrow(_stmt, "groupKey")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _columnIndexOfSystemCategory: Int = getColumnIndexOrThrow(_stmt, "systemCategory")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfCreatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "createdAtEpochMillis")
        val _columnIndexOfUpdatedAtEpochMillis: Int = getColumnIndexOrThrow(_stmt, "updatedAtEpochMillis")
        val _result: CategoryEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpIconKey: String
          _tmpIconKey = _stmt.getText(_columnIndexOfIconKey)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpGroupKey: String
          _tmpGroupKey = _stmt.getText(_columnIndexOfGroupKey)
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          val _tmpSystemCategory: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSystemCategory).toInt()
          _tmpSystemCategory = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpCreatedAtEpochMillis: Long
          _tmpCreatedAtEpochMillis = _stmt.getLong(_columnIndexOfCreatedAtEpochMillis)
          val _tmpUpdatedAtEpochMillis: Long
          _tmpUpdatedAtEpochMillis = _stmt.getLong(_columnIndexOfUpdatedAtEpochMillis)
          _result = CategoryEntity(_tmpId,_tmpName,_tmpIconKey,_tmpType,_tmpGroupKey,_tmpSortOrder,_tmpSystemCategory,_tmpArchived,_tmpCreatedAtEpochMillis,_tmpUpdatedAtEpochMillis)
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
    val _sql: String = "SELECT COUNT(*) FROM categories"
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

  public override suspend fun archiveCustomCategory(id: String, updatedAt: Long): Int {
    val _sql: String = "UPDATE categories SET archived = 1, updatedAtEpochMillis = ? WHERE id = ? AND systemCategory = 0"
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
