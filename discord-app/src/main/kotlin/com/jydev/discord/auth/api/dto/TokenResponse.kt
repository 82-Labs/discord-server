package com.jydev.discord.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "인증 토큰 응답")
data class TokenResponse(
    @field:Schema(
        description = "JWT 액세스 토큰",
        example = "Access Token",
        required = true
    )
    val accessToken: String,
    
    @field:Schema(
        description = "UUID 리프레시 토큰 (임시 가입 사용자의 경우 null)",
        example = "Refresh Token",
        nullable = true,
        required = false
    )
    val refreshToken: String? = null
)