package com.jydev.discord.domain.auth

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class RefreshToken(
    val token : String,
    val session: AuthSession,
    expirationDays : Long,
) {

    companion object {
        fun create(userId: Long, expirationDays : Long) : RefreshToken =
            RefreshToken(
                token = UUID.randomUUID().toString(),
                session = AuthSession(userId = userId),
                expirationDays = expirationDays
            )
        
        fun of(
            token: String,
            session: AuthSession,
            expiredAt: Instant
        ): RefreshToken {
            // expiredAt과 현재 시간의 차이를 일 단위로 계산
            val daysBetween = ChronoUnit.DAYS.between(Instant.now(), expiredAt)
            val expirationDays = if (daysBetween > 0) daysBetween else 0
            
            return RefreshToken(
                token = token,
                session = session,
                expirationDays = expirationDays
            )
        }
    }

    var expiredAt = calculateExpiredAt(expirationDays)
        private set

    fun refresh(expiration : Long) {
        session.rotation()
        expiredAt = calculateExpiredAt(expiration)
    }

    private fun calculateExpiredAt(expiration: Long): Instant =
        Instant.now().plus(expiration, ChronoUnit.DAYS)
}