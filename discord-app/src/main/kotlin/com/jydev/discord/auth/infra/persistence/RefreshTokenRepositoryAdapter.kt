package com.jydev.discord.auth.infra.persistence

import com.jydev.discord.domain.auth.RefreshToken
import com.jydev.discord.domain.auth.RefreshTokenRepository
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryAdapter(
    private val redisRepository: RefreshTokenRedisRepository,
    private val mapper: RefreshTokenMapper
) : RefreshTokenRepository {
    
    override suspend fun findByUserId(userId: Long): RefreshToken? {
        return redisRepository.findById(userId)
            ?.let { mapper.toDomain(it) }
    }
    
    override suspend fun save(refreshToken: RefreshToken): RefreshToken {
        val document = mapper.toDocument(refreshToken)
        redisRepository.save(document)
        return refreshToken
    }
    
    override suspend fun deleteByUserId(userId: Long) {
        redisRepository.deleteById(userId)
    }
}