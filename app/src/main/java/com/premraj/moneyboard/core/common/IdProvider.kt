package com.premraj.moneyboard.core.common

import java.util.UUID

fun interface IdProvider {
    fun newId(): String
}

object UuidIdProvider : IdProvider {
    override fun newId(): String = UUID.randomUUID().toString()
}
