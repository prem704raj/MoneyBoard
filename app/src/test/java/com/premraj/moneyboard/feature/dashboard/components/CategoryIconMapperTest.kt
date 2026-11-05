package com.premraj.moneyboard.feature.dashboard.components

import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryIconMapperTest {

    @Test
    fun presetDefinitions_containsAll24IllustratedIcons() {
        assertEquals("Must define exactly 24 preset categories", 24, CategoryIconMapper.PRESET_DEFINITIONS.size)

        val expectedDrawables = listOf(
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

        val actualDrawables = CategoryIconMapper.PRESET_DEFINITIONS.map { it.drawableRes }
        assertEquals(expectedDrawables.toSet(), actualDrawables.toSet())
    }

    @Test
    fun getPresetDrawable_resolvesByCategoryIdAndKey() {
        // Test key presets by categoryId
        assertEquals(R.drawable.ic_rent, CategoryIconMapper.getPresetDrawable(SeedIds.EXPENSE_RENT))
        assertEquals(R.drawable.ic_groceries, CategoryIconMapper.getPresetDrawable(SeedIds.EXPENSE_GROCERIES))
        assertEquals(R.drawable.ic_lic, CategoryIconMapper.getPresetDrawable(SeedIds.SAVING_LIC_POLICY))
        assertEquals(R.drawable.ic_sip, CategoryIconMapper.getPresetDrawable(SeedIds.INVESTMENT_SIP))
        assertEquals(R.drawable.ic_emergency_fund, CategoryIconMapper.getPresetDrawable(SeedIds.SAVING_EMERGENCY_FUND))

        // Test key presets by iconKey
        assertEquals(R.drawable.ic_rent, CategoryIconMapper.getPresetDrawable("", "rent"))
        assertEquals(R.drawable.ic_rent, CategoryIconMapper.getPresetDrawable("", "home"))
        assertEquals(R.drawable.ic_groceries, CategoryIconMapper.getPresetDrawable("", "groceries"))
        assertEquals(R.drawable.ic_gas, CategoryIconMapper.getPresetDrawable("", "gas"))
        assertEquals(R.drawable.ic_gas, CategoryIconMapper.getPresetDrawable("", "fuel"))
        assertEquals(R.drawable.ic_medicine, CategoryIconMapper.getPresetDrawable("", "medicine"))
        assertEquals(R.drawable.ic_sending_to_parents, CategoryIconMapper.getPresetDrawable("", "family"))
        assertEquals(R.drawable.ic_other_expenses, CategoryIconMapper.getPresetDrawable("", "other"))
    }

    @Test
    fun getCategoryDrawableRes_topLevelMapping() {
        assertEquals(R.drawable.ic_gym, categoryDrawableRes("gym"))
        assertEquals(R.drawable.ic_wifi, categoryDrawableRes("wifi"))
        assertEquals(R.drawable.ic_shopping, categoryDrawableRes("shopping"))
        assertEquals(R.drawable.ic_laundry, categoryDrawableRes("laundry"))
        assertNull(categoryDrawableRes("unknown_nonexistent_key"))
    }

    @Test
    fun resolveExpenseIcon_presetVsCustomEmoji() {
        // System preset resolves to drawableResId
        val rentResult = CategoryIconMapper.resolveExpenseIcon(
            categoryId = SeedIds.EXPENSE_RENT,
            iconKey = "home",
            isSystemCategory = true
        )
        assertEquals(R.drawable.ic_rent, rentResult.first)
        assertNull(rentResult.second)

        // Custom expense with valid emoji resolves to emoji string
        val emojiResult = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_piano_id",
            iconKey = "🎹",
            isSystemCategory = false
        )
        assertNull(emojiResult.first)
        assertEquals("🎹", emojiResult.second)

        // Custom expense with complex emoji
        val skinToneResult = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_thumbs_id",
            iconKey = "👍🏽",
            isSystemCategory = false
        )
        assertNull(skinToneResult.first)
        assertEquals("👍🏽", skinToneResult.second)

        // Unknown invalid key on custom item with no preset match
        val unknownResult = CategoryIconMapper.resolveExpenseIcon(
            categoryId = "custom_item_unknown",
            iconKey = "non_emoji_plain_text",
            isSystemCategory = false
        )
        assertNull(unknownResult.first)
        assertNull(unknownResult.second)
    }
}
