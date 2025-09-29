package com.jydev.discord.chat.channel.api.dto

import com.jydev.discord.domain.user.UserStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채널 참여자 정보")
data class ChannelUserResponse(
    @Schema(description = "사용자 ID", example = "123456789")
    val userId: Long,
    
    @Schema(description = "사용자 이름", example = "user123")
    val username: String,
    
    @Schema(description = "닉네임", example = "홍길동")
    val nickname: String,
    
    @Schema(description = "사용자 상태", example = "ONLINE")
    val status: UserStatus
)