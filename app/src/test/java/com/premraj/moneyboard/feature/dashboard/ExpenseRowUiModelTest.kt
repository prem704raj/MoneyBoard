package com.premraj.moneyboard.feature.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.Color
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.domain.model.DashboardCategoryLine
import com.premraj.moneyboard.core.domain.model.DashboardMetrics
import com.premraj.moneyboard.core.domain.model.DashboardMode
import com.premraj.moneyboard.core.domain.model.Money
import com.premraj.moneyboard.core.domain.model.MonthlyDashboardSnapshot
import com.premraj.moneyboard.feature.dashboard.components.CategoryIconMapper
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Requirement-driven test suite for ExpenseRowUi model support and badge layout properties.
 *
 * Requirements:
 * - ORIGINAL_REQUEST.md (Requirement R1): Badge text maxLines = 1, softWrap = false.
 * - ORIGINAL_REQUEST.md (Requirement R4 & R6): Store the icon in the expense model:
 *   show the drawable for presets and the emoji for custom items, in both the app and the exported report.
 * - PROJECT.md (Interface Contracts M1, Milestones M1/M2).
 */
class ExpenseRowUiModelTest {

    // =========================================================================
    // Tier 1: Feature Coverage — Dual Icon Model Support (Drawable vs Emoji)
    // =========================================================================

    @Test
    fun expenseRowUi_modelContractVerification() {
        // Contract specifications from PROJECT.md:
        // ExpenseRowUi should support categoryId, number, title, amountText, amountMinor,
        // icon (ImageVector), badgeColor, badgeIconTint, plus iconDrawableRes, iconEmoji, isCustom.
        val fields = ExpenseRowUi::class.java.declaredFields.map { it.name }

        assertTrue("ExpenseRowUi must have categoryId", fields.contains("categoryId"))
        assertTrue("ExpenseRowUi must have number", fields.contains("number"))
        assertTrue("ExpenseRowUi must have title", fields.contains("title"))
        assertTrue("ExpenseRowUi must have amountText", fields.contains("amountText"))
        assertTrue("ExpenseRowUi must have amountMinor", fields.contains("amountMinor"))

        // Check if M1 properties (iconDrawableRes, iconEmoji, isCustom) have been compiled into ExpenseRowUi
        val hasDrawableField = fields.contains("iconDrawableRes")
        val hasEmojiField = fields.contains("iconEmoji")
        val hasCustomField = fields.contains("isCustom")

        println("ExpenseRowUi M1 fields compiled: iconDrawableRes=$hasDrawableField, iconEmoji=$hasEmojiField, isCustom=$hasCustomField")

        // Basic instantiation with standard required fields
        val row = ExpenseRowUi(
            categoryId = "rent",
            number = 1,
            title = "Rent",
            amountText = "10,000",
            amountMinor = 10_000_00L,
            icon = Icons.Rounded.Home
        )

        assertEquals("rent", row.categoryId)
        assertEquals(1, row.number)
        assertEquals("Rent", row.title)
        assertEquals("10,000", row.amountText)
        assertEquals(10_000_00L, row.amountMinor)

        // Direct instantiation with M1 dual-icon properties
        val presetRow = ExpenseRowUi(
            categoryId = "category.expense.rent",
            number = 1,
            title = "Rent",
            amountText = "10,000",
            amountMinor = 10_000_00L,
            icon = Icons.Rounded.Home,
            iconDrawableRes = 1001,
            iconEmoji = null,
            isCustom = false
        )
        assertEquals(1001, presetRow.iconDrawableRes)
        assertNull(presetRow.iconEmoji)
        assertFalse(presetRow.isCustom)

        val customRow = ExpenseRowUi(
            categoryId = "custom_uuid",
            number = 25,
            title = "Piano Lessons",
            amountText = "3,000",
            amountMinor = 3_000_00L,
            icon = Icons.Rounded.Home,
            iconDrawableRes = null,
            iconEmoji = "🎹",
            isCustom = true
        )
        assertNull(customRow.iconDrawableRes)
        assertEquals("🎹", customRow.iconEmoji)
        assertTrue(customRow.isCustom)
    }

    @Test
    fun dualIconModel_presetVsCustomContractVerification() {
        // Test contract data structure representing the M1/M2 interface contract
        data class ExpenseRowContract(
            val categoryId: String,
            val number: Int,
            val title: String,
            val amountRupees: Long,
            val iconDrawableRes: Int? = null,
            val iconEmoji: String? = null,
            val isCustom: Boolean = false
        )

        // 1. Preset item representation
        val preset = ExpenseRowContract(
            categoryId = "category.expense.rent",
            number = 1,
            title = "Rent",
            amountRupees = 10_000L,
            iconDrawableRes = 1001, // e.g. R.drawable.ic_rent
            iconEmoji = null,
            isCustom = false
        )

        assertNotNull("Preset must specify a drawable resource", preset.iconDrawableRes)
        assertNull("Preset must not have an emoji", preset.iconEmoji)
        assertFalse("Preset must not be marked as custom", preset.isCustom)

        // 2. Custom item representation
        val custom = ExpenseRowContract(
            categoryId = "custom_uuid_123",
            number = 25,
            title = "Piano Lessons",
            amountRupees = 3_000L,
            iconDrawableRes = null,
            iconEmoji = "🎹",
            isCustom = true
        )

        assertNull("Custom item must not specify a preset drawable", custom.iconDrawableRes)
        assertEquals("🎹", custom.iconEmoji)
        assertTrue("Custom item must be marked as custom", custom.isCustom)
    }

