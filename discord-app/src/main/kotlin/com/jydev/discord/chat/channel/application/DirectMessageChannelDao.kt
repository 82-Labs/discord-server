package com.jydev.discord.chat.channel.application

import com.jydev.discord.chat.channel.application.dto.DirectMessageChannelReadModel

interface DirectMessageChannelDao {
    
    suspend fun findByUserIds(userIds: Set<Long>): DirectMessageChannelReadModel?
    
    suspend fun findAllChannelsByUserId(userId: Long): List<DirectMessageChannelReadModel>
}