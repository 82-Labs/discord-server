package com.jydev.discord.domain.chat

interface UserDirectMessageChannelRepository {
    
    suspend fun findByUserIdAndChannelId(userId: Long, channelId: Long): UserDirectMessageChannel?
    
    suspend fun save(userChannel: UserDirectMessageChannel): UserDirectMessageChannel
}