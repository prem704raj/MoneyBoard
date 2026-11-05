package com.premraj.moneyboard.feature.dashboard.components

import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Empirical Adversarial Challenger Test Suite for [CategoryIconMapper].
 * Verifies all 24 category presets resolve to valid drawable resources,
 * confirms resource files exist on disk, and tests alias lookups and fallback behaviors.
 */
class CategoryIconMapperAdversarialChallengerTest {

    private val all24CategoryIds = listOf(
        SeedIds.EXPENSE_RENT,
        SeedIds.EXPENSE_GROCERIES,
        SeedIds.EXPENSE_GAS,
        SeedIds.EXPENSE_HOUSE_HELP,
        SeedIds.EXPENSE_DINING,
        SeedIds.EXPENSE_ELECTRICITY,
        SeedIds.EXPENSE_WIFI,
        SeedIds.EXPENSE_MOBILE,
        SeedIds.EXPENSE_CAB,
        SeedIds.EXPENSE_LAUNDRY,
        SeedIds.EXPENSE_SHOPPING,
        SeedIds.EXPENSE_GYM,
        SeedIds.EXPENSE_FITNESS_DIET,
        SeedIds.EXPENSE_SALON,
        SeedIds.EXPENSE_OUTING,
        SeedIds.EXPENSE_TRAVEL,
        SeedIds.EXPENSE_HOMETOWN,
        SeedIds.EXPENSE_MEDICINE,
        SeedIds.EXPENSE_FAMILY_SUPPORT,
        SeedIds.INVESTMENT_SIP,
        SeedIds.EXPENSE_HEALTH_INSURANCE,
        SeedIds.SAVING_LIC_POLICY,
        SeedIds.SAVING_EMERGENCY_FUND,
        SeedIds.EXPENSE_OTHER
    )

    private val expectedDrawableResIds = listOf(
        R.drawable.ic_rent,
        R.drawable.ic_groceries,
        R.drawable.ic_gas,
        R.drawable.ic_maid,
        R.drawable.ic_online_food,
        R.drawable.ic_electricity,
        R.drawable.ic_wifi,
        R.drawable.ic_mobile_recharge,
        R.drawable.ic_cab_rapido,
        R.drawable.ic_laundry,
        R.drawable.ic_shopping,
        R.drawable.ic_gym,
        R.drawable.ic_gym_diet,
        R.drawable.ic_skincare_saloon,
        R.drawable.ic_weekend_outing,
        R.drawable.ic_domestic_trip,
        R.drawable.ic_hometown_visit,
        R.drawable.ic_medicine,
        R.drawable.ic_sending_to_parents,
        R.drawable.ic_sip,
        R.drawable.ic_health_insurance,
        R.drawable.ic_lic,
        R.drawable.ic_emergency_fund,
        R.drawable.ic_other_expenses
    )

    private val expectedFileNames = listOf(
        "ic_rent.png",
        "ic_groceries.png",
        "ic_gas.png",
        "ic_maid.png",
        "ic_online_food.png",
        "ic_electricity.png",
        "ic_wifi.png",
        "ic_mobile_recharge.png",
        "ic_cab_rapido.png",
        "ic_laundry.png",
        "ic_shopping.png",
        "ic_gym.png",
        "ic_gym_diet.png",
        "ic_skincare_saloon.png",
        "ic_weekend_outing.png",
        "ic_domestic_trip.png",
        "ic_hometown_visit.png",
        "ic_medicine.png",
        "ic_sending_to_parents.png",
        "ic_sip.png",
        "ic_health_insurance.png",
        "ic_lic.png",
        "ic_emergency_fund.png",
        "ic_other_expenses.png"
    )

    @Test
    fun presetDefinitions_exactly24DistinctEntries() {
        assertEquals("Must contain exactly 24 preset definitions", 24, CategoryIconMapper.PRESET_DEFINITIONS.size)

        val uniqueIds = CategoryIconMapper.PRESET_DEFINITIONS.map { it.categoryId }.toSet()
        assertEquals("All 24 category IDs must be distinct", 24, uniqueIds.size)

        val uniqueDrawables = CategoryIconMapper.PRESET_DEFINITIONS.map { it.drawableRes }.toSet()
        assertEquals("All 24 drawable resource IDs must be distinct", 24, uniqueDrawables.size)
    }

    @Test
    fun all24Categories_resolveToValidDrawableRes() {
        for (categoryId in all24CategoryIds) {
            val drawableRes = CategoryIconMapper.getPresetDrawable(categoryId)
            assertNotNull("Category '$categoryId' must resolve to a non-null drawable resource", drawableRes)
            assertTrue("Drawable resource ID must be a positive integer", drawableRes!! > 0)
        }
    }

    @Test
    fun all24Categories_haveCorrespondingPhysicalFilesWithPngHeader() {
        val drawableDir = File("src/main/res/drawable").let {
            if (it.exists()) it else File("app/src/main/res/drawable")
        }
        assertTrue("Drawable directory must exist: ${drawableDir.absolutePath}", drawableDir.exists())

        // Standard PNG Magic Bytes: 0x89 0x50 0x4E 0x47 0x0D 0x0A 0x1A 0x0A
        val pngMagic = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte())

