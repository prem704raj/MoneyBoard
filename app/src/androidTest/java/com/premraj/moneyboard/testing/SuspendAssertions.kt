package com.premraj.moneyboard.testing

suspend inline fun <reified T : Throwable> assertSuspendThrows(
    crossinline block: suspend () -> Unit
) {
    var thrown: Throwable? = null
    try {
        block()
    } catch (error: Throwable) {
        thrown = error
    }
    if (thrown !is T) {
        throw AssertionError(
            "Expected ${T::class.java.name}, got ${thrown?.javaClass?.name ?: "no exception"}",
            thrown
        )
    }
}
