package com.jydev.discord.domain.auth

data class AuthUser(
    val userId: Long,
    val roles: List<String> = emptyList()
)