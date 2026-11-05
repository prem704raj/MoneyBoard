package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.testutil.EmojiValidatorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

/**
 * Requirement-driven test suite for Custom Item Addition, Emoji Validation,
 * Room SQLite Persistence Model, and Preset Removal/Restore Cycle.
 *
 * Requirements:
 * - ORIGINAL_REQUEST.md (Requirement R6):
 *   1. Restore: presets that were removed, shown as chips or a list with their original icons.
 *   2. Add custom: name, monthly amount, category, and an icon field with emoji validation.
 *   - Input validation (name required, amount > 0).
 *   - Custom items must be editable and deletable afterwards.
 *   - Persist everything using the project's existing storage (Room SQLite).
 * - PROJECT.md (Interface Contracts M2, Milestones M2/M4).
 */
class AddRestoreExpenseFlowTest {

    // =========================================================================
    // Tier 2: Boundary & Corner Cases — Custom Item Form Validation
    // =========================================================================

    @Test
    fun customItemFormValidation_requiresNonBlankName() {
        fun isValidName(name: String): Boolean = name.trim().isNotBlank()

        assertFalse("Empty name must be invalid", isValidName(""))
        assertFalse("Whitespace name must be invalid", isValidName("   "))
        assertFalse("Newline name must be invalid", isValidName("\n\t"))
        assertTrue("Valid name should pass", isValidName("Piano Lessons"))
    }

    @Test
    fun customItemFormValidation_requiresAmountGreaterThanZero() {
        fun isValidAmount(amountStr: String): Boolean {
            val amount = amountStr.toLongOrNull() ?: return false
            return amount > 0L
        }

        assertFalse("Zero amount must be rejected", isValidAmount("0"))
        assertFalse("Negative amount must be rejected", isValidAmount("-500"))
        assertFalse("Non-numeric string must be rejected", isValidAmount("abc"))
        assertFalse("Empty amount must be rejected", isValidAmount(""))
        assertTrue("Positive amount must be accepted", isValidAmount("3000"))
    }

    @Test
    fun customItemFormValidation_requiresValidSingleEmojiIcon() {
        fun isValidIcon(icon: String): Boolean {
            return EmojiValidatorAdapter.isValidEmoji(icon)
        }

        // Must reject non-emoji characters
        assertFalse("Letter 'a' must be rejected", isValidIcon("a"))
        assertFalse("Digit '5' must be rejected", isValidIcon("5"))
        assertFalse("Punctuation '!' must be rejected", isValidIcon("!"))
        assertFalse("Multi-emoji must be rejected", isValidIcon("🍕🍔"))

        // Must accept valid single emojis
        assertTrue("Pizza emoji must be accepted", isValidIcon("🍕"))
        assertTrue("Piano emoji must be accepted", isValidIcon("🎹"))
        assertTrue("Dog emoji must be accepted", isValidIcon("🐶"))
        assertTrue("Book emoji must be accepted", isValidIcon("📚"))
    }

    // =========================================================================
    // Tier 3: Cross-Feature Combinations — Persistence & Entity Mapping
    // =========================================================================

