package com.jydev.discord.chat.application

import com.jydev.discord.chat.application.dto.DirectMessageChannelReadModel

interface DirectMessageChannelDao {
    
    suspend fun findByUserIds(userIds: Set<Long>): DirectMessageChannelReadModel?
    
    suspend fun findVisibleChannelsByUserId(userId: Long): List<DirectMessageChannelReadModel>
}