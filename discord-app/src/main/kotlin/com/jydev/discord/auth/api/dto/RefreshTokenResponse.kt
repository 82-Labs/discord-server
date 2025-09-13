package com.jydev.discord.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 갱신 응답")
data class RefreshTokenResponse(
    @Schema(
        description = "새로 발급된 JWT 액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken : String,

    @Schema(
        description = "새로 갱신된 리프레시 토큰",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    val refreshToken : String
)
