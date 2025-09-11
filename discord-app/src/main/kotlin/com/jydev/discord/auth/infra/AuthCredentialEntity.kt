package com.jydev.discord.auth.infra

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("auth_credential")
data class AuthCredentialEntity(
    @Id
    @Column("id")
    val id: Long? = null,

    @Column("user_id")
    val userId: Long? = null,
    
    @Column("provider")
    val provider: String,
    
    @Column("external_id")
    val externalId: String,
)