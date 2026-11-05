package com.premraj.moneyboard.core.validation

import java.util.regex.Pattern

/**
 * Strict validator for user emoji keyboard inputs.
 * Isolates exactly one grapheme cluster using Unicode extended grapheme matching and validates codepoints
 * to reject non-emoji characters such as Latin/alphanumeric letters, numbers, and plain punctuation.
 */
object EmojiValidator {

    private val EXTENDED_GRAPHEME_PATTERN = Pattern.compile("\\X")

    /**
     * Isolates and returns exactly the first grapheme cluster from [text],
     * or null if the string is empty.
     */
    fun extractFirstGrapheme(text: String): String? {
        if (text.isEmpty()) return null
        val matcher = EXTENDED_GRAPHEME_PATTERN.matcher(text)
        return if (matcher.find() && matcher.start() == 0) {
            matcher.group()
        } else {
            null
        }
    }

    /**
     * Validates that [input] is exactly ONE grapheme cluster and represents an authentic emoji,
     * strictly rejecting alphanumeric characters ('a', '1'), punctuation, and control symbols.
     */
    fun isValidEmoji(input: String): Boolean {
        if (input.isBlank()) return false

        // 1. Grapheme cluster boundary check
        val matcher = EXTENDED_GRAPHEME_PATTERN.matcher(input)
        if (!matcher.matches()) {
            // Must contain exactly one grapheme cluster (rejects multi-emoji sequences like "🍕🍔")
            return false
        }

        // 2. Extract codepoints
        val codePoints = mutableListOf<Int>()
        var offset = 0
        while (offset < input.length) {
            val cp = input.codePointAt(offset)
            codePoints.add(cp)
            offset += Character.charCount(cp)
        }
        if (codePoints.isEmpty()) return false

        // 3. Strict rejection of ASCII (0x00..0x7F)
        // Immediately rejects 'a'-'z', 'A'-'Z', '0'-'9', and plain punctuation (!, @, #, $, %, etc.)
        if (codePoints.any { it <= 0x7F }) return false

        // 4. Strict rejection of standard alphabet/letter/digit/whitespace categories in BMP
        for (cp in codePoints) {
            val type = Character.getType(cp)
            if (type == Character.UPPERCASE_LETTER.toInt() ||
                type == Character.LOWERCASE_LETTER.toInt() ||
                type == Character.TITLECASE_LETTER.toInt() ||
                type == Character.MODIFIER_LETTER.toInt() ||
                type == Character.OTHER_LETTER.toInt() ||
                type == Character.DECIMAL_DIGIT_NUMBER.toInt() ||
                type == Character.LETTER_NUMBER.toInt() ||
                type == Character.CONTROL.toInt() ||
                type == Character.SPACE_SEPARATOR.toInt()
            ) {
                // Astral emoji codepoints (>= 0x1F000) are acceptable; BMP letters are strictly rejected
                if (cp < 0x1F000) return false
            }
        }

        // 5. Must contain at least one primary emoji codepoint
        val hasPrimaryEmoji = codePoints.any { isPrimaryEmoji(it) }
        if (!hasPrimaryEmoji) return false

        // 6. All remaining codepoints must be recognized emoji modifiers or joiners
        for (cp in codePoints) {
            if (!isPrimaryEmoji(cp) && !isEmojiModifierOrJoiner(cp)) {
                return false
            }
        }

        return true
    }

    private fun isPrimaryEmoji(cp: Int): Boolean {
        return when (cp) {
            in 0x1F600..0x1F64F -> true // Emoticons
            in 0x1F300..0x1F5FF -> true // Misc Symbols and Pictographs
            in 0x1F680..0x1F6FF -> true // Transport and Map
            in 0x1F900..0x1F9FF -> true // Supplemental Symbols and Pictographs
            in 0x1FA70..0x1FAFF -> true // Symbols and Pictographs Extended-A
            in 0x1FA00..0x1FA6F -> true // Chess & Symbols Extended
            in 0x2600..0x26FF -> true   // Misc Symbols (☕, ⚡, ⚽, ☀️)
            in 0x2700..0x27BF -> true   // Dingbats (✨, ✈️, ✉️)
            in 0x1F1E6..0x1F1FF -> true // Regional Indicator Symbols (Flags)
            in 0x2300..0x23FF -> true   // Misc Technical (⌚, ⌛)
            in 0x2B00..0x2BFF -> true   // Misc Symbols and Arrows (⭐, ⭕)
            in 0x25A0..0x25FF -> true   // Geometric Shapes (⬛, ⬜)
            0x1F004, 0x1F0CF -> true    // Mahjong, Playing Cards
            else -> false
        }
    }

    private fun isEmojiModifierOrJoiner(cp: Int): Boolean {
        return when (cp) {
            0x200D -> true              // Zero Width Joiner (ZWJ)
            0xFE0E, 0xFE0F -> true      // Variation Selectors
            in 0x1F3FB..0x1F3FF -> true // Fitzpatrick skin tone modifiers
            in 0x1F9B0..0x1F9B3 -> true // Hair components
            in 0xE0020..0xE007F -> true // Tag characters (subdivision flags)
            else -> false
        }
    }
}
