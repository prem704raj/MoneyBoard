package com.premraj.moneyboard.core.common

fun interface TimeProvider {
    fun nowEpochMillis(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
