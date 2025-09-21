package com.jydev.discord.user.api.dto

import com.jydev.discord.domain.user.UserStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "현재 사용자 정보 응답")
data class UserApiResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "사용자명", example = "john_doe")
    val username: String,
    
    @field:Schema(description = "닉네임", example = "John")
    val nickname: String,
    
    @field:Schema(description = "사용자 상태", example = "ONLINE")
    val status: UserStatus
)