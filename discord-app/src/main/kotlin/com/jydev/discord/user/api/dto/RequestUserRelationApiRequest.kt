package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "친구 요청 보내기")
data class RequestUserRelationApiRequest(
    
    @field:Schema(
        description = "친구 요청을 받을 사용자명",
        example = "john_doe",
        required = true
    )
    @field:NotBlank(message = "사용자명은 필수입니다")
    @field:Size(min = 2, max = 32, message = "사용자명은 2자 이상 32자 이하여야 합니다")
    @field:Pattern(
        regexp = "^[a-z0-9_]+$",
        message = "사용자명은 영문 소문자, 숫자, 언더스코어만 사용 가능합니다"
    )
    val receiverUsername: String
)