package com.jydev.discord.user.application.dto

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.Username

data class UserRelationRequestReadModel(
    val id: Long,
    val userId: Long,
    val username: Username,
    val nickname: Nickname
)