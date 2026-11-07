package com.premraj.moneyboard.core.export

import com.premraj.moneyboard.core.database.MoneyBoardDatabase
import com.premraj.moneyboard.core.database.entity.AccountEntity
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.database.entity.MonthlyIncomePlanEntity
import com.premraj.moneyboard.core.database.entity.TransactionEntity
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.settings.ThemeMode
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.core.settings.UserSettingsRepository
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

data class BackupPreview(
    val app: String,
    val schemaVersion: Int,
    val exportTimestamp: String,
    val accountCount: Int,
    val categoryCount: Int,
    val incomePlanCount: Int,
    val categoryPlanCount: Int,
    val transactionCount: Int,
    val cityName: String,
    val salaryRupees: Long
)

data class RestoreSummary(
    val accountsRestored: Int,
    val categoriesRestored: Int,
    val incomePlansRestored: Int,
    val categoryPlansRestored: Int,
    val transactionsRestored: Int
)

object FullDatabaseBackupManager {

    const val CURRENT_SCHEMA_VERSION = 1
    const val APP_IDENTIFIER = "SalaryLane"

    suspend fun exportAllData(
        database: MoneyBoardDatabase,
        settings: UserSettings
    ): String {
        val root = JSONObject().apply {
            put("schemaVersion", CURRENT_SCHEMA_VERSION)
            put("app", APP_IDENTIFIER)
            put("exportTimestamp", Instant.now().toString())

            // 1. Settings
            put("settings", JSONObject().apply {
                put("cityName", settings.cityName)
                put("profileTag", settings.profileTag)
                put("emiTag", settings.emiTag)
                put("inHandSalaryRupees", settings.inHandSalaryRupees)
                put("currencyCode", settings.currencyCode)
                put("themeMode", settings.themeMode.name)
                put("defaultMode", settings.defaultMode.name)
                put("textScale", settings.textScale.toDouble())
            })

            // 2. Accounts
            val accounts = database.accountDao().getAll()
            val accountsArray = JSONArray()
            accounts.forEach { acc ->
                accountsArray.put(JSONObject().apply {
                    put("id", acc.id)
                    put("name", acc.name)
                    put("type", acc.type)
                    put("openingBalanceMinor", acc.openingBalanceMinor)
                    put("currencyCode", acc.currencyCode)
                    put("includeInNetWorth", acc.includeInNetWorth)
                    put("archived", acc.archived)
                    put("createdAtEpochMillis", acc.createdAtEpochMillis)
                    put("updatedAtEpochMillis", acc.updatedAtEpochMillis)
                })
            }
            put("accounts", accountsArray)

            // 3. Categories
            val categories = database.categoryDao().getAll()
            val categoriesArray = JSONArray()
            categories.forEach { cat ->
                categoriesArray.put(JSONObject().apply {
                    put("id", cat.id)
                    put("name", cat.name)
                    put("iconKey", cat.iconKey)
                    put("type", cat.type)
                    put("groupKey", cat.groupKey)
                    put("sortOrder", cat.sortOrder)
                    put("systemCategory", cat.systemCategory)
                    put("archived", cat.archived)
                    put("createdAtEpochMillis", cat.createdAtEpochMillis)
                    put("updatedAtEpochMillis", cat.updatedAtEpochMillis)
                })
            }
            put("categories", categoriesArray)

            // 4. Monthly Income Plans
            val incomePlans = database.monthlyPlanDao().getAllIncomePlans()
            val incomePlansArray = JSONArray()
            incomePlans.forEach { ip ->
                incomePlansArray.put(JSONObject().apply {
                    put("id", ip.id)
                    put("monthKey", ip.monthKey)
                    put("name", ip.name)
                    put("plannedAmountMinor", ip.plannedAmountMinor)
                    put("currencyCode", ip.currencyCode)
                    put("expectedDateEpochDay", ip.expectedDateEpochDay ?: JSONObject.NULL)
                    put("sortOrder", ip.sortOrder)
                    put("createdAtEpochMillis", ip.createdAtEpochMillis)
                    put("updatedAtEpochMillis", ip.updatedAtEpochMillis)
                })
            }
            put("incomePlans", incomePlansArray)

            // 5. Monthly Category Plans
            val categoryPlans = database.monthlyPlanDao().getAllCategoryPlans()
            val categoryPlansArray = JSONArray()
            categoryPlans.forEach { cp ->
                categoryPlansArray.put(JSONObject().apply {
                    put("monthKey", cp.monthKey)
                    put("categoryId", cp.categoryId)
                    put("plannedAmountMinor", cp.plannedAmountMinor)
                    put("currencyCode", cp.currencyCode)
                    put("createdAtEpochMillis", cp.createdAtEpochMillis)
                    put("updatedAtEpochMillis", cp.updatedAtEpochMillis)
                })
            }
            put("categoryPlans", categoryPlansArray)

            // 6. Transactions
            val transactions = database.transactionDao().getAll()
            val transactionsArray = JSONArray()
            transactions.forEach { tx ->
                transactionsArray.put(JSONObject().apply {
                    put("id", tx.id)
                    put("accountId", tx.accountId)
                    put("categoryId", tx.categoryId)
                    put("type", tx.type)
                    put("amountMinor", tx.amountMinor)
                    put("currencyCode", tx.currencyCode)
                    put("localDateEpochDay", tx.localDateEpochDay)
                    put("occurredAtEpochMillis", tx.occurredAtEpochMillis)
                    put("note", tx.note ?: JSONObject.NULL)
                    put("merchant", tx.merchant ?: JSONObject.NULL)
                    put("createdAtEpochMillis", tx.createdAtEpochMillis)
                    put("updatedAtEpochMillis", tx.updatedAtEpochMillis)
                    put("deletedAtEpochMillis", tx.deletedAtEpochMillis ?: JSONObject.NULL)
                })
            }
            put("transactions", transactionsArray)
        }

        return root.toString(2)
    }