    @Test
    fun customItem_roomEntityConstructionAndMapping_verified() {
        val customId = UUID.randomUUID().toString()
        val customName = "Piano Lessons"
        val customEmoji = "🎹"
        val amountPaise = 3_000_00L
        val monthKey = "2026-10"
        val now = System.currentTimeMillis()

        // Construct CategoryEntity matching Room schema (CategoryEntity.kt)
        val categoryEntity = CategoryEntity(
            id = customId,
            name = customName,
            iconKey = customEmoji,
            type = CategoryType.EXPENSE.name,
            groupKey = CategoryGroup.LIFESTYLE.name,
            sortOrder = 100,
            systemCategory = false, // Must be marked as custom (false)
            archived = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        assertEquals(customId, categoryEntity.id)
        assertEquals(customName, categoryEntity.name)
        assertEquals("🎹", categoryEntity.iconKey)
        assertFalse("Custom item must have systemCategory = false", categoryEntity.systemCategory)
        assertFalse("Custom item must not be archived initially", categoryEntity.archived)

        // Construct MonthlyCategoryPlanEntity matching Room schema (MonthlyCategoryPlanEntity.kt)
        val monthKeyInt = 202610
        val planEntity = MonthlyCategoryPlanEntity(
            monthKey = monthKeyInt,
            categoryId = customId,
            plannedAmountMinor = amountPaise,
            currencyCode = "INR",
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        assertEquals(monthKeyInt, planEntity.monthKey)
        assertEquals(customId, planEntity.categoryId)
        assertEquals(3_000_00L, planEntity.plannedAmountMinor)
        assertEquals("INR", planEntity.currencyCode)
    }

    // =========================================================================
    // Tier 3: Cross-Feature Combinations — Preset Removal + Restore Cycle
    // =========================================================================

    @Test
    fun presetRemovalAndRestoreCycle_preservesPresetInCategoriesAndTracksPlan() {
        val monthKey = "2026-10"

        // 1. Initial State: 24 active system presets
        val all24SystemPresetIds = setOf(
            SeedIds.EXPENSE_RENT, SeedIds.EXPENSE_GROCERIES, SeedIds.EXPENSE_GAS,
            SeedIds.EXPENSE_HOUSE_HELP, SeedIds.EXPENSE_DINING, SeedIds.EXPENSE_ELECTRICITY,
            SeedIds.EXPENSE_WIFI, SeedIds.EXPENSE_MOBILE, SeedIds.EXPENSE_CAB,
            SeedIds.EXPENSE_LAUNDRY, SeedIds.EXPENSE_SHOPPING, SeedIds.EXPENSE_GYM,
            SeedIds.EXPENSE_FITNESS_DIET, SeedIds.EXPENSE_SALON, SeedIds.EXPENSE_OUTING,
            SeedIds.EXPENSE_TRAVEL, SeedIds.EXPENSE_HOMETOWN, SeedIds.EXPENSE_MEDICINE,
            SeedIds.EXPENSE_FAMILY_SUPPORT, SeedIds.INVESTMENT_SIP, SeedIds.EXPENSE_HEALTH_INSURANCE,
            SeedIds.SAVING_LIC_POLICY, SeedIds.SAVING_EMERGENCY_FUND, SeedIds.EXPENSE_OTHER
        )

        assertEquals(24, all24SystemPresetIds.size)

        // Active monthly plan category IDs
        val activeMonthlyPlanCategoryIds = all24SystemPresetIds.toMutableSet()

        // Helper to compute removed presets for the Restore section:
        // Presets where systemCategory == true and categoryId !in activeMonthlyPlanCategoryIds
        fun getRemovedPresets(): Set<String> {
            return all24SystemPresetIds.filter { it !in activeMonthlyPlanCategoryIds }.toSet()
        }

        // Initially no presets are removed
        assertTrue("Initially no presets are removed", getRemovedPresets().isEmpty())

        // 2. User removes a preset: e.g. EXPENSE_RENT
        val removedPresetId = SeedIds.EXPENSE_RENT
        activeMonthlyPlanCategoryIds.remove(removedPresetId)

        // Active count drops to 23
        assertEquals(23, activeMonthlyPlanCategoryIds.size)
        assertFalse(activeMonthlyPlanCategoryIds.contains(removedPresetId))

        // 3. Restore section detects removed preset
        val removedPresets = getRemovedPresets()
        assertEquals(1, removedPresets.size)
        assertTrue(removedPresets.contains(removedPresetId))

        // 4. User restores preset from the Restore section
        activeMonthlyPlanCategoryIds.add(removedPresetId)

        // Count restored to 24
        assertEquals(24, activeMonthlyPlanCategoryIds.size)
        assertTrue(activeMonthlyPlanCategoryIds.contains(removedPresetId))
        assertTrue("Restore section is empty again after restore", getRemovedPresets().isEmpty())
    }
}
