package com.jydev.discord.auth.application.dto

data class AuthResult(
    val accessToken: String,
    val refreshToken: String? = null,
)
