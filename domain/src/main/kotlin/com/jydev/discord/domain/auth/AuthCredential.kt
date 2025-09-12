package com.jydev.discord.domain.auth

class AuthCredential(
    val id: Long? = null,
    val userId : Long? = null,
    val authProvider: AuthProvider
) {

    fun isNotTemporal() = userId != null

    companion object {
        fun create(authProvider: AuthProvider, userId: Long? = null): AuthCredential {
            return AuthCredential(
                authProvider = authProvider,
                userId = userId
            )
        }
    }
}