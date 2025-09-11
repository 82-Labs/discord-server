package com.jydev.discord.domain.auth.jwt

import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.auth.User
import com.jydev.discord.domain.auth.TemporalUser
import com.jydev.discord.domain.user.UserRole
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
    ): String {
        
        return Jwts.builder()
            .subject(authUser.id.toString())
            .claim(CLAIM_ROLES, authUser.roles.map { it.name })
            .issuedAt(Date.from(currentTime.now()))
            .expiration(Date.from(currentTime.now().plus(expiration, unit)))
            .signWith(key)
            .compact()
    }
    
    
    fun getAuthUser(token: String): AuthUser {
        val claims = parseToken(token)
        val id = extractUserId(claims)
        val roles = extractRoles(claims)
        
        return createAuthUser(id, roles)
    }
    
    private fun extractUserId(claims: Claims): Long {
        return claims.subject?.toLongOrNull() 
            ?: throw IllegalArgumentException("유효하지 않은 토큰: subject가 없거나 잘못되었습니다")
    }
    
    private fun extractRoles(claims: Claims): List<UserRole> {
        val roleNames = claims[CLAIM_ROLES] as? List<*> ?: return emptyList()
        
        return roleNames.mapNotNull { roleName ->
            (roleName as? String)?.let { name ->
                try {
                    UserRole.valueOf(name)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }
    
    private fun createAuthUser(id: Long, roles: List<UserRole>): AuthUser {
        return if (roles.contains(UserRole.TEMPORAL)) {
            TemporalUser(authCredentialId = id)
        } else {
            User(userId = id, roles = roles)
        }
    }
    
    fun isExpired(token: String): Boolean {
        return try {
            parseToken(token).expiration.before(Date.from(currentTime.now()))
        } catch (e: Exception) {
            throw IllegalArgumentException("잘못된 토큰 형식입니다", e)
        }
    }
    
    private fun parseToken(token: String): Claims = 
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}