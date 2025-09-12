package com.jydev.discord.auth.infra.persistence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
data class RefreshTokenDocument(
    val userId: Long = 0L,
    val token: String = "",
    val sessionId: String = "",
    val expiredAt: Instant = Instant.now()
)