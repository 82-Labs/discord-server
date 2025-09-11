package com.jydev.discord.auth.infra.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface R2dbcAuthCredentialRepository : CoroutineCrudRepository<AuthCredentialEntity, Long> {
    fun findByUserId(userId: Long): AuthCredentialEntity?
    fun findByExternalIdAndProvider(externalId: String, provider : String): AuthCredentialEntity?
}