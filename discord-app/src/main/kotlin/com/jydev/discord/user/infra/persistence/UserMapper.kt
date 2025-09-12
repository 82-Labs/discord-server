package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username

fun UserEntity.toDomain(): User {
    return User.of(
        userId = id,
        nickname = Nickname(nickname),
        username = Username(username),
        roles = if (roles.isBlank()) emptyList() else roles.split(",").map { UserRole.valueOf(it) },
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        nickname = nickname.value,
        username = username.value,
        roles = roles.joinToString(",") { it.name },
    )
}