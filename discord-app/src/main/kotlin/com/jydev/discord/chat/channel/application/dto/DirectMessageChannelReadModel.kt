package com.jydev.discord.chat.channel.application.dto

data class DirectMessageChannelReadModel(
    val channelId: Long,
    val userIds: Set<Long>,
    val hidden: Boolean
)