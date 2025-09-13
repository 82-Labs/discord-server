package com.jydev.discord.domain.auth

interface RefreshTokenRepository {
    suspend fun findByUserId(userId: Long): RefreshToken?
    suspend fun save(refreshToken: RefreshToken): RefreshToken
    suspend fun deleteByUserId(userId: Long)
}