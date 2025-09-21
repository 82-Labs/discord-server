package com.jydev.discord.user.application.dto

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.UserStatus
import com.jydev.discord.domain.user.Username

data class UserReadModel(
    val id: Long,
    val username: Username,
    val nickname: Nickname,
    val status: UserStatus
)