package com.jydev.discord.user.application.dto

data class RegisterUserResult(
    val accessToken : String,
    val refreshToken : String,
)
