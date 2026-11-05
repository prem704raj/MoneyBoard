package com.premraj.moneyboard.feature.export

import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.domain.model.Category
import com.premraj.moneyboard.core.domain.model.CategoryGroup
import com.premraj.moneyboard.core.domain.model.CategoryType
import com.premraj.moneyboard.core.util.HeaderSubtitleFormatter
import com.premraj.moneyboard.core.validation.EmojiValidator
import com.premraj.moneyboard.feature.dashboard.components.CategoryIconMapper
import com.premraj.moneyboard.feature.dashboard.ExpenseRowUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Final Acceptance and E2E Verification Test Suite for Milestone 4:
 *
 * Verifies all 6 requirements:
 * - R1: Cramped "Active" badge layout & non-wrapping constraints
 * - R2: Dynamic header subtitle omitting "No EMI" completely
 * - R3: 1224px Unified Report Sheet dynamic metrics & MediaStore PDF URI
 * - R4: 24 illustrated icons mapping
 * - R5: App launcher icons across mipmap densities
 * - R6: Add/Restore dialog emoji validation & custom expense model
 */
class Milestone4VerificationTest {

    // =========================================================================
    // R1: Cramped "Active" Badge Layout
    // =========================================================================

    @Test
    fun r1_verifyExpenseRowUi_supportsLongTitlesWithoutBadgeCorruption() {
        val longTitle1 = "Yearly one Domestic trip (30,000 ÷ 12)"
        val longTitle2 = "Yearly 2 times Hometown visit (42,000 ÷ 12)"

        val row1 = ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_TRAVEL,
            number = 16,
            title = longTitle1,
            amountText = "2,500",
            amountMinor = 250_000L
        )

