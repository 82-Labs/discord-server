package com.jydev.discord.user.infra.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_relation")
data class UserRelationEntity(
    @Id
    @Column("id")
    val id: Long? = null,
    
    @Column("user_id")
    val userId: Long,
    
    @Column("related_user_id")
    val relatedUserId: Long,
    
    @Column("relation_type")
    val relationType: String
)