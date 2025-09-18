package com.jydev.discord.user.infra.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("user_relation_request")
data class UserRelationRequestEntity(
    @Id
    @Column("id")
    val id: Long? = null,
    
    @Column("sender_id")
    val senderId: Long,
    
    @Column("receiver_id")
    val receiverId: Long,
    
    @Column("status")
    val status: String
)