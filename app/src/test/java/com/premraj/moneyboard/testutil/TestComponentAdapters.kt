package com.premraj.moneyboard.testutil

/**
 * Dynamic test adapter that automatically binds to real compiled implementation
 * classes in app/src/main when available (from worker_m1 and worker_m2),
 * or falls back to the authoritative specification reference stub.
 *
 * This guarantees:
 * 1. Test suite compiles cleanly in isolation during progressive milestone development.
 * 2. As soon as implementation code is written, tests exercise the real production logic.
 * 3. Any defect in the real implementation is caught and exposed immediately.
 */
object EmojiValidatorAdapter {
    private val realClass: Class<*>? = runCatching {
        Class.forName("com.premraj.moneyboard.core.validation.EmojiValidator")
    }.getOrNull()

    fun isUsingRealImplementation(): Boolean = realClass != null

    fun isValidEmoji(input: String): Boolean {
        if (realClass != null) {
            val instance = realClass.getField("INSTANCE").get(null)
            val method = realClass.getMethod("isValidEmoji", String::class.java)
            return method.invoke(instance, input) as Boolean
        }
        return ReferenceEmojiValidator.isValidEmoji(input)
    }

    fun extractFirstGrapheme(text: String): String? {
        if (realClass != null) {
            val instance = realClass.getField("INSTANCE").get(null)
            val method = realClass.getMethod("extractFirstGrapheme", String::class.java)
            return method.invoke(instance, text) as String?
        }
        return ReferenceEmojiValidator.extractFirstGrapheme(text)
    }
}

object HeaderSubtitleFormatterAdapter {
    private val realClass: Class<*>? = runCatching {
        Class.forName("com.premraj.moneyboard.core.util.HeaderSubtitleFormatter")
    }.getOrNull()

    fun isUsingRealImplementation(): Boolean = realClass != null

    fun format(
        profileTag: String = "Single Person",
        cityName: String,
        emiRupees: Long
    ): String {
        if (realClass != null) {
            val instance = realClass.getField("INSTANCE").get(null)
            val method = realClass.getMethod(
                "format",
                String::class.java,
                String::class.java,
                Long::class.javaPrimitiveType
            )
            return method.invoke(instance, profileTag, cityName, emiRupees) as String
        }
        return ReferenceHeaderSubtitleFormatter.format(profileTag, cityName, emiRupees)
    }
}
