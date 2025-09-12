package com.jydev.discord.auth.infra.persistence

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class RefreshTokenRedisRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, RefreshTokenDocument>
) {
    companion object {
        private const val KEY_PREFIX = "refresh_token:"
    }
    
    private fun getKey(userId: Long): String = "$KEY_PREFIX$userId"
    
    suspend fun findById(userId: Long): RefreshTokenDocument? {
        return redisTemplate.opsForValue()
            .get(getKey(userId))
            .awaitFirstOrNull()
    }
    
    suspend fun save(document: RefreshTokenDocument): RefreshTokenDocument {
        val key = getKey(document.userId)
        val ttl = Duration.between(java.time.Instant.now(), document.expiredAt)
        
        redisTemplate.opsForValue()
            .set(key, document, ttl)
            .awaitSingle()
        
        return document
    }
    
    suspend fun deleteById(userId: Long) {
        redisTemplate.delete(getKey(userId))
            .awaitFirstOrNull()
    }
}