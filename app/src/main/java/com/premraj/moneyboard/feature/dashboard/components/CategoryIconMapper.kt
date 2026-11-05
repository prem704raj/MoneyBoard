package com.premraj.moneyboard.feature.dashboard.components

import androidx.annotation.DrawableRes
import com.premraj.moneyboard.R
import com.premraj.moneyboard.core.database.SeedIds
import com.premraj.moneyboard.core.validation.EmojiValidator

/**
 * Maps category keys and IDs to the 24 illustrated PNG drawables extracted from Image 2.
 * Also provides resolution for custom emoji icons and fallback presets.
 */
object CategoryIconMapper {

    data class PresetIconDefinition(
        val categoryId: String,
        val defaultName: String,
        val iconKeys: List<String>,
        @param:DrawableRes val drawableRes: Int
    )

    val PRESET_DEFINITIONS: List<PresetIconDefinition> = listOf(
        PresetIconDefinition(SeedIds.EXPENSE_RENT, "Rent", listOf("rent", "home"), R.drawable.ic_rent),
        PresetIconDefinition(SeedIds.EXPENSE_GROCERIES, "Groceries", listOf("groceries"), R.drawable.ic_groceries),
        PresetIconDefinition(SeedIds.EXPENSE_GAS, "Gas", listOf("gas", "fuel"), R.drawable.ic_gas),
        PresetIconDefinition(SeedIds.EXPENSE_HOUSE_HELP, "Maid", listOf("maid", "house_help"), R.drawable.ic_maid),
        PresetIconDefinition(SeedIds.EXPENSE_DINING, "Online food", listOf("online_food", "food", "dining"), R.drawable.ic_online_food),
        PresetIconDefinition(SeedIds.EXPENSE_ELECTRICITY, "Electricity", listOf("electricity"), R.drawable.ic_electricity),
        PresetIconDefinition(SeedIds.EXPENSE_WIFI, "Wi-Fi", listOf("wifi"), R.drawable.ic_wifi),
        PresetIconDefinition(SeedIds.EXPENSE_MOBILE, "Mobile recharge", listOf("mobile_recharge", "mobile", "phone"), R.drawable.ic_mobile_recharge),
        PresetIconDefinition(SeedIds.EXPENSE_CAB, "Cab/Rapido", listOf("cab_rapido", "cab", "rapido"), R.drawable.ic_cab_rapido),
        PresetIconDefinition(SeedIds.EXPENSE_LAUNDRY, "Laundry", listOf("laundry"), R.drawable.ic_laundry),
        PresetIconDefinition(SeedIds.EXPENSE_SHOPPING, "Shopping", listOf("shopping"), R.drawable.ic_shopping),
        PresetIconDefinition(SeedIds.EXPENSE_GYM, "Gym", listOf("gym", "fitness"), R.drawable.ic_gym),
        PresetIconDefinition(SeedIds.EXPENSE_FITNESS_DIET, "Gym diet", listOf("gym_diet", "fitness_diet"), R.drawable.ic_gym_diet),
        PresetIconDefinition(SeedIds.EXPENSE_SALON, "Skincare and saloon", listOf("skincare_saloon", "salon", "saloon", "skincare"), R.drawable.ic_skincare_saloon),
        PresetIconDefinition(SeedIds.EXPENSE_OUTING, "Weekend outing", listOf("weekend_outing", "outing"), R.drawable.ic_weekend_outing),
        PresetIconDefinition(SeedIds.EXPENSE_TRAVEL, "Domestic trip", listOf("domestic_trip", "travel", "trip"), R.drawable.ic_domestic_trip),
        PresetIconDefinition(SeedIds.EXPENSE_HOMETOWN, "Hometown visit", listOf("hometown_visit", "hometown", "home_travel"), R.drawable.ic_hometown_visit),
        PresetIconDefinition(SeedIds.EXPENSE_MEDICINE, "Medicine", listOf("medicine", "medical", "pharmacy"), R.drawable.ic_medicine),
        PresetIconDefinition(SeedIds.EXPENSE_FAMILY_SUPPORT, "Sending to parents", listOf("sending_to_parents", "family", "parents", "family_support"), R.drawable.ic_sending_to_parents),
        PresetIconDefinition(SeedIds.INVESTMENT_SIP, "SIP", listOf("sip", "investment"), R.drawable.ic_sip),
        PresetIconDefinition(SeedIds.EXPENSE_HEALTH_INSURANCE, "Health insurance", listOf("health_insurance", "insurance"), R.drawable.ic_health_insurance),
        PresetIconDefinition(SeedIds.SAVING_LIC_POLICY, "LIC", listOf("lic", "lic_policy"), R.drawable.ic_lic),
        PresetIconDefinition(SeedIds.SAVING_EMERGENCY_FUND, "Emergency fund", listOf("emergency_fund", "emergency", "savings"), R.drawable.ic_emergency_fund),
        PresetIconDefinition(SeedIds.EXPENSE_OTHER, "Other expenses", listOf("other_expenses", "other"), R.drawable.ic_other_expenses)
    )

    private val ID_TO_DRAWABLE: Map<String, Int> = PRESET_DEFINITIONS.associate { it.categoryId to it.drawableRes }

    private val KEY_TO_DRAWABLE: Map<String, Int> = buildMap {
        for (def in PRESET_DEFINITIONS) {
            put(def.categoryId.lowercase(), def.drawableRes)
            for (key in def.iconKeys) {
                put(key.lowercase(), def.drawableRes)
            }
        }
        // Supporting fallbacks
        put(SeedIds.EXPENSE_FUEL.lowercase(), R.drawable.ic_gas)
    }

    /**
     * Resolves the illustrated drawable resource ID for a preset given [categoryId] and optional [iconKey].
     * Returns null if no matching preset drawable is found.
     */
    @DrawableRes
    fun getPresetDrawable(categoryId: String, iconKey: String = ""): Int? {
        val byId = ID_TO_DRAWABLE[categoryId]
        if (byId != null) return byId

        val byKey = KEY_TO_DRAWABLE[iconKey.lowercase().trim()]
        if (byKey != null) return byKey

        val byCatLower = KEY_TO_DRAWABLE[categoryId.lowercase().trim()]
        return byCatLower
    }

    /**
     * Returns the illustrated drawable resource ID for a given key or category ID, or null.
     */
    @DrawableRes
    fun getCategoryDrawableRes(keyOrId: String): Int? {
        return KEY_TO_DRAWABLE[keyOrId.lowercase().trim()]
    }

    /**
     * Resolves the visual representation of an expense item.
     * Returns Pair(drawableResId, emojiText):
     * - For presets: Pair(drawableRes, null)
     * - For valid emojis: Pair(null, emoji)
     * - Fallback: Pair(drawableRes, null) or Pair(null, null)
     */
    fun resolveExpenseIcon(
        categoryId: String,
        iconKey: String,
        isSystemCategory: Boolean = true
    ): Pair<Int?, String?> {
        if (isSystemCategory) {
            val preset = getPresetDrawable(categoryId, iconKey)
            if (preset != null) return Pair(preset, null)
        }

        // Check if iconKey is a valid emoji
        if (EmojiValidator.isValidEmoji(iconKey)) {
            return Pair(null, iconKey)
        }

        // Fallback to preset if available
        val fallback = getPresetDrawable(categoryId, iconKey)
        return if (fallback != null) Pair(fallback, null) else Pair(null, null)
    }
}

/**
 * Top-level convenience mapping function.
 */
@DrawableRes
fun categoryDrawableRes(keyOrId: String): Int? = CategoryIconMapper.getCategoryDrawableRes(keyOrId)