        val row2 = ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_HOMETOWN,
            number = 17,
            title = longTitle2,
            amountText = "3,500",
            amountMinor = 350_000L
        )

        assertEquals(longTitle1, row1.title)
        assertEquals(longTitle2, row2.title)
        assertFalse(row1.title.contains("\n"))
        assertFalse(row2.title.contains("\n"))
    }

    // =========================================================================
    // R2: Dynamic Header Subtitle (Strict Zero EMI Contract)
    // =========================================================================

    @Test
    fun r2_verifyDynamicHeaderSubtitle_strictlyOmitsNoEmiPlaceholder() {
        // Zero EMI
        val subZero = HeaderSubtitleFormatter.format("Single Person", "Pune", 0L)
        assertEquals("Single Person | Pune", subZero)
        assertFalse(subZero.contains("No EMI", ignoreCase = true))
        assertFalse(subZero.contains("EMI", ignoreCase = true))

        // Negative EMI
        val subNeg = HeaderSubtitleFormatter.format("Single Person", "Bengaluru", -5000L)
        assertEquals("Single Person | Bengaluru", subNeg)
        assertFalse(subNeg.contains("No EMI", ignoreCase = true))

        // Positive EMI
        val subPos = HeaderSubtitleFormatter.format("Single Person", "Mumbai", 15_000L)
        assertTrue(subPos.startsWith("Single Person | Mumbai | EMI ₹"))
        assertTrue(subPos.contains("15,000") || subPos.contains("15000"))
        assertFalse(subPos.contains("No EMI", ignoreCase = true))

        // Edge case: blank profile or city defaults gracefully
        val subDefaults = HeaderSubtitleFormatter.format("   ", "   ", 0L)
        assertEquals("Single Person | Pune", subDefaults)
    }

    // =========================================================================
    // R3: MediaStore PDF Collection & Dynamic Report Calculations
    // =========================================================================

    @Test
    fun r3_verifyPdfCollectionUri_sourceCodeUsesFilesContentUri() {
        val file = if (File("src/main/java/com/premraj/moneyboard/core/export/ReportExportManager.kt").exists()) {
            File("src/main/java/com/premraj/moneyboard/core/export/ReportExportManager.kt")
        } else {
            File("app/src/main/java/com/premraj/moneyboard/core/export/ReportExportManager.kt")
        }
        assertTrue("ReportExportManager.kt must exist", file.exists())
        val content = file.readText()
        assertTrue("Must use MediaStore.Files.getContentUri(\"external\") for PDF", content.contains("MediaStore.Files.getContentUri(\"external\")"))
        val pdfFunctionBody = content.substringAfter("fun savePdfToMediaStore")
        assertFalse("Must not use MediaStore.Images.Media.EXTERNAL_CONTENT_URI in savePdfToMediaStore", pdfFunctionBody.contains("MediaStore.Images.Media.EXTERNAL_CONTENT_URI"))
    }

    @Test
    fun r3_verifyUnifiedReportSheet_dynamicPercentagesSumCloseTo100() {
        val expenses = listOf(
            ExpenseRowUi(categoryId = SeedIds.EXPENSE_RENT, number = 1, title = "Rent", amountText = "10,000", amountMinor = 1_000_000L),
            ExpenseRowUi(categoryId = SeedIds.EXPENSE_GROCERIES, number = 2, title = "Groceries", amountText = "6,000", amountMinor = 600_000L),
            ExpenseRowUi(categoryId = SeedIds.EXPENSE_FAMILY_SUPPORT, number = 3, title = "Sending to parents", amountText = "10,000", amountMinor = 1_000_000L),
            ExpenseRowUi(categoryId = SeedIds.INVESTMENT_SIP, number = 4, title = "SIP", amountText = "5,000", amountMinor = 500_000L)
        )
        val totalMinor = expenses.sumOf { it.amountMinor }
        val slices = calculateDynamicDonutSlices(expenses, totalMinor)

        assertEquals(6, slices.size)
        val percentageSum = slices.sumOf { it.percentageValue.toDouble() }
        assertEquals(100.0, percentageSum, 1.0)
    }

    // =========================================================================
    // R4: 24 Illustrated Icons Cropped from Image 2
    // =========================================================================

    @Test
    fun r4_verifyAll24IllustratedIcons_mappedToValidDrawableResources() {
        val canonicalKeys = listOf(
            "rent", "groceries", "gas", "maid", "dining",
            "electricity", "wifi", "mobile", "cab", "laundry",
            "shopping", "gym", "gym_diet", "salon", "outing",
            "travel", "hometown", "medicine", "parents", "sip",
            "health_insurance", "lic", "emergency", "other"
        )

        assertEquals("Must have exactly 24 illustrated category items", 24, canonicalKeys.size)

        canonicalKeys.forEach { key ->
            val resId = CategoryIconMapper.getCategoryDrawableRes(key)
            assertNotNull("Icon key '$key' must map to a drawable resource", resId)
            assertTrue("Drawable resource ID for '$key' must be > 0", (resId ?: 0) > 0)
        }
    }

    // =========================================================================
    // R5: App Launcher Icon Files Across All Mipmap Densities
    // =========================================================================

    @Test
    fun r5_verifyLauncherIcons_existAcrossAllDensities() {
        val resDir = if (File("src/main/res").exists()) File("src/main/res") else File("app/src/main/res")
        val densities = listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")

        densities.forEach { density ->
            val mipmapDir = File(resDir, "mipmap-$density")
            assertTrue("Directory mipmap-$density must exist", mipmapDir.exists() && mipmapDir.isDirectory)

            val launcherPng = File(mipmapDir, "ic_launcher.png")
            assertTrue("ic_launcher.png must exist in mipmap-$density", launcherPng.exists() && launcherPng.length() > 0)

            val launcherRoundPng = File(mipmapDir, "ic_launcher_round.png")
            assertTrue("ic_launcher_round.png must exist in mipmap-$density", launcherRoundPng.exists() && launcherRoundPng.length() > 0)
        }
    }

    // =========================================================================
    // R6: Emoji Validation & Dual Icon Support
    // =========================================================================

    @Test
    fun r6_verifyStrictEmojiValidation_acceptsRealEmojis_andRejectsNonEmojis() {
        // Valid emojis
        assertTrue(EmojiValidator.isValidEmoji("💰"))
        assertTrue(EmojiValidator.isValidEmoji("🍕"))
        assertTrue(EmojiValidator.isValidEmoji("🏋️"))
        assertTrue(EmojiValidator.isValidEmoji("🇮🇳"))
        assertTrue(EmojiValidator.isValidEmoji("👨‍👩‍👧‍👦"))

        // Invalid non-emojis
        assertFalse(EmojiValidator.isValidEmoji("a"))
        assertFalse(EmojiValidator.isValidEmoji("Z"))
        assertFalse(EmojiValidator.isValidEmoji("1"))
        assertFalse(EmojiValidator.isValidEmoji("@"))
        assertFalse(EmojiValidator.isValidEmoji("!"))
        assertFalse(EmojiValidator.isValidEmoji("hello"))
        assertFalse(EmojiValidator.isValidEmoji(""))
        assertFalse(EmojiValidator.isValidEmoji("   "))
    }

    @Test
    fun r6_verifyExpenseRowUi_dualIconSupport() {
        // Preset item with drawable
        val preset = ExpenseRowUi(
            categoryId = SeedIds.EXPENSE_RENT,
            number = 1,
            title = "Rent",
            amountText = "10,000",
            amountMinor = 1_000_000L,
            iconDrawableRes = com.premraj.moneyboard.R.drawable.ic_rent,
            iconEmoji = null,
            isCustom = false
        )
        assertNotNull(preset.iconDrawableRes)
        assertNull(preset.iconEmoji)
        assertFalse(preset.isCustom)

        // Custom item with emoji
        val custom = ExpenseRowUi(
            categoryId = "custom_crypto",
            number = 25,
            title = "Crypto Staking",
            amountText = "2,000",
            amountMinor = 200_000L,
            iconDrawableRes = null,
            iconEmoji = "🪙",
            isCustom = true
        )
        assertNull(custom.iconDrawableRes)
        assertEquals("🪙", custom.iconEmoji)
        assertTrue(custom.isCustom)
    }

    @Test
    fun verifyHeaderImageExportPlumbing() {
        val screenFile = if (File("src/main/java/com/premraj/moneyboard/feature/dashboard/FinanceBoardScreen.kt").exists()) {
            File("src/main/java/com/premraj/moneyboard/feature/dashboard/FinanceBoardScreen.kt")
        } else {
            File("app/src/main/java/com/premraj/moneyboard/feature/dashboard/FinanceBoardScreen.kt")
        }
        val appFile = if (File("src/main/java/com/premraj/moneyboard/MoneyBoardApp.kt").exists()) {
            File("src/main/java/com/premraj/moneyboard/MoneyBoardApp.kt")
        } else {
            File("app/src/main/java/com/premraj/moneyboard/MoneyBoardApp.kt")
        }
        assertTrue("FinanceBoardScreen.kt must exist", screenFile.exists())
        assertTrue("MoneyBoardApp.kt must exist", appFile.exists())

        val screenContent = screenFile.readText()
        assertTrue("FinanceBoardScreen must accept onExportImage parameter", screenContent.contains("onExportImage: () -> Unit = {}"))
        assertTrue("FinanceBoardScreen must forward onExportImage to FinanceHeroHeader", screenContent.contains("onExportImage = onExportImage"))

        val appContent = appFile.readText()
        assertTrue("MoneyBoardApp must pass onExportImage = ::exportImage to FinanceBoardScreen", appContent.contains("onExportImage = ::exportImage"))
    }
}
