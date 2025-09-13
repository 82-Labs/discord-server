package com.jydev.discord.auth.application.dto

data class RefreshTokenResult(
    val accessToken: String,
    val refreshToken: String
)
