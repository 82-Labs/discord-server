package com.jydev.discord.auth.infra.persistence

import com.jydev.discord.domain.auth.AuthSession
import com.jydev.discord.domain.auth.RefreshToken
import org.springframework.stereotype.Component

@Component
class RefreshTokenMapper {
    
    fun toDocument(refreshToken: RefreshToken): RefreshTokenDocument {
        return RefreshTokenDocument(
            userId = refreshToken.session.userId,
            token = refreshToken.token,
            sessionId = refreshToken.session.sessionId,
            expiredAt = refreshToken.expiredAt
        )
    }
    
    fun toDomain(document: RefreshTokenDocument): RefreshToken {
        val session = AuthSession(
            userId = document.userId,
            sessionId = document.sessionId
        )
        
        return RefreshToken.of(
            token = document.token,
            session = session,
            expiredAt = document.expiredAt
        )
    }
}