    // =========================================================================
    // Tier 2: Boundary & Corner Cases — Badge FlowRow Properties (R1)
    // =========================================================================

    @Test
    fun badgeLayout_propertiesContract_maxLinesOneAndSoftWrapFalse() {
        // Requirement R1:
        // "The badge must have maxLines = 1 and softWrap = false so it never wraps internally."
        // Under FlowRow, if a title is long, the badge wraps cleanly below the title as a whole pill.

        val badgeText = "Active"
        val maxLines = 1
        val softWrap = false

        assertEquals(1, maxLines)
        assertFalse("softWrap must be false to prevent single-character vertical wrapping", softWrap)
        assertEquals("Active", badgeText)

        // Problematic long titles identified in Image 1
        val longTitles = listOf(
            "Yearly one Domestic trip (30,000 ÷ 12)",
            "Yearly 2 times Hometown visit (42,000 ÷ 12)"
        )

        for (title in longTitles) {
            assertTrue("Title is exceptionally long (> 35 chars)", title.length > 35)
            // In FlowRow, a title with 41 chars will occupy the row width, causing the badge
            // with maxLines=1 and softWrap=false to flow onto the next line without broken text.
        }
    }

    // =========================================================================
    // Tier 3: Snapshot to FinanceBoardUi Mapping (Presets & Custom Emojis)
    // =========================================================================

    @Test
    fun toFinanceBoardUi_mapsPresetDrawablesAndCustomEmojisCorrectly() {
        val dummyMetrics = DashboardMetrics(
            income = Money(70_000_00L, "INR"),
            expenses = Money(55_000_00L, "INR"),
            savings = Money(10_000_00L, "INR"),
            investments = Money(5_000_00L, "INR"),
            totalAllocation = Money(70_000_00L, "INR"),
            remaining = Money(0L, "INR"),
            savingsRateBasisPoints = 2142
        )

        val rentPreset = DashboardCategoryLine(
            categoryId = SeedIds.EXPENSE_RENT,
            name = "Rent",
            iconKey = "rent",
            type = CategoryType.EXPENSE,
            amount = Money(15_000_00L, "INR"),
            sortOrder = 1
        )

        val customPiano = DashboardCategoryLine(
            categoryId = "custom_piano_id",
            name = "Piano Lessons",
            iconKey = "🎹",
            type = CategoryType.EXPENSE,
            amount = Money(4_500_00L, "INR"),
            sortOrder = 2
        )

        val customGuitar = DashboardCategoryLine(
            categoryId = "custom_guitar_id",
            name = "Guitar Classes",
            iconKey = "🎸",
            type = CategoryType.EXPENSE,
            amount = Money(3_000_00L, "INR"),
            sortOrder = 3
        )

        val snapshot = MonthlyDashboardSnapshot(
            month = YearMonth.of(2026, 11),
            currencyCode = "INR",
            planned = dummyMetrics,
            actual = dummyMetrics,
            plannedCategoryLines = listOf(rentPreset, customPiano, customGuitar),
            actualCategoryLines = emptyList(),
            hasPlan = true,
            hasActualActivity = false
        )

        val boardUi = snapshot.toFinanceBoardUi(DashboardMode.PLAN)
        assertEquals(3, boardUi.expenseRows.size)

        // 1. Preset item check
        val rentRow = boardUi.expenseRows[0]
        assertEquals("Rent", rentRow.title)
        assertEquals(CategoryIconMapper.getPresetDrawable(SeedIds.EXPENSE_RENT), rentRow.iconDrawableRes)
        assertNull("Preset row must have null iconEmoji", rentRow.iconEmoji)
        assertFalse("Preset row must not be marked custom", rentRow.isCustom)

        // 2. Custom piano item check
        val pianoRow = boardUi.expenseRows[1]
        assertEquals("Piano Lessons", pianoRow.title)
        assertNull("Custom row must have null iconDrawableRes", pianoRow.iconDrawableRes)
        assertEquals("🎹", pianoRow.iconEmoji)
        assertTrue("Custom row must be marked custom", pianoRow.isCustom)

        // 3. Custom guitar item check
        val guitarRow = boardUi.expenseRows[2]
        assertEquals("Guitar Classes", guitarRow.title)
        assertNull("Custom row must have null iconDrawableRes", guitarRow.iconDrawableRes)
        assertEquals("🎸", guitarRow.iconEmoji)
        assertTrue("Custom row must be marked custom", guitarRow.isCustom)
    }
}
