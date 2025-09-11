package com.jydev.discord.domain.auth

interface AuthCredentialRepository {
    suspend fun findByUserId(userId: Long): AuthCredential?
    suspend fun findByAuthProvider(authProvider: AuthProvider): AuthCredential?
    suspend fun save(credential: AuthCredential): AuthCredential
}