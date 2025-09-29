package com.jydev.discord.chat.channel.infra.persistence

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param

interface R2dbcUserDirectMessageChannelRepository : CoroutineCrudRepository<UserDirectMessageChannelEntity, Long> {
    
    @Query("""
        SELECT * FROM user_direct_message_channels
        WHERE user_id = :userId AND is_hidden = false
        ORDER BY channel_id
    """)
    suspend fun findVisibleByUserId(@Param("userId") userId: Long): List<UserDirectMessageChannelEntity>
    
    suspend fun findByUserId(userId: Long): List<UserDirectMessageChannelEntity>
    
    suspend fun findByUserIdAndChannelId(userId: Long, channelId: Long): UserDirectMessageChannelEntity?
}