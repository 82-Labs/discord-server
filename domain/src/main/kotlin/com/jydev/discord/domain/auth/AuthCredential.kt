package com.jydev.discord.domain.auth

class AuthCredential(
    val id: Long? = null,
    val userId : Long? = null,
    val authProvider: AuthProvider
) {
    companion object {
        fun create(provider: ProviderType, externalId: String, userId: Long? = null): AuthCredential {
            return AuthCredential(
                authProvider = AuthProvider(provider, ExternalId(externalId)),
                userId = userId
            )
        }
    }
}