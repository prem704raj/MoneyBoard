package com.premraj.moneyboard.core.validation

import com.premraj.moneyboard.testutil.EmojiValidatorAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Requirement-driven test suite for EmojiValidator covering Tier 1 and Tier 2.
 *
 * Requirements:
 * - ORIGINAL_REQUEST.md (Requirement R6): Custom icon input accepts user emoji keyboard input.
 *   Must isolate and retain exactly one grapheme cluster using BreakIterator.
 *   Validates against emoji codepoint / character type / range checks.
 *   Ensures non-emoji characters ('a', numbers, plain punctuation, whitespace) are strictly rejected.
 * - PROJECT.md (Interface Contracts M1, Milestones M1/M4).
 */
class EmojiValidatorTest {

    // =========================================================================
    // Tier 1: Feature Coverage — Valid Emojis
    // =========================================================================

    @Test
    fun validEmoji_singleCodepoints_accepted() {
        // Explicitly required test inputs from ORIGINAL_REQUEST.md and dispatch prompt
        val validPrimaryEmojis = listOf(
            "🍕", // U+1F355 Pizza
            "☕", // U+2615 Hot Beverage
            "⚽", // U+26BD Soccer Ball
            "⚡", // U+26A1 High Voltage
            "🎬", // U+1F3AC Clapper Board
            "🚀", // U+1F680 Rocket
            "🎉", // U+1F389 Party Popper
            "🍔", // U+1F354 Hamburger
            "🚗", // U+1F697 Automobile
            "💡", // U+1F4A1 Light Bulb
            "⭐", // U+2B50 Star
            "❤️"  // U+2764 Heavy Black Heart with optional VS16
        )

        for (emoji in validPrimaryEmojis) {
            assertTrue(
                "Expected '$emoji' to be recognized as a valid emoji",
                EmojiValidatorAdapter.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun validEmoji_multiCodepointSkinToneModifiers_accepted() {
        // Emojis with Fitzpatrick skin tone modifiers (U+1F3FB..U+1F3FF)
        val skinToneEmojis = listOf(
            "👍🏽", // Thumbs up + medium skin tone
            "👋🏻", // Waving hand + light skin tone
            "🙋🏿", // Person raising hand + dark skin tone
            "🏋️‍♂️"  // Weightlifter + male sign + VS16
        )

        for (emoji in skinToneEmojis) {
            assertTrue(
                "Expected skin tone emoji '$emoji' to be valid",
                EmojiValidatorAdapter.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun validEmoji_nationalFlags_regionalIndicators_accepted() {
        // Flag emojis consisting of 2 regional indicator symbol letters (U+1F1E6..U+1F1FF)
        val flags = listOf(
            "🇮🇳", // India (IN)
            "🇺🇸", // United States (US)
            "🇯🇵", // Japan (JP)
            "🇬🇧", // United Kingdom (GB)
            "🇩🇪"  // Germany (DE)
        )

        for (flag in flags) {
            assertTrue(
                "Expected national flag '$flag' to be valid single emoji cluster",
                EmojiValidatorAdapter.isValidEmoji(flag)
            )
        }
    }

    @Test
    fun validEmoji_zeroWidthJoinerSequences_accepted() {
        // ZWJ (U+200D) complex multi-person and profession sequences
        val zwjSequences = listOf(
            "👨‍👩‍👧‍👦", // Family: Man + ZWJ + Woman + ZWJ + Girl + ZWJ + Boy
            "👩‍💻",     // Woman technologist: Woman + ZWJ + Laptop
            "👨‍🍳",     // Man cook: Man + ZWJ + Cooking
            "🧑‍🎨"      // Artist
        )

        for (zwj in zwjSequences) {
            assertTrue(
                "Expected ZWJ sequence '$zwj' to be recognized as a valid single emoji cluster",
                EmojiValidatorAdapter.isValidEmoji(zwj)
            )
        }
    }

    // =========================================================================
    // Tier 1: Feature Coverage — Invalid Emoji Rejection
    // =========================================================================

    @Test
    fun invalidEmoji_plainAlphabetLetters_strictlyRejected() {
        val letters = listOf("a", "b", "z", "A", "M", "Z", "x")
        for (letter in letters) {
            assertFalse(
                "Alphabet letter '$letter' must be strictly rejected",
                EmojiValidatorAdapter.isValidEmoji(letter)
            )
        }
    }

    @Test
    fun invalidEmoji_plainDigits_strictlyRejected() {
        val digits = listOf("0", "1", "2", "5", "9")
        for (digit in digits) {
            assertFalse(
                "Digit '$digit' must be strictly rejected",
                EmojiValidatorAdapter.isValidEmoji(digit)
            )
        }
    }

    @Test
    fun invalidEmoji_punctuationAndSymbols_strictlyRejected() {
        val punctuation = listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")", ".", ",", "?", "/", "\\", "-", "_")
        for (p in punctuation) {
            assertFalse(
                "Punctuation '$p' must be strictly rejected",
                EmojiValidatorAdapter.isValidEmoji(p)
            )
        }
    }

    @Test
    fun invalidEmoji_whitespaceAndBlankStrings_rejected() {
        val whitespaces = listOf("", " ", "   ", "\t", "\n", "\r\n")
        for (ws in whitespaces) {
            assertFalse(
                "Whitespace or empty string '$ws' must be rejected",
                EmojiValidatorAdapter.isValidEmoji(ws)
            )
        }
    }

    @Test
    fun invalidEmoji_multiEmojiSequences_strictlyRejected() {
        // Must enforce exactly ONE grapheme cluster
        val multiEmojis = listOf(
            "🍕🍔",
            "☕⚽",
            "🇮🇳🇺🇸",
            "🍕 🍔",
            "⚡🎬🚀"
        )
        for (seq in multiEmojis) {
            assertFalse(
                "Multi-emoji sequence '$seq' must be rejected (only exactly 1 emoji allowed)",
                EmojiValidatorAdapter.isValidEmoji(seq)
            )
        }
    }

    @Test
    fun invalidEmoji_mixedTextAndEmoji_rejected() {
        val mixed = listOf(
            "a🍕",
            "🍕a",
            "1☕",
            "☕1",
            "pizza 🍕",
            "🍕!",
            "#️⃣" // Keycap containing ASCII '#'
        )
        for (m in mixed) {
            assertFalse(
                "Mixed text and emoji '$m' must be rejected",
                EmojiValidatorAdapter.isValidEmoji(m)
            )
        }
    }

    // =========================================================================
    // Tier 2: Boundary & Corner Cases
    // =========================================================================

    @Test
    fun boundary_extractFirstGrapheme_isolatesClusterProperly() {
        assertEquals("🍕", EmojiValidatorAdapter.extractFirstGrapheme("🍕🍔🍟"))
        assertEquals("☕", EmojiValidatorAdapter.extractFirstGrapheme("☕⚽"))
        assertEquals("👨‍👩‍👧‍👦", EmojiValidatorAdapter.extractFirstGrapheme("👨‍👩‍👧‍👦 family group"))
        assertEquals("🇮🇳", EmojiValidatorAdapter.extractFirstGrapheme("🇮🇳 India"))
        assertEquals("a", EmojiValidatorAdapter.extractFirstGrapheme("abc"))
        assertEquals(" ", EmojiValidatorAdapter.extractFirstGrapheme(" hello"))
        assertNull(EmojiValidatorAdapter.extractFirstGrapheme(""))
    }

    @Test
    fun boundary_nonEmojiUnicodeSymbols_rejected() {
        // Mathematical and currency symbols outside primary emoji pictograph blocks
        val symbols = listOf("∑", "√", "π", "₹", "$", "€", "£", "¥", "©", "®", "™")
        for (s in symbols) {
            assertFalse(
                "Non-emoji symbol '$s' must be rejected",
                EmojiValidatorAdapter.isValidEmoji(s)
            )
        }
    }

    @Test
    fun boundary_extremeLengthStrings_handledWithoutException() {
        val hugeAscii = "a".repeat(1000)
        assertFalse(EmojiValidatorAdapter.isValidEmoji(hugeAscii))

        val hugeEmojiSequence = "🍕".repeat(100)
        assertFalse(EmojiValidatorAdapter.isValidEmoji(hugeEmojiSequence))
    }

    @Test
    fun boundary_variationSelectorEmojis_accepted() {
        // Emojis that require Variation Selector-16 (U+FE0F) to render as color emoji
        val vs16Emojis = listOf("✈️", "✉️", "☀️", "☁️", "☂️")
        for (emoji in vs16Emojis) {
            assertTrue(
                "VS16 emoji '$emoji' should be recognized as a valid emoji",
                EmojiValidatorAdapter.isValidEmoji(emoji)
            )
        }
    }
}
