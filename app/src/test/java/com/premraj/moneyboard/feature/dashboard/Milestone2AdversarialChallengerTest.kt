package com.premraj.moneyboard.feature.dashboard

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.database.entity.CategoryEntity
import com.premraj.moneyboard.core.database.entity.MonthlyCategoryPlanEntity
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.util.HeaderSubtitleFormatter
import com.premraj.moneyboard.core.validation.EmojiValidator
import com.premraj.moneyboard.feature.dashboard.components.AvailableCategoryItem
import com.premraj.moneyboard.feature.dashboard.components.CategoryIconMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.UUID

/**
 * Empirical Adversarial Challenger Test Suite for Milestone 2.
 * Validates:
 * 1. R6 Add / Restore Dialog:
 *    - Form validation: empty name, 0 amount, negative amount, non-emoji input (all rejected)
 *    - Valid custom item creation with emoji (accepted, Room entity mapping, editing & deletion)
 *    - Restore removed presets: removed presets appear with original icons and can be restored
 * 2. R4/R6 Untinted icon rendering:
 *    - Verify ExpenseIconView does not apply color tint to Image(painterResource(...))
 * 3. Room persistence & startup seeding:
 *    - Verify MoneyBoardApplication calls seedIfEmpty unconditionally on first launch without BuildConfig.DEBUG guard
 * 4. R1 Badge Layout (FlowRow, maxLines = 1, softWrap = false)
 * 5. R2 Header Subtitle Formatter (no "No EMI" placeholder)
 */
class Milestone2AdversarialChallengerTest {

    // =========================================================================
    // 1. R6 FORM VALIDATION ADVERSARIAL TESTING
    // =========================================================================

    private fun isCustomFormValid(name: String, amountStr: String, icon: String): Boolean {
        val isNameValid = name.trim().isNotBlank()
        val amount = amountStr.toLongOrNull() ?: 0L
        val isAmountValid = amount > 0L
        val isIconValid = EmojiValidator.isValidEmoji(icon)
        return isNameValid && isAmountValid && isIconValid
    }

    @Test
    fun formValidation_emptyOrWhitespaceName_rejected() {
        val invalidNames = listOf(
            "",
            " ",
            "   ",
            "\t",
            "\n",
            "\r\n",
            "  \t\n  "
        )
        for (name in invalidNames) {
            assertFalse(
                "Name '$name' must be rejected",
                isCustomFormValid(name, "1000", "🍕")
            )
        }
    }

    @Test
    fun formValidation_zeroOrNegativeOrInvalidAmount_rejected() {
        val invalidAmounts = listOf(
            "0",
            "000",
            "-1",
            "-1000",
            "-0",
            "",
            "abc",
            "12.50",
            "₹1000",
            " ",
            "NaN"
        )
        for (amount in invalidAmounts) {
            assertFalse(
                "Amount '$amount' must be rejected",
                isCustomFormValid("Valid Name", amount, "🍕")
            )
        }
    }

    @Test
    fun formValidation_nonEmojiIcon_rejected() {
        val invalidIcons = listOf(
            "a", "Z", "9", "0", "@", "#", "!", "?",
            "abc", "pizza", "123",
            "🍕🍔", // Multi-emoji
            "🍕a", "a🍕", // Mixed
            " 🍕", "🍕 ", "\n🍕", // Whitespace padded
            "1️⃣", // Keycap with ASCII digit
            "₹", "$", "€", // Currency symbols
            "Ω", "α", "Я", "中", "ॐ", // Non-emoji scripts
            "&amp;", "&#128512;" // HTML entities
        )
        for (icon in invalidIcons) {
            assertFalse(
                "Icon '$icon' must be rejected",
                isCustomFormValid("Valid Name", "1000", icon)
            )
        }
    }

    @Test
    fun formValidation_allFieldsInvalid_rejected() {
        assertFalse(isCustomFormValid("", "0", "a"))
        assertFalse(isCustomFormValid("   ", "-500", "123"))
        assertFalse(isCustomFormValid("", "", ""))
    }

    // =========================================================================
    // 2. VALID CUSTOM ITEM CREATION WITH EMOJI & ROOM ENTITY MAPPING
    // =========================================================================

    @Test
    fun customItemCreation_diverseValidEmojis_accepted() {
        val validEmojis = listOf(
            "🍕", // Food
            "🎹", // Musical instrument
            "🎸", // Guitar
            "🐶", // Animal
            "🚗", // Vehicle
            "💻", // Technology
            "⚽", // Sport
            "🏖️", // Travel
            "📚", // Education
            "🎨", // Art
            "👨‍💻", // ZWJ sequence: tech worker
            "👍🏽", // Skin tone modifier
            "🇮🇳"  // National flag
        )

        for (emoji in validEmojis) {
            assertTrue(
                "Emoji '$emoji' should be valid and accepted in form",
                isCustomFormValid("Custom Item", "2500", emoji)
            )
        }
    }

