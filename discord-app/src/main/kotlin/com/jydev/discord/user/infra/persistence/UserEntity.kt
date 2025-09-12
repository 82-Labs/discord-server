package com.jydev.discord.user.infra.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class UserEntity(
    @Id
    @Column("id")
    val id: Long?,

    @Column("nickname")
    val nickname: String,

    @Column("username")
    val username: String,

    @Column("roles")
    val roles: String
)