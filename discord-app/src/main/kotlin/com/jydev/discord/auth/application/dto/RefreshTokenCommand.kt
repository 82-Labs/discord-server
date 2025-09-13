package com.jydev.discord.auth.application.dto

data class RefreshTokenCommand(
    val accessToken: String,
    val refreshToken: String,
)