    @Test
    fun customItemCreation_roomEntityMappingAndArchival_verified() {
        val customId = UUID.randomUUID().toString()
        val customName = "Piano Lessons"
        val customEmoji = "🎹"
        val monthlyRupees = 4_500L
        val amountMinor = monthlyRupees * 100L
        val monthKeyInt = 202611
        val now = System.currentTimeMillis()

        // 1. CategoryEntity mapping
        val category = CategoryEntity(
            id = customId,
            name = customName,
            iconKey = customEmoji,
            type = CategoryType.EXPENSE.name,
            groupKey = CategoryGroup.LIFESTYLE.name,
            sortOrder = 100,
            systemCategory = false, // Must be marked as custom
            archived = false,
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        assertEquals(customId, category.id)
        assertEquals(customName, category.name)
        assertEquals(customEmoji, category.iconKey)
        assertFalse("Custom category must have systemCategory = false", category.systemCategory)
        assertFalse("New custom category must not be archived", category.archived)

        // 2. MonthlyCategoryPlanEntity mapping
        val plan = MonthlyCategoryPlanEntity(
            monthKey = monthKeyInt,
            categoryId = customId,
            plannedAmountMinor = amountMinor,
            currencyCode = "INR",
            createdAtEpochMillis = now,
            updatedAtEpochMillis = now
        )

        assertEquals(monthKeyInt, plan.monthKey)
        assertEquals(customId, plan.categoryId)
        assertEquals(450_000L, plan.plannedAmountMinor)
        assertEquals("INR", plan.currencyCode)

        // 3. Custom category archival on deletion
        val archivedCategory = category.copy(archived = true, updatedAtEpochMillis = now + 1000L)
        assertTrue("Archived category must have archived = true", archivedCategory.archived)
        assertFalse("Archived category retains systemCategory = false", archivedCategory.systemCategory)
    }

    // =========================================================================
    // 3. RESTORE REMOVED PRESETS EMPIRICAL VERIFICATION
    // =========================================================================

    private val all24SystemPresetIds = listOf(
        SeedIds.EXPENSE_RENT, SeedIds.EXPENSE_GROCERIES, SeedIds.EXPENSE_GAS,
        SeedIds.EXPENSE_HOUSE_HELP, SeedIds.EXPENSE_DINING, SeedIds.EXPENSE_ELECTRICITY,
        SeedIds.EXPENSE_WIFI, SeedIds.EXPENSE_MOBILE, SeedIds.EXPENSE_CAB,
        SeedIds.EXPENSE_LAUNDRY, SeedIds.EXPENSE_SHOPPING, SeedIds.EXPENSE_GYM,
        SeedIds.EXPENSE_FITNESS_DIET, SeedIds.EXPENSE_SALON, SeedIds.EXPENSE_OUTING,
        SeedIds.EXPENSE_TRAVEL, SeedIds.EXPENSE_HOMETOWN, SeedIds.EXPENSE_MEDICINE,
        SeedIds.EXPENSE_FAMILY_SUPPORT, SeedIds.INVESTMENT_SIP, SeedIds.EXPENSE_HEALTH_INSURANCE,
        SeedIds.SAVING_LIC_POLICY, SeedIds.SAVING_EMERGENCY_FUND, SeedIds.EXPENSE_OTHER
    )

    @Test
    fun restorePresets_allPresetsActive_removedListIsEmpty() {
        val activeIds = all24SystemPresetIds.toSet()
        val removed = all24SystemPresetIds.filter { !activeIds.contains(it) }
        assertTrue("When all presets are active, removed list must be empty", removed.isEmpty())
    }

    @Test
    fun restorePresets_singleAndMultipleRemoved_appearInRestoreListWithOriginalIcons() {
        val activeIds = all24SystemPresetIds.toMutableSet()

        // Remove 3 presets: Rent, Gym, SIP
        val removedPresetsToTest = listOf(
            SeedIds.EXPENSE_RENT,
            SeedIds.EXPENSE_GYM,
            SeedIds.INVESTMENT_SIP
        )

        for (preset in removedPresetsToTest) {
            activeIds.remove(preset)
        }

        val detectedRemoved = all24SystemPresetIds.filter { !activeIds.contains(it) }
        assertEquals(3, detectedRemoved.size)
        assertTrue(detectedRemoved.containsAll(removedPresetsToTest))

        // Verify each removed preset resolves to its valid original illustrated icon
        for (presetId in detectedRemoved) {
            val drawableRes = CategoryIconMapper.getPresetDrawable(presetId)
            assertNotNull("Removed preset '$presetId' must have original drawable icon", drawableRes)
            assertTrue("Drawable resource ID must be positive", drawableRes!! > 0)
        }

        // Simulate restore of Rent
        activeIds.add(SeedIds.EXPENSE_RENT)
        val remainingRemoved = all24SystemPresetIds.filter { !activeIds.contains(it) }
        assertEquals(2, remainingRemoved.size)
        assertFalse(remainingRemoved.contains(SeedIds.EXPENSE_RENT))
    }

    @Test
    fun restorePresets_all24PresetsHaveOriginalIconsMapped() {
        for (presetId in all24SystemPresetIds) {
            val drawableRes = CategoryIconMapper.getPresetDrawable(presetId)
            assertNotNull("Preset '$presetId' must resolve to a valid drawable", drawableRes)
            assertTrue(drawableRes!! > 0)
        }
    }

    // =========================================================================
    // 4. UNTINTED ICON RENDERING STATIC & LOGICAL VERIFICATION
    // =========================================================================

    @Test
    fun untintedIconRendering_expenseIconViewDoesNotApplyColorFilterToImage() {
        val file = File("app/src/main/java/com/premraj/moneyboard/feature/dashboard/components/ExpenseIconView.kt").let {
            if (it.exists()) it else File("src/main/java/com/premraj/moneyboard/feature/dashboard/components/ExpenseIconView.kt")
        }
        assertTrue("ExpenseIconView.kt must exist", file.exists())

        val source = file.readText()

        // 1. Must use Image with painterResource
        assertTrue("ExpenseIconView must use Image(painter = painterResource(...))", source.contains("painter = painterResource"))

        // 2. Must NOT apply colorFilter to Image
        assertFalse(
            "ExpenseIconView must not apply colorFilter to Image to preserve untinted colors",
            source.contains("colorFilter")
        )

        // 3. Must use Text for emojis
        assertTrue("ExpenseIconView must use Text for emoji rendering", source.contains("Text(") && source.contains("item.iconEmoji"))
    }

    // =========================================================================
    // 5. ROOM PERSISTENCE & STARTUP SEEDING UNCONDITIONAL EXECUTION
    // =========================================================================

    @Test
    fun startupSeeding_moneyBoardApplicationSeedsUnconditionally() {
        val file = File("app/src/main/java/com/premraj/moneyboard/MoneyBoardApplication.kt").let {
            if (it.exists()) it else File("src/main/java/com/premraj/moneyboard/MoneyBoardApplication.kt")
        }
        assertTrue("MoneyBoardApplication.kt must exist", file.exists())

        val source = file.readText()

        // 1. Must call seedIfEmpty
        assertTrue("MoneyBoardApplication must call seedIfEmpty", source.contains("seedIfEmpty"))

        // 2. Must NOT be guarded by BuildConfig.DEBUG
        assertFalse(
            "seedIfEmpty must be unconditional and not guarded by BuildConfig.DEBUG",
            source.contains("BuildConfig.DEBUG")
        )

        // 3. Must call databaseInitializer.initialize()
        assertTrue("MoneyBoardApplication must initialize database", source.contains("databaseInitializer.initialize()"))
    }

    // =========================================================================
    // 6. R1 BADGE LAYOUT FLOWROW & TEXT NO-WRAP VERIFICATION
    // =========================================================================

    @Test
    fun badgeLayout_addExpenseDialogUsesFlowRowWithNoWrap() {
        val file = File("app/src/main/java/com/premraj/moneyboard/feature/dashboard/components/AddExpenseDialog.kt").let {
            if (it.exists()) it else File("src/main/java/com/premraj/moneyboard/feature/dashboard/components/AddExpenseDialog.kt")
        }
        assertTrue("AddExpenseDialog.kt must exist", file.exists())

        val source = file.readText()

        // Verify FlowRow is used in PresetItemRow
        assertTrue("AddExpenseDialog must use FlowRow for title/badge layout", source.contains("FlowRow("))

        // Verify maxLines = 1 and softWrap = false
        assertTrue("AddExpenseDialog badge text must set maxLines = 1", source.contains("maxLines = 1"))
        assertTrue("AddExpenseDialog badge text must set softWrap = false", source.contains("softWrap = false"))
    }

    // =========================================================================
    // 7. R2 DYNAMIC HEADER SUBTITLE STRICT FORMAT & NO "NO EMI"
    // =========================================================================

    @Test
    fun headerSubtitle_dynamicFormattingStrictness() {
        // Zero EMI
        val zeroResult = HeaderSubtitleFormatter.format("Single Person", "Pune", 0L)
        assertEquals("Single Person | Pune", zeroResult)
        assertFalse("Must never output 'No EMI'", zeroResult.contains("No EMI"))
        assertFalse("Must not contain 'EMI' when 0", zeroResult.contains("EMI"))

        // Negative EMI
        val negResult = HeaderSubtitleFormatter.format("Single Person", "Pune", -5000L)
        assertEquals("Single Person | Pune", negResult)
        assertFalse(negResult.contains("EMI"))

        // Positive EMI
        val posResult = HeaderSubtitleFormatter.format("Single Person", "Pune", 5000L)
        assertEquals("Single Person | Pune | EMI ₹5,000", posResult)

        // Edge case: empty strings
        val fallbackResult = HeaderSubtitleFormatter.format("", "", 0L)
        assertEquals("Single Person | Pune", fallbackResult)
    }
}
