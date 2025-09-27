package com.jydev.discord.chat.channel.infra.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "direct_message_channels")
@CompoundIndex(name = "unique_user_ids", def = "{'userIds': 1}", unique = true)
data class DirectMessageChannelDocument(
    @Id
    val id: Long,
    
    @Indexed
    val userIds: Set<Long>
)