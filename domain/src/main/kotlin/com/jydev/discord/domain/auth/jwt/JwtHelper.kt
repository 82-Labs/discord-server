package com.jydev.discord.domain.auth.jwt

import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.AuthUser
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.*
import javax.crypto.SecretKey

class JwtHelper(
    secretKey: String,
    private val currentTime: CurrentTime
) {
    
    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())
    
    companion object {
        private const val CLAIM_ROLES = "roles"
    }
    
    fun generateToken(
        authUser: AuthUser, 
        expiration: Long = 15,
        unit: TemporalUnit = ChronoUnit.MINUTES
    ): String =
        Jwts.builder()
            .subject(authUser.userId.toString())
            .claim(CLAIM_ROLES, authUser.roles)
            .issuedAt(Date.from(currentTime.now()))
            .expiration(Date.from(currentTime.now().plus(expiration, unit)))
            .signWith(key)
            .compact()
    
    
    fun getAuthUser(token: String): AuthUser {
        val claims = parseToken(token)
        val userId = claims.subject?.toLongOrNull() 
            ?: throw IllegalArgumentException("Invalid token: missing or invalid subject")
        val roles = (claims[CLAIM_ROLES] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        return AuthUser(userId, roles)
    }
    
    fun isExpired(token: String): Boolean {
        return try {
            parseToken(token).expiration.before(Date.from(currentTime.now()))
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid token format", e)
        }
    }
    
    private fun parseToken(token: String): Claims = 
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}