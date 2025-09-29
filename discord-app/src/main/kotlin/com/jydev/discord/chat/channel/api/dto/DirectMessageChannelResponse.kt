package com.jydev.discord.chat.channel.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "다이렉트 메시지 채널 응답")
data class DirectMessageChannelResponse(
    @Schema(description = "채널 ID", example = "123456789")
    val channelId: Long,
    
    @Schema(description = "채널에 참여한 사용자 ID 목록", example = "[1, 2, 3]")
    val userIds: Set<Long>,
    
    @Schema(description = "채널 숨김 여부 (true: 숨김, false: 표시)", example = "false")
    val hidden: Boolean
)