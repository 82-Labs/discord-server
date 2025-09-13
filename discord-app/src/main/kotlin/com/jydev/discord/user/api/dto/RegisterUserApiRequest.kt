package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "사용자 등록 요청")
data class RegisterUserApiRequest(
    
    @field:Schema(
        description = "사용자명 (2-32자, 영문소문자/숫자/언더스코어만 허용)",
        example = "test_user123",
        required = true
    )
    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 2, max = 32, message = "사용자명은 2자 이상 32자 이하여야 합니다")
    @field:Pattern(
        regexp = "^[a-z0-9_]+$",
        message = "사용자명은 영문 소문자, 숫자, 언더스코어만 사용 가능합니다"
    )
    val username: String
)