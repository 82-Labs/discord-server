package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "친구 요청 처리")
data class HandleUserRelationRequestApiRequest(
    
    @field:Schema(
        description = "처리할 친구 요청 ID",
        example = "1",
        required = true
    )
    @field:NotNull(message = "요청 ID는 필수입니다")
    @field:Positive(message = "요청 ID는 양수여야 합니다")
    val requestId: Long
)