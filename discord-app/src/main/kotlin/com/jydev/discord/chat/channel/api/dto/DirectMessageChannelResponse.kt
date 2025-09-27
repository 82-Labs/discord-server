package com.jydev.discord.chat.api.dto

data class DirectMessageChannelResponse(
    val channelId: Long,
    val userIds: Set<Long>
)