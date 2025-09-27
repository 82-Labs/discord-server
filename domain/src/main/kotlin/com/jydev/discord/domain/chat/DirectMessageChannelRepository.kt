package com.jydev.discord.domain.chat

interface DirectMessageChannelRepository {
    
    suspend fun save(channel: DirectMessageChannel): DirectMessageChannel
    
    suspend fun deleteById(channelId: Long)
}