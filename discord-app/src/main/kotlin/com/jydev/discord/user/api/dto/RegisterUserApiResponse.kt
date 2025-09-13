package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 등록 응답")
data class RegisterUserApiResponse(
    @field:Schema(
        description = "JWT 액세스 토큰",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken: String,
    
    @field:Schema(
        description = "UUID 리프레시 토큰",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    val refreshToken: String
)