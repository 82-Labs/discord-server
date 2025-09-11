package com.jydev.discord.auth.infra.persistence

import com.jydev.discord.domain.auth.AuthCredential
import com.jydev.discord.domain.auth.AuthCredentialRepository
import com.jydev.discord.domain.auth.AuthProvider
import org.springframework.stereotype.Repository

@Repository
class AuthCredentialRepositoryAdapter(
    private val r2dbcAuthCredentialRepository: R2dbcAuthCredentialRepository
) : AuthCredentialRepository {
    override suspend fun findByUserId(userId: Long): AuthCredential? {
        return r2dbcAuthCredentialRepository.findByUserId(userId)?.toDomain()
    }

    override suspend fun findByAuthProvider(authProvider: AuthProvider): AuthCredential? {
        return r2dbcAuthCredentialRepository.findByExternalIdAndProvider(authProvider.externalId.value, authProvider.type.name)?.toDomain()
    }

    override suspend fun save(credential: AuthCredential): AuthCredential {
        return r2dbcAuthCredentialRepository.save(credential.toEntity()).toDomain()
    }
}