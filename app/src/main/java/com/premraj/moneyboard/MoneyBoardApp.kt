package com.premraj.moneyboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import com.premraj.moneyboard.core.export.BackupPreview
import com.premraj.moneyboard.core.export.FullDatabaseBackupManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.designsystem.MoneyBoardTheme
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.TransactionType
import com.premraj.moneyboard.core.domain.repository.CreateCategoryCommand
import com.premraj.moneyboard.core.domain.repository.CreateTransactionCommand
import com.premraj.moneyboard.core.domain.repository.SetCategoryPlanCommand
import com.premraj.moneyboard.core.domain.repository.SetIncomePlanCommand
import com.premraj.moneyboard.core.export.DataBackupHelper
import com.premraj.moneyboard.core.export.MonthlyReportCsvGenerator
import com.premraj.moneyboard.core.export.MonthlyReportPdfGenerator
import com.premraj.moneyboard.core.export.ReportExportManager
import com.premraj.moneyboard.core.export.ReportShareHelper
import com.premraj.moneyboard.core.settings.ThemeMode
import com.premraj.moneyboard.core.settings.UserSettings
import com.premraj.moneyboard.feature.dashboard.DashboardPreviewData
import com.premraj.moneyboard.feature.dashboard.DashboardViewModel
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import com.premraj.moneyboard.feature.dashboard.FinanceBoardScreen
import com.premraj.moneyboard.feature.dashboard.FinanceBoardUiModel
import com.premraj.moneyboard.feature.dashboard.components.AddExpenseDialog
import com.premraj.moneyboard.feature.dashboard.components.EditCityQuickDialog
import com.premraj.moneyboard.feature.dashboard.components.EditExpenseDialog
import com.premraj.moneyboard.feature.dashboard.components.EditSalaryQuickDialog
import com.premraj.moneyboard.feature.dashboard.components.LogTransactionDialog
import com.premraj.moneyboard.feature.dashboard.toFinanceBoardUi
import com.premraj.moneyboard.feature.export.ExpenseReportSheet
import com.premraj.moneyboard.feature.settings.SettingsSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyBoardApp() {
    val context = LocalContext.current
    val app = context.applicationContext as? MoneyBoardApplication
    val scope = rememberCoroutineScope()

    if (app != null) {
        val settingsRepo = app.appContainer.userSettingsRepository
        val userSettings by settingsRepo.settings.collectAsStateWithLifecycle()

        val viewModel: DashboardViewModel = viewModel(
            factory = DashboardViewModel.factory(
                observeMonthlyDashboard = app.appContainer.observeMonthlyDashboardUseCase,
                initialMonth = java.time.YearMonth.now()
            )
        )
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        val isDarkTheme = when (userSettings.themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        var showSettings by remember { mutableStateOf(false) }
        var editingExpense by remember { mutableStateOf<ExpenseRowUi?>(null) }
        var showAddExpenseDialog by remember { mutableStateOf(false) }
        var showLogTransactionDialog by remember { mutableStateOf(false) }
        var showEditSalaryDialog by remember { mutableStateOf(false) }
        var showEditCityDialog by remember { mutableStateOf(false) }
        var isExporting by remember { mutableStateOf(false) }
        var exportingMessage by remember { mutableStateOf<String?>(null) }
        var showRestoreConfirmDialog by remember { mutableStateOf(false) }
        var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
        var restorePreviewData by remember { mutableStateOf<BackupPreview?>(null) }

        val restoreLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(Charsets.UTF_8).readText()
                    } ?: throw IllegalStateException("Could not open selected backup file")

                    val previewRes = FullDatabaseBackupManager.previewBackup(jsonString)
                    if (previewRes.isFailure) {
                        val err = previewRes.exceptionOrNull()?.message ?: "Invalid backup file"
                        Toast.makeText(context, "Cannot restore backup: $err", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    pendingRestoreJson = jsonString
                    restorePreviewData = previewRes.getOrThrow()
                    showRestoreConfirmDialog = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Helper to extract current active board
        val currentBoard: FinanceBoardUiModel = uiState.snapshot?.toFinanceBoardUi(uiState.mode)
            ?: FinanceBoardUiModel(
                salaryText = "₹${userSettings.inHandSalaryRupees} / month",
                totalAllocationText = "₹69,350",
                expenseRows = DashboardPreviewData.expenses,
                salaryVsExpenses = DashboardPreviewData.salaryVsExpenses,
                savings = DashboardPreviewData.savings,
                living = DashboardPreviewData.living,
                annual = DashboardPreviewData.annual,
                chartLines = emptyList(),
                donutSlices = com.premraj.moneyboard.feature.dashboard.components.defaultDonutSlices(),
                remainingText = "₹650 / month",
                savingsRateText = "20.2%",
                totalSavingsText = "₹14,000 / month",
                actualSpendingText = "₹55,350 / month",
                annualBalanceText = "₹7,800"
            )

        val planBoard: FinanceBoardUiModel = uiState.snapshot?.toFinanceBoardUi(com.premraj.moneyboard.core.domain.model.DashboardMode.PLAN)
            ?: currentBoard
        val actualBoard: FinanceBoardUiModel? = uiState.snapshot?.toFinanceBoardUi(com.premraj.moneyboard.core.domain.model.DashboardMode.ACTUAL)

        fun exportImage() {
            val activity = context as? ComponentActivity
            if (activity == null) {
                Toast.makeText(context, "Export requires active activity context", Toast.LENGTH_SHORT).show()
                return
            }
            if (isExporting) return
            isExporting = true
            exportingMessage = "Exporting 1224px Image..."
            scope.launch {
                try {
                    val bitmap = ReportExportManager.captureComposableBitmap(activity) {
                        ExpenseReportSheet(
                            board = planBoard,
                            actualBoard = actualBoard,
                            settings = userSettings,
                            selectedMonth = uiState.selectedMonth
                        )
                    }
                    val filename = "MoneyBoard_Report_${uiState.selectedMonth.year}_${uiState.selectedMonth.monthValue}_${System.currentTimeMillis()}.png"
                    val uri = withContext(Dispatchers.IO) {
                        ReportExportManager.savePngToMediaStore(activity, bitmap, filename)
                    }
                    if (uri != null) {
                        Toast.makeText(context, "Report saved to Pictures/MoneyBoard", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to save image to Pictures/MoneyBoard", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Image export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isExporting = false
                    exportingMessage = null
                }
            }
        }

        fun exportPdf() {
            val activity = context as? ComponentActivity
            if (activity == null) {
                Toast.makeText(context, "Export requires active activity context", Toast.LENGTH_SHORT).show()
                return
            }
            if (isExporting) return
            isExporting = true
            exportingMessage = "Exporting 1224px PDF Report..."
            scope.launch {
                try {
                    val bitmap = ReportExportManager.captureComposableBitmap(activity) {
                        ExpenseReportSheet(
                            board = planBoard,
                            actualBoard = actualBoard,
                            settings = userSettings,
                            selectedMonth = uiState.selectedMonth
                        )
                    }
                    val filename = "MoneyBoard_Report_${uiState.selectedMonth.year}_${uiState.selectedMonth.monthValue}_${System.currentTimeMillis()}.pdf"
                    val uri = withContext(Dispatchers.IO) {
                        ReportExportManager.savePdfToMediaStore(activity, bitmap, filename)
                    }
                    if (uri != null) {
                        Toast.makeText(context, "PDF saved to Documents/MoneyBoard", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to save PDF to Documents/MoneyBoard", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "PDF export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isExporting = false
                    exportingMessage = null
                }
            }
        }

        fun exportCsv() {
            runCatching {
                val csvFile = MonthlyReportCsvGenerator.generateCsv(
                    context = context,
                    board = planBoard,
                    actualBoard = actualBoard,
                    month = uiState.selectedMonth,
                    settings = userSettings
                )
                ReportShareHelper.shareFile(
                    context = context,
                    file = csvFile,
                    mimeType = "text/csv",
                    chooserTitle = "Share Monthly Expenses CSV"
                )
            }.onFailure {
                Toast.makeText(context, "Failed to export CSV: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        fun resetToDemoPlan() {
            scope.launch {
                app.appContainer.referenceDemoPlanSeeder.forceSeed(uiState.selectedMonth)
                Toast.makeText(context, "Reference plan reloaded successfully", Toast.LENGTH_SHORT).show()
            }
        }

        fun updateSalary(newSalary: Long) {
            settingsRepo.updateSalary(newSalary)
            scope.launch {
                app.appContainer.monthlyPlanRepository.setIncomePlan(
                    SetIncomePlanCommand(
                        id = "debug.salary.${uiState.selectedMonth.year}.${uiState.selectedMonth.monthValue}",
                        month = uiState.selectedMonth,
                        name = "In-hand Salary",
                        plannedAmount = Money(amountMinor = Math.multiplyExact(newSalary, 100L), currencyCode = "INR"),
                        expectedDate = uiState.selectedMonth.atDay(1),
                        sortOrder = 10
                    )
                )
            }
        }

        fun resolveCategoryId(expense: ExpenseRowUi): String {
            if (expense.categoryId.isNotBlank()) return expense.categoryId
            val titleLower = expense.title.lowercase()
            return when {
                "rent" in titleLower -> SeedIds.EXPENSE_RENT
                "grocer" in titleLower -> SeedIds.EXPENSE_GROCERIES
                "gas" in titleLower -> SeedIds.EXPENSE_GAS
                "maid" in titleLower || "house_help" in titleLower -> SeedIds.EXPENSE_HOUSE_HELP
                "food" in titleLower || "dining" in titleLower -> SeedIds.EXPENSE_DINING
                "electric" in titleLower -> SeedIds.EXPENSE_ELECTRICITY
                "wi-fi" in titleLower || "wifi" in titleLower -> SeedIds.EXPENSE_WIFI
                "mobile" in titleLower || "recharge" in titleLower -> SeedIds.EXPENSE_MOBILE
                "cab" in titleLower || "rapido" in titleLower -> SeedIds.EXPENSE_CAB
                "laundry" in titleLower -> SeedIds.EXPENSE_LAUNDRY
                "shopping" in titleLower -> SeedIds.EXPENSE_SHOPPING
                "diet" in titleLower -> SeedIds.EXPENSE_FITNESS_DIET
                "gym" in titleLower -> SeedIds.EXPENSE_GYM
                "salon" in titleLower || "saloon" in titleLower || "skin" in titleLower -> SeedIds.EXPENSE_SALON
                "outing" in titleLower -> SeedIds.EXPENSE_OUTING
                "domestic" in titleLower || "trip" in titleLower || "travel" in titleLower -> SeedIds.EXPENSE_TRAVEL
                "hometown" in titleLower -> SeedIds.EXPENSE_HOMETOWN
                "medicine" in titleLower || "med" in titleLower -> SeedIds.EXPENSE_MEDICINE
                "parent" in titleLower || "family" in titleLower -> SeedIds.EXPENSE_FAMILY_SUPPORT
                "sip" in titleLower -> SeedIds.INVESTMENT_SIP
                "health" in titleLower || "insurance" in titleLower -> SeedIds.EXPENSE_HEALTH_INSURANCE
                "lic" in titleLower -> SeedIds.SAVING_LIC_POLICY
                "emergency" in titleLower -> SeedIds.SAVING_EMERGENCY_FUND
                else -> SeedIds.EXPENSE_OTHER
            }
        }

        fun updateExpenseAmount(categoryId: String, newRupees: Long) {
            scope.launch {
                try {
                    val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                    app.appContainer.monthlyPlanRepository.setCategoryPlan(
                        SetCategoryPlanCommand(
                            month = uiState.selectedMonth,
                            categoryId = validCatId,
                            plannedAmount = Money(amountMinor = Math.multiplyExact(newRupees, 100L), currencyCode = "INR")
                        )
                    )
                    Toast.makeText(context, "Budget item updated", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to update budget: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun addCustomExpense(
            name: String,
            amountRupees: Long,
            emoji: String,
            type: CategoryType,
            group: CategoryGroup
        ) {
            scope.launch {
                try {
                    val category = app.appContainer.categoryRepository.createCategory(
                        CreateCategoryCommand(
                            name = name,
                            iconKey = emoji,
                            type = type,
                            group = group
                        )
                    )
                    app.appContainer.monthlyPlanRepository.setCategoryPlan(
                        SetCategoryPlanCommand(
                            month = uiState.selectedMonth,
                            categoryId = category.id,
                            plannedAmount = Money(amountMinor = Math.multiplyExact(amountRupees, 100L), currencyCode = "INR")
                        )
                    )
                    Toast.makeText(context, "Added custom expense: $name", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to add custom expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun removeExpense(categoryId: String) {
            scope.launch {
                try {
                    val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                    app.appContainer.monthlyPlanRepository.deleteCategoryPlan(
                        month = uiState.selectedMonth,
                        categoryId = validCatId
                    )
                    val cat = app.appContainer.categoryRepository.getCategory(validCatId)
                    if (cat != null && !cat.systemCategory) {
                        app.appContainer.categoryRepository.archiveCustomCategory(validCatId)
                    }
                    Toast.makeText(context, "Expense removed from budget", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to remove item: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun updateActualExpense(categoryId: String, newRupees: Long) {
            scope.launch {
                try {
                    val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                    val transactions = app.appContainer.transactionRepository.observeTransactions(uiState.selectedMonth).first()
                    val existing = transactions.firstOrNull { it.categoryId == validCatId }
                    if (newRupees == 0L) {
                        if (existing != null) {
                            app.appContainer.transactionRepository.softDeleteTransaction(existing.id)
                        }
                        Toast.makeText(context, "Actual expense reset to ₹0", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val category = app.appContainer.categoryRepository.getCategory(validCatId)
                    val txType = when (category?.type) {
                        CategoryType.SAVING -> TransactionType.SAVING
                        CategoryType.INVESTMENT -> TransactionType.INVESTMENT
                        else -> TransactionType.EXPENSE
                    }
                    val newMoney = Money(amountMinor = Math.multiplyExact(newRupees, 100L), currencyCode = "INR")

                    if (existing != null) {
                        app.appContainer.transactionRepository.updateTransaction(
                            existing.copy(amount = newMoney)
                        )
                    } else {
                        app.appContainer.transactionRepository.createTransaction(
                            CreateTransactionCommand(
                                accountId = SeedIds.CASH_ACCOUNT,
                                categoryId = validCatId,
                                type = txType,
                                amount = newMoney,
                                accountingDate = if (uiState.selectedMonth == java.time.YearMonth.now()) LocalDate.now() else uiState.selectedMonth.atDay(1),
                                occurredAt = Instant.now(),
                                merchant = null,
                                note = "Updated via actual dashboard"
                            )
                        )
                    }
                    Toast.makeText(context, "Actual expense updated: ₹$newRupees", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to record expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun clearActualExpensesForCategory(categoryId: String) {
            scope.launch {
                try {
                    val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                    val transactions = app.appContainer.transactionRepository.observeTransactions(uiState.selectedMonth).first()
                    val matching = transactions.filter { it.categoryId == validCatId }
                    matching.forEach { tx ->
                        app.appContainer.transactionRepository.softDeleteTransaction(tx.id)
                    }
                    Toast.makeText(context, "Actual expenses cleared", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to clear: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun logTransaction(
            categoryId: String,
            amountRupees: Long,
            merchant: String?,
            note: String?,
            accountId: String
        ) {
            scope.launch {
                try {
                    val validCatId = if (categoryId.isNotBlank()) categoryId else SeedIds.EXPENSE_OTHER
                    val category = app.appContainer.categoryRepository.getCategory(validCatId)
                    val txType = when (category?.type) {
                        CategoryType.SAVING -> TransactionType.SAVING
                        CategoryType.INVESTMENT -> TransactionType.INVESTMENT
                        else -> TransactionType.EXPENSE
                    }
                    app.appContainer.transactionRepository.createTransaction(
                        CreateTransactionCommand(
                            accountId = accountId,
                            categoryId = validCatId,
                            type = txType,
                            amount = Money(amountMinor = Math.multiplyExact(amountRupees, 100L), currencyCode = "INR"),
                            accountingDate = LocalDate.now(),
                            occurredAt = Instant.now(),
                            merchant = merchant,
                            note = note
                        )
                    )
                    Toast.makeText(context, "Recorded actual expense: ₹$amountRupees", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to log transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun exportBackup() {
            scope.launch {
                runCatching {
                    val backupFile = DataBackupHelper.createFullBackupFile(
                        context = context,
                        database = app.appContainer.database,
                        settings = userSettings
                    )
                    ReportShareHelper.shareFile(
                        context = context,
                        file = backupFile,
                        mimeType = "application/json",
                        chooserTitle = "Export SalaryLane Full Database Backup"
                    )
                }.onFailure {
                    Toast.makeText(context, "Backup export failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        MoneyBoardTheme(
            darkTheme = isDarkTheme,
            textScale = userSettings.textScale
        ) {
            BackHandler(enabled = uiState.selectedMonth != YearMonth.now()) {
                viewModel.resetToCurrentMonth()
            }

            FinanceBoardScreen(
                uiState = uiState,
                userSettings = userSettings,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onResetToCurrentMonth = viewModel::resetToCurrentMonth,
                onModeSelected = viewModel::selectMode,
                onExportPdf = ::exportPdf,
                onExportImage = ::exportImage,
                onOpenSettings = { showSettings = true },
                onExpenseClick = { editingExpense = it },
                onAddExpenseClick = { showAddExpenseDialog = true },
                onLogTransaction = { showLogTransactionDialog = true },
                onEditSalary = { showEditSalaryDialog = true },
                onEditCity = { showEditCityDialog = true }
            )

            // Dialog for editing an existing item or removing it (e.g. if user does not pay rent)
            editingExpense?.let { expense ->
                val resolvedCatId = resolveCategoryId(expense)
                EditExpenseDialog(
                    expense = expense.copy(categoryId = resolvedCatId),
                    mode = uiState.mode,
                    onDismiss = { editingExpense = null },
                    onSaveAmount = { newRupees ->
                        if (uiState.mode == DashboardMode.ACTUAL) {
                            updateActualExpense(resolvedCatId, newRupees)
                        } else {
                            updateExpenseAmount(resolvedCatId, newRupees)
                        }
                        editingExpense = null
                    },
                    onRemoveExpense = {
                        if (uiState.mode == DashboardMode.ACTUAL) {
                            clearActualExpensesForCategory(resolvedCatId)
                        } else {
                            removeExpense(resolvedCatId)
                        }
                        editingExpense = null
                    }
                )
            }

            // Dialog for adding or restoring a category
            if (showAddExpenseDialog) {
                val activeCategoryIds = currentBoard.expenseRows.map { it.categoryId }.toSet()
                AddExpenseDialog(
                    existingCategoryIds = activeCategoryIds,
                    onDismiss = { showAddExpenseDialog = false },
                    onAddExpense = { catId, amount ->
                        updateExpenseAmount(catId, amount)
                        showAddExpenseDialog = false
                    },
                    onAddCustomExpense = { name, amount, emoji, type, group ->
                        addCustomExpense(name, amount, emoji, type, group)
                        showAddExpenseDialog = false
                    }
                )
            }

            // Dialog for logging an actual expense transaction
            if (showLogTransactionDialog) {
                LogTransactionDialog(
                    availableCategories = currentBoard.expenseRows,
                    onDismiss = { showLogTransactionDialog = false },
                    onSaveTransaction = { catId, amt, merch, note, acc ->
                        logTransaction(catId, amt, merch, note, acc)
                        showLogTransactionDialog = false
                    }
                )
            }

            // Quick dialog to edit in-hand salary directly from header
            if (showEditSalaryDialog) {
                EditSalaryQuickDialog(
                    currentSalaryRupees = userSettings.inHandSalaryRupees,
                    onDismiss = { showEditSalaryDialog = false },
                    onSaveSalary = { newSalary ->
                        updateSalary(newSalary)
                        showEditSalaryDialog = false
                    }
                )
            }

            // Quick dialog to change city location directly from header
            if (showEditCityDialog) {
                EditCityQuickDialog(
                    currentCity = userSettings.cityName,
                    onDismiss = { showEditCityDialog = false },
                    onSaveCity = { newCity ->
                        settingsRepo.updateCity(newCity)
                        showEditCityDialog = false
                    }
                )
            }

            if (showSettings) {
                SettingsSheet(
                    settings = userSettings,
                    onDismiss = { showSettings = false },
                    onUpdateCity = settingsRepo::updateCity,
                    onUpdateProfile = settingsRepo::updateProfile,
                    onUpdateSalary = ::updateSalary,
                    onUpdateTheme = settingsRepo::updateTheme,
                    onUpdateDefaultMode = { mode ->
                        settingsRepo.updateDefaultMode(mode)
                        viewModel.selectMode(mode)
                    },
                    onUpdateTextScale = settingsRepo::updateTextScale,
                    onExportPdf = ::exportPdf,
                    onExportImage = ::exportImage,
                    onExportCsv = ::exportCsv,
                    onExportBackup = ::exportBackup,
                    onRestoreBackup = { restoreLauncher.launch("*/*") },
                    onResetToDemoPlan = ::resetToDemoPlan
                )
            }

            if (showRestoreConfirmDialog && restorePreviewData != null) {
                val p = restorePreviewData!!
                AlertDialog(
                    onDismissRequest = {
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                        restorePreviewData = null
                    },
                    title = {
                        Text(
                            text = "Restore SalaryLane Backup?",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Found valid offline backup archive:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "• Accounts: ${p.accountCount}\n" +
                                        "• Categories: ${p.categoryCount}\n" +
                                        "• Monthly Plans: ${p.categoryPlanCount + p.incomePlanCount}\n" +
                                        "• Transactions: ${p.transactionCount}\n" +
                                        "• Primary City: ${p.cityName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Restoring will import all historical records into your local Room database safely.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val jsonToRestore = pendingRestoreJson
                                showRestoreConfirmDialog = false
                                pendingRestoreJson = null
                                restorePreviewData = null
                                if (jsonToRestore != null) {
                                    scope.launch {
                                        try {
                                            val summary = FullDatabaseBackupManager.restoreAllData(
                                                database = app.appContainer.database,
                                                settingsRepository = settingsRepo,
                                                jsonString = jsonToRestore
                                            ).getOrThrow()
                                            Toast.makeText(
                                                context,
                                                "Restore complete! Imported ${summary.transactionsRestored} transactions & ${summary.categoryPlansRestored} plans.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Restore failed: ${e.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Restore Now", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showRestoreConfirmDialog = false
                                pendingRestoreJson = null
                                restorePreviewData = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            text = exportingMessage ?: "Exporting report...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    } else {
        MoneyBoardTheme {
            FinanceBoardScreen()
        }
    }
}