        for (fileName in expectedFileNames) {
            val file = File(drawableDir, fileName)
            assertTrue("Drawable file '$fileName' must exist at ${file.absolutePath}", file.exists())
            assertTrue("Drawable file '$fileName' must be non-empty", file.length() > 100)

            val bytes = file.readBytes()
            assertEquals("File '$fileName' must have PNG magic header [0]", pngMagic[0], bytes[0])
            assertEquals("File '$fileName' must have PNG magic header [1]", pngMagic[1], bytes[1])
            assertEquals("File '$fileName' must have PNG magic header [2]", pngMagic[2], bytes[2])
            assertEquals("File '$fileName' must have PNG magic header [3]", pngMagic[3], bytes[3])
        }
    }

    @Test
    fun iconKeysAndAliases_allResolveCorrectly() {
        val aliasTests = mapOf(
            "rent" to R.drawable.ic_rent,
            "home" to R.drawable.ic_rent,
            "groceries" to R.drawable.ic_groceries,
            "gas" to R.drawable.ic_gas,
            "fuel" to R.drawable.ic_gas,
            "maid" to R.drawable.ic_maid,
            "house_help" to R.drawable.ic_maid,
            "online_food" to R.drawable.ic_online_food,
            "food" to R.drawable.ic_online_food,
            "dining" to R.drawable.ic_online_food,
            "electricity" to R.drawable.ic_electricity,
            "wifi" to R.drawable.ic_wifi,
            "mobile_recharge" to R.drawable.ic_mobile_recharge,
            "mobile" to R.drawable.ic_mobile_recharge,
            "phone" to R.drawable.ic_mobile_recharge,
            "cab_rapido" to R.drawable.ic_cab_rapido,
            "cab" to R.drawable.ic_cab_rapido,
            "rapido" to R.drawable.ic_cab_rapido,
            "laundry" to R.drawable.ic_laundry,
            "shopping" to R.drawable.ic_shopping,
            "gym" to R.drawable.ic_gym,
            "fitness" to R.drawable.ic_gym,
            "gym_diet" to R.drawable.ic_gym_diet,
            "fitness_diet" to R.drawable.ic_gym_diet,
            "skincare_saloon" to R.drawable.ic_skincare_saloon,
            "salon" to R.drawable.ic_skincare_saloon,
            "saloon" to R.drawable.ic_skincare_saloon,
            "skincare" to R.drawable.ic_skincare_saloon,
            "weekend_outing" to R.drawable.ic_weekend_outing,
            "outing" to R.drawable.ic_weekend_outing,
            "domestic_trip" to R.drawable.ic_domestic_trip,
            "travel" to R.drawable.ic_domestic_trip,
            "trip" to R.drawable.ic_domestic_trip,
            "hometown_visit" to R.drawable.ic_hometown_visit,
            "hometown" to R.drawable.ic_hometown_visit,
            "home_travel" to R.drawable.ic_hometown_visit,
            "medicine" to R.drawable.ic_medicine,
            "medical" to R.drawable.ic_medicine,
            "pharmacy" to R.drawable.ic_medicine,
            "sending_to_parents" to R.drawable.ic_sending_to_parents,
            "family" to R.drawable.ic_sending_to_parents,
            "parents" to R.drawable.ic_sending_to_parents,
            "family_support" to R.drawable.ic_sending_to_parents,
            "sip" to R.drawable.ic_sip,
            "investment" to R.drawable.ic_sip,
            "health_insurance" to R.drawable.ic_health_insurance,
            "insurance" to R.drawable.ic_health_insurance,
            "lic" to R.drawable.ic_lic,
            "lic_policy" to R.drawable.ic_lic,
            "emergency_fund" to R.drawable.ic_emergency_fund,
            "emergency" to R.drawable.ic_emergency_fund,
            "savings" to R.drawable.ic_emergency_fund,
            "other_expenses" to R.drawable.ic_other_expenses,
            "other" to R.drawable.ic_other_expenses
        )

        for ((key, expectedRes) in aliasTests) {
            val resolved = CategoryIconMapper.getPresetDrawable("", key)
            assertEquals("Key '$key' must resolve to expected drawable", expectedRes, resolved)

            val resolvedViaTopLevel = categoryDrawableRes(key)
            assertEquals("categoryDrawableRes('$key') must match", expectedRes, resolvedViaTopLevel)
        }
    }

    @Test
    fun resolveExpenseIcon_systemVersusCustomAdversarialMatrix() {
        // 1. System preset category always resolves to drawableRes
        val (res1, emoji1) = CategoryIconMapper.resolveExpenseIcon(
            categoryId = SeedIds.EXPENSE_RENT,
            iconKey = "home",
            isSystemCategory = true
        )
        assertEquals(R.drawable.ic_rent, res1)
        assertNull(emoji1)

        // 2. Custom category with valid emoji resolves to emoji string
        val (res2, emoji2) = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_1",
            iconKey = "🍕",
            isSystemCategory = false
        )
        assertNull(res2)
        assertEquals("🍕", emoji2)

        // 3. Custom category with invalid emoji text but matching a preset alias falls back to drawable
        val (res3, emoji3) = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_rent",
            iconKey = "rent",
            isSystemCategory = false
        )
        assertEquals(R.drawable.ic_rent, res3)
        assertNull(emoji3)

        // 4. Custom category with completely invalid text returns null pair
        val (res4, emoji4) = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_unknown",
            iconKey = "invalid_random_string",
            isSystemCategory = false
        )
        assertNull(res4)
        assertNull(emoji4)
    }
}