    fun previewBackup(jsonString: String): Result<BackupPreview> = runCatching {
        val root = JSONObject(jsonString)
        val schemaVersion = root.optInt("schemaVersion", 1)
        if (schemaVersion > CURRENT_SCHEMA_VERSION) {
            throw IllegalArgumentException("Backup was created with newer schema version $schemaVersion. Please update SalaryLane.")
        }

        val app = root.optString("app", "SalaryLane")
        val exportTimestamp = root.optString("exportTimestamp", "")

        val settingsObj = root.optJSONObject("settings")
        val cityName = settingsObj?.optString("cityName", "Pune") ?: "Pune"
        val salaryRupees = settingsObj?.optLong("inHandSalaryRupees", 0L) ?: 0L

        val accountsCount = root.optJSONArray("accounts")?.length() ?: 0
        val categoriesCount = root.optJSONArray("categories")?.length() ?: 0
        val incomePlansCount = root.optJSONArray("incomePlans")?.length() ?: 0
        val categoryPlansCount = root.optJSONArray("categoryPlans")?.length() ?: 0
        val transactionsCount = root.optJSONArray("transactions")?.length() ?: 0

        BackupPreview(
            app = app,
            schemaVersion = schemaVersion,
            exportTimestamp = exportTimestamp,
            accountCount = accountsCount,
            categoryCount = categoriesCount,
            incomePlanCount = incomePlansCount,
            categoryPlanCount = categoryPlansCount,
            transactionCount = transactionsCount,
            cityName = cityName,
            salaryRupees = salaryRupees
        )
    }

    suspend fun restoreAllData(
        database: MoneyBoardDatabase,
        settingsRepository: UserSettingsRepository?,
        jsonString: String
    ): Result<RestoreSummary> = runCatching {
        val root = JSONObject(jsonString)
        val schemaVersion = root.optInt("schemaVersion", 1)
        if (schemaVersion > CURRENT_SCHEMA_VERSION) {
            throw IllegalArgumentException("Incompatible schema version: $schemaVersion. Maximum supported is $CURRENT_SCHEMA_VERSION.")
        }

        // Parse Settings
        val settingsObj = root.optJSONObject("settings")
        if (settingsObj != null && settingsRepository != null) {
            val city = settingsObj.optString("cityName", "Pune")
            val profile = settingsObj.optString("profileTag", "Single Person")
            val emi = settingsObj.optString("emiTag", "")
            val salary = settingsObj.optLong("inHandSalaryRupees", 0L)
            val scale = settingsObj.optDouble("textScale", 1.0).toFloat()

            settingsRepository.updateCity(city)
            settingsRepository.updateProfile(profile, emi)
            settingsRepository.updateSalary(salary)
            settingsRepository.updateTextScale(scale)
        }

        // Parse Accounts
        val accountsList = mutableListOf<AccountEntity>()
        val accountsArray = root.optJSONArray("accounts")
        if (accountsArray != null) {
            for (i in 0 until accountsArray.length()) {
                val o = accountsArray.getJSONObject(i)
                accountsList.add(
                    AccountEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        type = o.getString("type"),
                        openingBalanceMinor = o.getLong("openingBalanceMinor"),
                        currencyCode = o.getString("currencyCode"),
                        includeInNetWorth = o.getBoolean("includeInNetWorth"),
                        archived = o.getBoolean("archived"),
                        createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                        updatedAtEpochMillis = o.getLong("updatedAtEpochMillis")
                    )
                )
            }
        }

