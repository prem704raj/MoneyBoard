package com.premraj.moneyboard.core.database

import androidx.room3.InvalidationTracker
import androidx.room3.RoomOpenDelegate
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.room3.util.TableInfo
import androidx.room3.util.TableInfo.Companion.read
import androidx.room3.util.dropFtsSyncTriggers
import androidx.room3.util.performClear
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.premraj.moneyboard.core.database.dao.AccountDao
import com.premraj.moneyboard.core.database.dao.AccountDao_Impl
import com.premraj.moneyboard.core.database.dao.CategoryDao
import com.premraj.moneyboard.core.database.dao.CategoryDao_Impl
import com.premraj.moneyboard.core.database.dao.TransactionDao
import com.premraj.moneyboard.core.database.dao.TransactionDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room3.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL", "MemberExtensionConflict"])
internal class MoneyBoardDatabase_Impl : MoneyBoardDatabase() {
  private val _accountDao: Lazy<AccountDao> = lazy {
    AccountDao_Impl(this)
  }

  private val _categoryDao: Lazy<CategoryDao> = lazy {
    CategoryDao_Impl(this)
  }

  private val _transactionDao: Lazy<TransactionDao> = lazy {
    TransactionDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "1cb03ab9373aecbb31d5ce7831f6b70f", "740261b2ff5dd79dd2fc705b1141b124") {
      public override suspend fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `accounts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `openingBalanceMinor` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `includeInNetWorth` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_archived` ON `accounts` (`archived`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_accounts_type` ON `accounts` (`type`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `iconKey` TEXT NOT NULL, `type` TEXT NOT NULL, `groupKey` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `systemCategory` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_type_archived` ON `categories` (`type`, `archived`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_groupKey` ON `categories` (`groupKey`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_sortOrder` ON `categories` (`sortOrder`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `accountId` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `type` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `currencyCode` TEXT NOT NULL, `localDateEpochDay` INTEGER NOT NULL, `occurredAtEpochMillis` INTEGER NOT NULL, `note` TEXT, `merchant` TEXT, `createdAtEpochMillis` INTEGER NOT NULL, `updatedAtEpochMillis` INTEGER NOT NULL, `deletedAtEpochMillis` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_type` ON `transactions` (`type`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_localDateEpochDay` ON `transactions` (`localDateEpochDay`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_localDateEpochDay_type_deletedAtEpochMillis` ON `transactions` (`localDateEpochDay`, `type`, `deletedAtEpochMillis`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId_localDateEpochDay` ON `transactions` (`categoryId`, `localDateEpochDay`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_deletedAtEpochMillis` ON `transactions` (`deletedAtEpochMillis`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '1cb03ab9373aecbb31d5ce7831f6b70f')")
      }

      public override suspend fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `accounts`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `transactions`")
      }

      public override suspend fun onCreate(connection: SQLiteConnection) {
      }

      public override suspend fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override suspend fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override suspend fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override suspend fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsAccounts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAccounts.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("openingBalanceMinor", TableInfo.Column("openingBalanceMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("currencyCode", TableInfo.Column("currencyCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("includeInNetWorth", TableInfo.Column("includeInNetWorth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("createdAtEpochMillis", TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAccounts.put("updatedAtEpochMillis", TableInfo.Column("updatedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAccounts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAccounts: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesAccounts.add(TableInfo.Index("index_accounts_archived", false, listOf("archived"), listOf("ASC")))
        _indicesAccounts.add(TableInfo.Index("index_accounts_type", false, listOf("type"), listOf("ASC")))
        val _infoAccounts: TableInfo = TableInfo("accounts", _columnsAccounts, _foreignKeysAccounts, _indicesAccounts)
        val _existingAccounts: TableInfo = read(connection, "accounts")
        if (!_infoAccounts.equals(_existingAccounts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |accounts(com.premraj.moneyboard.core.database.entity.AccountEntity).
              | Expected:
              |""".trimMargin() + _infoAccounts + """
              |
              | Found:
              |""".trimMargin() + _existingAccounts)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("iconKey", TableInfo.Column("iconKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("groupKey", TableInfo.Column("groupKey", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("sortOrder", TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("systemCategory", TableInfo.Column("systemCategory", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("createdAtEpochMillis", TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("updatedAtEpochMillis", TableInfo.Column("updatedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCategories.add(TableInfo.Index("index_categories_type_archived", false, listOf("type", "archived"), listOf("ASC", "ASC")))
        _indicesCategories.add(TableInfo.Index("index_categories_groupKey", false, listOf("groupKey"), listOf("ASC")))
        _indicesCategories.add(TableInfo.Index("index_categories_sortOrder", false, listOf("sortOrder"), listOf("ASC")))
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.premraj.moneyboard.core.database.entity.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTransactions.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("accountId", TableInfo.Column("accountId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("categoryId", TableInfo.Column("categoryId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("amountMinor", TableInfo.Column("amountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("currencyCode", TableInfo.Column("currencyCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("localDateEpochDay", TableInfo.Column("localDateEpochDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("occurredAtEpochMillis", TableInfo.Column("occurredAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("note", TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("merchant", TableInfo.Column("merchant", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("createdAtEpochMillis", TableInfo.Column("createdAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("updatedAtEpochMillis", TableInfo.Column("updatedAtEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("deletedAtEpochMillis", TableInfo.Column("deletedAtEpochMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysTransactions.add(TableInfo.ForeignKey("accounts", "NO ACTION", "NO ACTION", listOf("accountId"), listOf("id")))
        _foreignKeysTransactions.add(TableInfo.ForeignKey("categories", "NO ACTION", "NO ACTION", listOf("categoryId"), listOf("id")))
        val _indicesTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTransactions.add(TableInfo.Index("index_transactions_accountId", false, listOf("accountId"), listOf("ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_categoryId", false, listOf("categoryId"), listOf("ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_type", false, listOf("type"), listOf("ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_localDateEpochDay", false, listOf("localDateEpochDay"), listOf("ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_localDateEpochDay_type_deletedAtEpochMillis", false, listOf("localDateEpochDay", "type", "deletedAtEpochMillis"), listOf("ASC", "ASC", "ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_categoryId_localDateEpochDay", false, listOf("categoryId", "localDateEpochDay"), listOf("ASC", "ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_deletedAtEpochMillis", false, listOf("deletedAtEpochMillis"), listOf("ASC")))
        val _infoTransactions: TableInfo = TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions)
        val _existingTransactions: TableInfo = read(connection, "transactions")
        if (!_infoTransactions.equals(_existingTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |transactions(com.premraj.moneyboard.core.database.entity.TransactionEntity).
              | Expected:
              |""".trimMargin() + _infoTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingTransactions)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "accounts", "categories", "transactions")
  }

  public override suspend fun clearAllTables() {
    performClear(this, true, "transactions", "accounts", "categories")
  }

  protected override fun getRequiredColumnTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _columnTypeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _columnTypeConvertersMap.put(AccountDao::class, AccountDao_Impl.getRequiredColumnConverters())
    _columnTypeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredColumnConverters())
    _columnTypeConvertersMap.put(TransactionDao::class, TransactionDao_Impl.getRequiredColumnConverters())
    return _columnTypeConvertersMap
  }

  protected override fun getRequiredDaoReturnTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _daoReturnTypeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _daoReturnTypeConvertersMap.put(AccountDao::class, AccountDao_Impl.getRequiredDaoReturnTypeConverters())
    _daoReturnTypeConvertersMap.put(CategoryDao::class, CategoryDao_Impl.getRequiredDaoReturnTypeConverters())
    _daoReturnTypeConvertersMap.put(TransactionDao::class, TransactionDao_Impl.getRequiredDaoReturnTypeConverters())
    return _daoReturnTypeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun accountDao(): AccountDao = _accountDao.value

  public override fun categoryDao(): CategoryDao = _categoryDao.value

  public override fun transactionDao(): TransactionDao = _transactionDao.value
}
