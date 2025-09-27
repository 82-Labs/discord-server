package com.jydev.discord.chat.application.dto

data class DirectMessageChannelReadModel(
    val channelId: Long,
    val userIds: Set<Long>
)