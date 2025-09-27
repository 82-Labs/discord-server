package com.jydev.discord.chat.channel.infra.persistence

import com.jydev.discord.chat.application.dto.DirectMessageChannelReadModel
import com.jydev.discord.domain.chat.DirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannel

// MongoDB Mappers
fun DirectMessageChannelDocument.toDomain(): DirectMessageChannel {
    return DirectMessageChannel(
        id = this.id,
        userIds = this.userIds
    )
}

fun DirectMessageChannel.toDocument(): DirectMessageChannelDocument {
    return DirectMessageChannelDocument(
        id = this.id,
        userIds = this.userIds
    )
}

fun DirectMessageChannelDocument.toReadModel(): DirectMessageChannelReadModel {
    return DirectMessageChannelReadModel(
        channelId = this.id,
        userIds = this.userIds
    )
}

// R2DBC Mappers
fun UserDirectMessageChannelEntity.toDomain(): UserDirectMessageChannel {
    return UserDirectMessageChannel(
        id = this.id,
        userId = this.userId,
        channelId = this.channelId,
        isHidden = this.isHidden
    )
}

fun UserDirectMessageChannel.toEntity(): UserDirectMessageChannelEntity {
    return UserDirectMessageChannelEntity(
        id = this.id,
        userId = this.userId,
        channelId = this.channelId,
        isHidden = this.isHidden
    )
}