package com.premraj.moneyboard.core.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Empirical Adversarial Challenger Test Suite for [EmojiValidator].
 * Stress-tests Unicode categories, boundary conditions, and aggressive adversarial inputs.
 */
class EmojiValidatorAdversarialChallengerTest {

    // =========================================================================
    // 1. DIVERSE UNICODE EMOJI CATEGORIES
    // =========================================================================

    @Test
    fun category_faces_accepted() {
        val faceEmojis = listOf(
            "😀", // Grinning Face (U+1F600)
            "😂", // Face with Tears of Joy (U+1F602)
            "🥹", // Face Holding Back Tears (U+1FA79)
            "🥳", // Partying Face (U+1F973)
            "🤠", // Cowboy Hat Face (U+1F920)
            "🥸", // Disguised Face (U+1F978)
            "🫠", // Melting Face (U+1FAE0)
            "🤩", // Star-Struck (U+1F929)
            "😎", // Smiling Face with Sunglasses (U+1F60E)
            "🤔"  // Thinking Face (U+1F914)
        )
        for (emoji in faceEmojis) {
            assertTrue(
                "Face emoji '$emoji' should be valid",
                EmojiValidator.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun category_symbols_accepted() {
        val symbolEmojis = listOf(
            "⚡", // High Voltage (U+26A1)
            "☕", // Hot Beverage (U+2615)
            "🎬", // Clapper Board (U+1F3AC)
            "✈️", // Airplane (U+2708 U+FE0F)
            "✨", // Sparkles (U+2728)
            "⌛", // Hourglass Done (U+231B)
            "⌚", // Watch (U+231A)
            "⭕", // Hollow Red Circle (U+2B55)
            "⬛", // Black Large Square (U+2B1B)
            "🀄"  // Mahjong Tile Red Dragon (U+1F004)
        )
        for (emoji in symbolEmojis) {
            assertTrue(
                "Symbol emoji '$emoji' should be valid",
                EmojiValidator.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun category_flags_and_regionalIndicators_accepted() {
        // Standard national flags (2 Regional Indicator Codepoints)
        val nationalFlags = listOf(
            "🇺🇸", // United States
            "🇮🇳", // India
            "🇯🇵", // Japan
            "🇧🇷", // Brazil
            "🇫🇷"  // France
        )
        for (flag in nationalFlags) {
            assertTrue(
                "National flag '$flag' should be valid",
                EmojiValidator.isValidEmoji(flag)
            )
        }

        // Single Regional Indicator symbols (U+1F1E6..U+1F1FF)
        val regionalIndicators = listOf(
            "\uD83C\uDDE6", // Regional Indicator Symbol Letter A (U+1F1E6)
            "\uD83C\uDDFF"  // Regional Indicator Symbol Letter Z (U+1F1FF)
        )
        for (ri in regionalIndicators) {
            assertTrue(
                "Regional indicator '$ri' should be valid",
                EmojiValidator.isValidEmoji(ri)
            )
        }
    }

    @Test
    fun category_skinToneModifiers_accepted() {
        val skinToneEmojis = listOf(
            "👍🏽", // Thumbs Up + Medium Skin Tone
            "👋🏻", // Waving Hand + Light Skin Tone
            "🙋🏿", // Person Raising Hand + Dark Skin Tone
            "🫱🏼", // Rightwards Hand + Medium-Light Skin Tone
            "✍🏾"  // Writing Hand + Medium-Dark Skin Tone
        )
        for (emoji in skinToneEmojis) {
            assertTrue(
                "Skin-tone modified emoji '$emoji' should be valid",
                EmojiValidator.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun category_zeroWidthJoiner_professions_family_accepted() {
        val zwjSequences = listOf(
            "👨‍👩‍👧‍👦", // Family: Man + ZWJ + Woman + ZWJ + Girl + ZWJ + Boy
            "👩‍👧",     // Family: Woman + ZWJ + Girl
            "👨‍👧‍👦",   // Family: Man + ZWJ + Girl + ZWJ + Boy
            "👩‍💻",     // Woman Technologist
            "👨‍🍳",     // Man Cook
            "🧑‍🎨",     // Artist
            "👨‍⚕️",     // Man Health Worker
            "🕵️‍♀️",     // Woman Detective
            "🏳️‍🌈",     // Rainbow Flag
            "🏳️‍⚧️"      // Transgender Flag
        )
        for (emoji in zwjSequences) {
            assertTrue(
                "ZWJ sequence '$emoji' should be valid",
                EmojiValidator.isValidEmoji(emoji)
            )
        }
    }

    @Test
    fun category_hairStyles_accepted() {
        val hairStyleEmojis = listOf(
            "👨‍🦰", // Man: Red Hair (U+1F468 U+200D U+1F9B0)
            "👩‍🦱", // Woman: Curly Hair (U+1F469 U+200D U+1F9B1)
            "🧑‍🦳", // Person: White Hair (U+1F9D1 U+200D U+1F9B3)
            "🧑‍🦲"  // Person: Bald (U+1F9D1 U+200D U+1F9B2)
        )
        for (emoji in hairStyleEmojis) {
            assertTrue(
                "Hair style emoji '$emoji' should be valid",
                EmojiValidator.isValidEmoji(emoji)
            )
        }
    }

    // =========================================================================
    // 2. AGGRESSIVE ADVERSARIAL INPUTS (MUST BE STRICTLY REJECTED)
    // =========================================================================

    @Test
    fun adversarial_emptyAndWhitespace_strictlyRejected() {
        val inputs = listOf(
            "",
            " ",
            "    ",
            "\t",
            "\n",
            "\r",
            "\r\n",
            " \t \n \r "
        )
        for (input in inputs) {
            assertFalse(
                "Blank/whitespace input '$input' must be rejected",
                EmojiValidator.isValidEmoji(input)
            )
        }
    }

    @Test
    fun adversarial_newlinesWithEmoji_strictlyRejected() {
        val inputs = listOf(
            "\n🍕",
            "🍕\n",
            "\r\n🍕",
            "🍕\r\n",
            "\t🍕",
            "🍕\t",
            " 🍕",
            "🍕 "
        )
        for (input in inputs) {
            assertFalse(
                "Emoji with whitespace/newline '$input' must be rejected",
                EmojiValidator.isValidEmoji(input)
            )
        }
    }

    @Test
    fun adversarial_multipleEmojis_strictlyRejected() {
        val inputs = listOf(
            "🍕🍔",
            "🐶🐱🐭",
            "🇮🇳🇺🇸",
            "🍕 🍔",
            "⭐✨⚡",
            "😀😀",
            "👍🏽👋🏻"
        )
        for (input in inputs) {
            assertFalse(
                "Multiple emoji sequence '$input' must be rejected",
                EmojiValidator.isValidEmoji(input)
            )
        }
    }

    @Test
    fun adversarial_emojiPrecededOrFollowedByAlphanumeric_strictlyRejected() {
        val inputs = listOf(
            "a🍕",
            "🍕a",
            "1🍕",
            "🍕1",
            "Z🍕",
            "🍕Z",
            "0🍕",
            "🍕0",
            "pizza🍕",
            "🍕pie",
            "abc",
            "123"
        )
        for (input in inputs) {
            assertFalse(
                "Emoji mixed with alphanumeric '$input' must be rejected",
                EmojiValidator.isValidEmoji(input)
            )
        }
    }

    @Test
    fun adversarial_emojiPrecededOrFollowedByPunctuation_strictlyRejected() {
        val inputs = listOf(
            "!🍕",
            "🍕!",
            "?🍕",
            "🍕?",
            "-🍕",
            "🍕-",
            "(🍕)",
            "[🍕]",
            "{🍕}",
            "/🍕",
            "🍕/"
        )
        for (input in inputs) {
            assertFalse(
                "Emoji with punctuation '$input' must be rejected",
                EmojiValidator.isValidEmoji(input)
            )
        }
    }

    @Test
    fun adversarial_keycapSequences_strictlyRejected() {
        // Keycap sequences contain ASCII character base (e.g. '1', '#', '*')
        // According to R6, non-emoji characters including numbers and punctuation must be strictly rejected
        val keycaps = listOf(
            "1️⃣", // Digit 1 + VS16 + Combining Enclosing Keycap
            "2️⃣", // Digit 2 + VS16 + Combining Enclosing Keycap
            "#️⃣", // Number Sign + VS16 + Combining Enclosing Keycap
            "*️⃣", // Asterisk + VS16 + Combining Enclosing Keycap
            "0️⃣"  // Digit 0 + VS16 + Combining Enclosing Keycap
        )
        for (kc in keycaps) {
            assertFalse(
                "Keycap sequence '$kc' containing ASCII base must be strictly rejected",
                EmojiValidator.isValidEmoji(kc)
            )
        }
    }

    @Test
    fun adversarial_asciiControlCharacters_strictlyRejected() {
        val controls = listOf(
            "\u0000",         // NUL
            "\u0007",         // BEL
            "\u0008",         // BS
            "\u001B",         // ESC
            "\u007F",         // DEL
            "🍕\u0000",       // Emoji + NUL
            "\u0000🍕",       // NUL + Emoji
            "🍕\u001B",       // Emoji + ESC
            "\u001B🍕"        // ESC + Emoji
        )
        for (ctrl in controls) {
            assertFalse(
                "ASCII control character input must be rejected",
                EmojiValidator.isValidEmoji(ctrl)
            )
        }
    }

    @Test
    fun adversarial_htmlEntities_strictlyRejected() {
        val htmlEntities = listOf(
            "&amp;",
            "&#128512;",
            "&lt;",
            "&gt;",
            "&quot;",
            "&#x1F355;",
            "&nbsp;"
        )
        for (entity in htmlEntities) {
            assertFalse(
                "HTML entity string '$entity' must be rejected",
                EmojiValidator.isValidEmoji(entity)
            )
        }
    }

    @Test
    fun adversarial_surrogateHalves_handledSafelyWithoutCrash() {
        val surrogateStrings = listOf(
            "\uD83D",         // Unpaired high surrogate
            "\uDE00",         // Unpaired low surrogate
            "\uDE00\uD83D",   // Reversed surrogates
            "🍕\uD83D",       // Valid emoji followed by unpaired high surrogate
            "\uD83D🍕"        // Unpaired high surrogate followed by valid emoji
        )
        for (s in surrogateStrings) {
            val result = runCatching { EmojiValidator.isValidEmoji(s) }
            assertTrue("Surrogate input should not throw uncaught exceptions", result.isSuccess)
            assertFalse("Surrogate input must not be treated as a valid emoji", result.getOrDefault(false))
        }
    }

    @Test
    fun adversarial_nonEmojiUnicodeScriptsAndSymbols_strictlyRejected() {
        val nonEmoji = listOf(
            "Ω", "α", "β",       // Greek
            "Я", "Ж", "Б",       // Cyrillic
            "中", "文", "字",     // CJK Ideographs
            "ع", "ر", "ب",       // Arabic
            "क", "ख", "ॐ",       // Devanagari
            "∑", "√", "∫", "≈",  // Mathematical operators
            "$", "€", "₹", "¥",  // Currency symbols
            "©", "®", "™"        // Copyright/trademark symbols
        )
        for (ch in nonEmoji) {
            assertFalse(
                "Non-emoji Unicode symbol/script '$ch' must be rejected",
                EmojiValidator.isValidEmoji(ch)
            )
        }
    }

    // =========================================================================
    // 3. EXTRACT FIRST GRAPHEME BOUNDARY CLUSTER TESTING
    // =========================================================================

    @Test
    fun extractFirstGrapheme_boundaryClusterHandling() {
        // Multi-emoji strings: extracts exactly the first cluster
        assertEquals("🍕", EmojiValidator.extractFirstGrapheme("🍕🍔🍟"))
        assertEquals("☕", EmojiValidator.extractFirstGrapheme("☕⚽"))

        // Complex ZWJ sequence: extracts the complete cluster
        assertEquals("👨‍👩‍👧‍👦", EmojiValidator.extractFirstGrapheme("👨‍👩‍👧‍👦 family group"))
        assertEquals("👩‍💻", EmojiValidator.extractFirstGrapheme("👩‍💻 developer"))

        // Flags: extracts the 2-codepoint flag
        assertEquals("🇮🇳", EmojiValidator.extractFirstGrapheme("🇮🇳 India"))
        assertEquals("🇺🇸", EmojiValidator.extractFirstGrapheme("🇺🇸 USA"))

        // Skin-tone modifiers: extracts base + modifier
        assertEquals("👍🏽", EmojiValidator.extractFirstGrapheme("👍🏽 thumbs up"))

        // Hair styles: extracts person + ZWJ + hair
        assertEquals("👨‍🦰", EmojiValidator.extractFirstGrapheme("👨‍🦰 red hair"))

        // Plain text inputs
        assertEquals("a", EmojiValidator.extractFirstGrapheme("abc"))
        assertEquals("1", EmojiValidator.extractFirstGrapheme("123"))
        assertEquals(" ", EmojiValidator.extractFirstGrapheme(" hello"))

        // Empty string
        assertNull(EmojiValidator.extractFirstGrapheme(""))
    }
}
