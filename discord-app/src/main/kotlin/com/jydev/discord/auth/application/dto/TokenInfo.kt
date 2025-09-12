package com.jydev.discord.auth.application.dto

data class TokenInfo(
    val accessToken: String,
    val refreshToken: String? = null,
)