        // Parse Categories
        val categoriesList = mutableListOf<CategoryEntity>()
        val categoriesArray = root.optJSONArray("categories")
        if (categoriesArray != null) {
            for (i in 0 until categoriesArray.length()) {
                val o = categoriesArray.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        iconKey = o.getString("iconKey"),
                        type = o.getString("type"),
                        groupKey = o.getString("groupKey"),
                        sortOrder = o.getInt("sortOrder"),
                        systemCategory = o.getBoolean("systemCategory"),
                        archived = o.getBoolean("archived"),
                        createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                        updatedAtEpochMillis = o.getLong("updatedAtEpochMillis")
                    )
                )
            }
        }

        // Parse Monthly Income Plans
        val incomePlansList = mutableListOf<MonthlyIncomePlanEntity>()
        val incomePlansArray = root.optJSONArray("incomePlans")
        if (incomePlansArray != null) {
            for (i in 0 until incomePlansArray.length()) {
                val o = incomePlansArray.getJSONObject(i)
                val expectedDay = if (o.isNull("expectedDateEpochDay")) null else o.getLong("expectedDateEpochDay")
                incomePlansList.add(
                    MonthlyIncomePlanEntity(
                        id = o.getString("id"),
                        monthKey = o.getInt("monthKey"),
                        name = o.getString("name"),
                        plannedAmountMinor = o.getLong("plannedAmountMinor"),
                        currencyCode = o.getString("currencyCode"),
                        expectedDateEpochDay = expectedDay,
                        sortOrder = o.getInt("sortOrder"),
                        createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                        updatedAtEpochMillis = o.getLong("updatedAtEpochMillis")
                    )
                )
            }
        }

        // Parse Monthly Category Plans
        val categoryPlansList = mutableListOf<MonthlyCategoryPlanEntity>()
        val categoryPlansArray = root.optJSONArray("categoryPlans")
        if (categoryPlansArray != null) {
            for (i in 0 until categoryPlansArray.length()) {
                val o = categoryPlansArray.getJSONObject(i)
                categoryPlansList.add(
                    MonthlyCategoryPlanEntity(
                        monthKey = o.getInt("monthKey"),
                        categoryId = o.getString("categoryId"),
                        plannedAmountMinor = o.getLong("plannedAmountMinor"),
                        currencyCode = o.getString("currencyCode"),
                        createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                        updatedAtEpochMillis = o.getLong("updatedAtEpochMillis")
                    )
                )
            }
        }

        // Parse Transactions
        val transactionsList = mutableListOf<TransactionEntity>()
        val transactionsArray = root.optJSONArray("transactions")
        if (transactionsArray != null) {
            for (i in 0 until transactionsArray.length()) {
                val o = transactionsArray.getJSONObject(i)
                val note = if (o.isNull("note")) null else o.getString("note")
                val merchant = if (o.isNull("merchant")) null else o.getString("merchant")
                val deletedAt = if (o.isNull("deletedAtEpochMillis")) null else o.getLong("deletedAtEpochMillis")
                transactionsList.add(
                    TransactionEntity(
                        id = o.getString("id"),
                        accountId = o.getString("accountId"),
                        categoryId = o.getString("categoryId"),
                        type = o.getString("type"),
                        amountMinor = o.getLong("amountMinor"),
                        currencyCode = o.getString("currencyCode"),
                        localDateEpochDay = o.getLong("localDateEpochDay"),
                        occurredAtEpochMillis = o.getLong("occurredAtEpochMillis"),
                        note = note,
                        merchant = merchant,
                        createdAtEpochMillis = o.getLong("createdAtEpochMillis"),
                        updatedAtEpochMillis = o.getLong("updatedAtEpochMillis"),
                        deletedAtEpochMillis = deletedAt
                    )
                )
            }
        }

        // Atomic Database Upsert in topological dependency order
        // 1. Accounts
        if (accountsList.isNotEmpty()) {
            database.accountDao().upsertAll(accountsList)
        }

        // 2. Categories
        if (categoriesList.isNotEmpty()) {
            database.categoryDao().upsertAll(categoriesList)
        }

        // 3. Income Plans
        if (incomePlansList.isNotEmpty()) {
            database.monthlyPlanDao().upsertIncomePlans(incomePlansList)
        }

        // 4. Category Plans
        if (categoryPlansList.isNotEmpty()) {
            database.monthlyPlanDao().upsertCategoryPlans(categoryPlansList)
        }

        // 5. Transactions
        if (transactionsList.isNotEmpty()) {
            database.transactionDao().upsertAll(transactionsList)
        }

        RestoreSummary(
            accountsRestored = accountsList.size,
            categoriesRestored = categoriesList.size,
            incomePlansRestored = incomePlansList.size,
            categoryPlansRestored = categoryPlansList.size,
            transactionsRestored = transactionsList.size
        )
    }
}

