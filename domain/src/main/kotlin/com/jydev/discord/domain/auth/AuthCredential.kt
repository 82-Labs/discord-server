package com.jydev.discord.domain.auth

class AuthCredential(
    val id: Long? = null,
    userId : Long? = null,
    val authProvider: AuthProvider
) {

    var userId : Long? = userId
        private set

    fun isNotTemporal() = userId != null

    fun initialUser(userId: Long) {
        this.userId = userId
    }

    companion object {
        fun create(authProvider: AuthProvider, userId: Long? = null): AuthCredential {
            return AuthCredential(
                authProvider = authProvider,
                userId = userId
            )
        }
    }
}