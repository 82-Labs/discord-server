package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 정보 응답")
data class UserInfoResponse(
    @field:Schema(
        description = "사용자 ID",
        example = "1",
        nullable = true
    )
    val userId: Long? = null,
    
    @field:Schema(
        description = "사용자명",
        example = "test_user123",
        nullable = true
    )
    val username: String? = null,
    
    @field:Schema(
        description = "닉네임",
        example = "테스트유저",
        nullable = true
    )
    val nickname: String? = null,
    
    @field:Schema(
        description = "권한 목록",
        example = "[\"USER\", \"ADMIN\"]"
    )
    val roles: List<String> = emptyList(),
    
    @field:Schema(
        description = "임시 사용자 여부",
        example = "false"
    )
    val isTemporalUser: Boolean = false,
    
    @field:Schema(
        description = "인증 정보 ID (임시 사용자인 경우만)",
        example = "123",
        nullable = true
    )
    val authCredentialId: Long? = null
)