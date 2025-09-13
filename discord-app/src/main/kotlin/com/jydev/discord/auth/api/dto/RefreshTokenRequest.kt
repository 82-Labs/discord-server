package com.jydev.discord.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @field:NotBlank(message = "액세스 토큰은 필수입니다")
    @Schema(
        description = "만료된 액세스 토큰 (Bearer 제외)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        required = true
    )
    val accessToken : String,
    
    @field:NotBlank(message = "리프레시 토큰은 필수입니다")
    @Schema(
        description = "유효한 리프레시 토큰",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    val refreshToken : String
)
