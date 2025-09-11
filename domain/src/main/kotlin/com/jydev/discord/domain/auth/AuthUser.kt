package com.jydev.discord.domain.auth

import com.jydev.discord.domain.user.UserRole

sealed class AuthUser(
    val id : Long
) {
    abstract val roles: List<UserRole>
}

data class User(
    val userId: Long,
    override val roles: List<UserRole>
) : AuthUser(id = userId)

data class TemporalUser(
    val authCredentialId: Long
) : AuthUser(id = authCredentialId) {
    override val roles: List<UserRole> = listOf(UserRole.TEMPORAL)
}