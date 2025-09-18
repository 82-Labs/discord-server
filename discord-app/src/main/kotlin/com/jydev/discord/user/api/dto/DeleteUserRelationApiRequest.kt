package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "사용자 관계 삭제 요청")
data class DeleteUserRelationApiRequest(
    @field:NotBlank(message = "대상 사용자명은 필수입니다")
    @field:Schema(description = "삭제할 관계의 대상 사용자명", example = "user456", required = true)
    val targetUsername: String
)