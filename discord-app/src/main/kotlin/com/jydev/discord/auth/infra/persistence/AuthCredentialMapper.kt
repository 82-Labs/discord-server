package com.jydev.discord.auth.infra.persistence

import com.jydev.discord.domain.auth.AuthCredential
import com.jydev.discord.domain.auth.AuthProvider
import com.jydev.discord.domain.auth.ExternalId
import com.jydev.discord.domain.auth.ProviderType

fun AuthCredentialEntity.toDomain(): AuthCredential {
    return AuthCredential(
        id = id,
        userId = userId,
        authProvider = AuthProvider(
            type = ProviderType.valueOf(provider),
            externalId = ExternalId(externalId)
        )
    )
}

fun AuthCredential.toEntity(): AuthCredentialEntity {
    return AuthCredentialEntity(
        id = id,
        userId = userId,
        provider = authProvider.type.name,
        externalId = authProvider.externalId.value
    )
}