package com.jydev.discord.chat.channel.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "다이렉트 메시지 채널 목록 응답")
data class DirectMessageChannelsResponse(
    @Schema(description = "다이렉트 메시지 채널 목록")
    val content: List<DirectMessageChannelResponse>
)