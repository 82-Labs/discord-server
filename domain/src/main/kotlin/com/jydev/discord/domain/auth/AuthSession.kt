package com.jydev.discord.domain.auth

import java.util.*

class AuthSession(
    val userId: Long,
    sessionId: String = UUID.randomUUID().toString(),
) {
    var sessionId = sessionId
    private set

    fun rotation() {
        sessionId = UUID.randomUUID().toString()
    }
}