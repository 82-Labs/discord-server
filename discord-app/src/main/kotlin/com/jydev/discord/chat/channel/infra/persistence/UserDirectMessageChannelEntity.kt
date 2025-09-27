package com.jydev.discord.chat.channel.infra.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_direct_message_channels")
data class UserDirectMessageChannelEntity(
    @Id
    @Column("id")
    val id: Long? = null,
    
    @Column("user_id")
    val userId: Long,
    
    @Column("channel_id")
    val channelId: Long,
    
    @Column("is_hidden")
    val isHidden: Boolean = false
)