package com.jydev.discord.auth.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "카카오 OAuth 인증 요청")
data class KakaoAuthRequest(
    @field:Schema(
        description = "카카오 OAuth 인증 코드",
        example = "EUJWnxLPSe8B3E6F0W6T8VQxAXMp_9D-5YsLKpC0oYsAAAGT2_Nn0Q",
        required = true
    )
    @field:NotBlank(message = "인증 코드는 필수입니다")
    val code: String
)