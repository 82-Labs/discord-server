package com.jydev.discord.domain.auth

class AuthProvider(
    val type: ProviderType,
    val externalId: ExternalId
)