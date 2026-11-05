package com.premraj.moneyboard.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Slider
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.premraj.moneyboard.core.designsystem.MoneyBoardColors
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.settings.ThemeMode
import com.premraj.moneyboard.core.settings.UserSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: UserSettings,
    onDismiss: () -> Unit,
    onUpdateCity: (String) -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onUpdateSalary: (Long) -> Unit,
    onUpdateTheme: (ThemeMode) -> Unit,
    onUpdateDefaultMode: (DashboardMode) -> Unit,
    onUpdateTextScale: (Float) -> Unit = {},
    onExportPdf: () -> Unit,
    onExportImage: () -> Unit = {},
    onExportCsv: () -> Unit,
    onExportBackup: () -> Unit = {},
    onRestoreBackup: () -> Unit = {},
    onResetToDemoPlan: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showCompetitorDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                }
            }

            // 1. Profile & Demographic Section
            SettingsSection(title = "Profile & Demographics") {
                SettingsActionCard(
                    title = "City & Location",
                    subtitle = settings.cityName,
                    icon = Icons.Rounded.LocationCity,
                    iconBg = MoneyBoardColors.BadgeSkyBlue,
                    onClick = { showEditProfileDialog = true }
                )

                SettingsActionCard(
                    title = "Profile Tag & EMI Status",
                    subtitle = if (settings.emiTag.isNotBlank() && !settings.emiTag.equals("No EMI", ignoreCase = true)) {
                        "${settings.profileTag} • ${settings.emiTag}"
                    } else {
                        settings.profileTag
                    },
                    icon = Icons.Rounded.Person,
                    iconBg = MoneyBoardColors.BadgePurple,
                    onClick = { showEditProfileDialog = true }
                )

                SettingsActionCard(
                    title = "In-Hand Salary",
                    subtitle = "₹${settings.inHandSalaryRupees} / month",
                    icon = Icons.Rounded.Payments,
                    iconBg = MoneyBoardColors.BadgeTeal,
                    onClick = { showEditProfileDialog = true }
                )
            }

            // 2. Reports & Data Export Section
            SettingsSection(title = "Reports & Export") {
                SettingsActionCard(
                    title = "Export as Image",
                    subtitle = "Save 1224px high-resolution PNG report to Pictures/MoneyBoard",
                    icon = Icons.Rounded.Image,
                    iconBg = MoneyBoardColors.BadgeBlue,
                    onClick = {
                        onExportImage()
                        onDismiss()
                    }
                )

                SettingsActionCard(
                    title = "Export as PDF",
                    subtitle = "Unified 1224px printable vector statement in Documents/MoneyBoard",
                    icon = Icons.Rounded.PictureAsPdf,
                    iconBg = MoneyBoardColors.BadgeRed,
                    onClick = {
                        onExportPdf()
                        onDismiss()
                    }
                )

                SettingsActionCard(
                    title = "Export CSV Spreadsheet",
                    subtitle = "Raw dataset of expenses, categories, and totals for Excel / Sheets",
                    icon = Icons.Rounded.TableChart,
                    iconBg = MoneyBoardColors.BadgeGreen,
                    onClick = {
                        onExportCsv()
                        onDismiss()
                    }
                )

                SettingsActionCard(
                    title = "Export Full Database Backup (JSON)",
                    subtitle = "Complete machine-readable JSON archive of all months, accounts, and transactions",
                    icon = Icons.Rounded.Download,
                    iconBg = MoneyBoardColors.BadgePurple,
                    onClick = {
                        onExportBackup()
                        onDismiss()
                    }
                )

                SettingsActionCard(
                    title = "Restore from JSON Backup",
                    subtitle = "Safely restore your full database from a previously saved JSON backup",
                    icon = Icons.Rounded.Refresh,
                    iconBg = MoneyBoardColors.BadgeTeal,
                    onClick = {
                        onRestoreBackup()
                        onDismiss()
                    }
                )
            }

            // 3. Appearance & Mode
            SettingsSection(title = "Appearance & Display") {
                // Text Size Controls (Increase / Decrease)
                val currentPct = (settings.textScale * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FormatSize,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Text Size Scale",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                        )
                    }

                    Text(
                        text = "$currentPct%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        ),
                        color = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newScale = ((settings.textScale - 0.05f) * 100).toInt() / 100f
                            onUpdateTextScale(newScale.coerceAtLeast(0.75f))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDark) Color(0xFF16324F) else Color(0xFFE2E8F0),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "Decrease Text Size",
                            tint = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Slider(
                        value = settings.textScale,
                        onValueChange = {
                            val rounded = ((it * 100).toInt()) / 100f
                            onUpdateTextScale(rounded)
                        },
                        valueRange = 0.75f..1.50f,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val newScale = ((settings.textScale + 0.05f) * 100).toInt() / 100f
                            onUpdateTextScale(newScale.coerceAtMost(1.50f))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDark) Color(0xFF16324F) else Color(0xFFE2E8F0),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Increase Text Size",
                            tint = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Navy900,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Quick preset buttons: Small (85%), Normal (100%), Medium (115%), Large (130%), XL (145%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.85f to "Small",
                        1.00f to "Normal",
                        1.15f to "Medium",
                        1.30f to "Large",
                        1.45f to "XL"
                    ).forEach { (scale, label) ->
                        val isSelected = kotlin.math.abs(settings.textScale - scale) < 0.04f
                        ThemeOptionButton(
                            label = "$label\n${(scale * 100).toInt()}%",
                            selected = isSelected,
                            onClick = { onUpdateTextScale(scale) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = "System",
                        selected = settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onUpdateTheme(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = "Light",
                        selected = settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onUpdateTheme(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = "Dark",
                        selected = settings.themeMode == ThemeMode.DARK,
                        onClick = { onUpdateTheme(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Default Dashboard Mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionButton(
                        label = "PLAN",
                        selected = settings.defaultMode == DashboardMode.PLAN,
                        onClick = { onUpdateDefaultMode(DashboardMode.PLAN) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        label = "ACTUAL",
                        selected = settings.defaultMode == DashboardMode.ACTUAL,
                        onClick = { onUpdateDefaultMode(DashboardMode.ACTUAL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Data Management Section
            SettingsSection(title = "Data Management") {
                SettingsActionCard(
                    title = "Reset to 24-Category Reference Plan",
                    subtitle = "Restores exact ₹69,350 reference expense breakdown from infographic",
                    icon = Icons.Rounded.Refresh,
                    iconBg = MoneyBoardColors.BadgeOrange,
                    onClick = { showResetConfirmDialog = true }
                )
            }

            // 5. About Section & Competitor Benchmarks
            SettingsSection(title = "Why MoneyBoard & Benchmarks") {
                SettingsActionCard(
                    title = "Why MoneyBoard? (vs Competitors)",
                    subtitle = "Compare vs YNAB, Walnut, Monefy, Spendee • Privacy & Speed",
                    icon = Icons.Rounded.Info,
                    iconBg = MoneyBoardColors.Navy900,
                    onClick = { showCompetitorDialog = true }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SalaryLane v0.3.0",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                        )
                        Text(
                            text = "100% Offline & Private Financial Management.\nAll data is stored exclusively on your device in Room SQLite.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                        )
                    }
                }
            }
        }
    }

    // Competitor Comparison Dialog
    if (showCompetitorDialog) {
        CompetitorComparisonDialog(
            onDismiss = { showCompetitorDialog = false }
        )
    }

    // Edit Profile & Salary Dialog
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentCity = settings.cityName,
            currentProfile = settings.profileTag,
            currentEmi = settings.emiTag,
            currentSalary = settings.inHandSalaryRupees,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { newCity, newProfile, newEmi, newSalary ->
                onUpdateCity(newCity)
                onUpdateProfile(newProfile, newEmi)
                onUpdateSalary(newSalary)
                showEditProfileDialog = false
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
            titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
            textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
            title = { Text("Reset to Reference Plan?") },
            text = { Text("This will reload the 24 reference categories and rupee amounts totalling ₹69,350 as displayed in the infographic poster.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetToDemoPlan()
                        showResetConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Reset Plan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = if (isDark) Color(0xFF90CAF9) else MoneyBoardColors.Blue700
        )
        content()
    }
}

@Composable
private fun SettingsActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isDark) MoneyBoardColors.DarkSurface else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = if (isDark) MoneyBoardColors.DarkTextSecondary.copy(alpha = 0.7f) else MoneyBoardColors.TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    val selectedBg = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900
    val unselectedBg = if (isDark) MoneyBoardColors.DarkSurfaceVariant else MoneyBoardColors.BackgroundLight
    val borderColor = if (selected) selectedBg else if (isDark) Color(0xFF22354E) else MoneyBoardColors.LedgerLine

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) selectedBg else unselectedBg)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (selected) Color.White else if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary
        )
    }
}

@Composable
private fun EditProfileDialog(
    currentCity: String,
    currentProfile: String,
    currentEmi: String,
    currentSalary: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == MoneyBoardColors.DarkBackground
    var city by remember { mutableStateOf(currentCity) }
    var profile by remember { mutableStateOf(currentProfile) }
    var emi by remember {
        mutableStateOf(if (currentEmi.equals("No EMI", ignoreCase = true)) "" else currentEmi)
    }
    var salary by remember { mutableStateOf(currentSalary.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) MoneyBoardColors.DarkSurface else MoneyBoardColors.SurfaceLight,
        titleContentColor = if (isDark) MoneyBoardColors.DarkTextPrimary else MoneyBoardColors.TextPrimary,
        textContentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary,
        title = { Text("Edit Profile & Budget") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City (e.g. Pune)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = profile,
                    onValueChange = { profile = it },
                    label = { Text("Profile Tag (e.g. Single Person)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emi,
                    onValueChange = { emi = it },
                    label = { Text("EMI Status (e.g. EMI ₹5,000)") },
                    placeholder = { Text("Optional - leave empty if no EMI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it.filter { char -> char.isDigit() } },
                    label = { Text("Monthly In-hand Salary (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salNum = salary.toLongOrNull() ?: 70_000L
                    onConfirm(city, profile, emi, salNum)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) MoneyBoardColors.Blue600 else MoneyBoardColors.Navy900,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (isDark) MoneyBoardColors.DarkTextSecondary else MoneyBoardColors.TextSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

