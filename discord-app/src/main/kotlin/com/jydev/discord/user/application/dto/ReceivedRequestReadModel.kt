package com.jydev.discord.user.application.dto

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.Username

data class ReceivedRequestReadModel(
    val id: Long,
    val senderId: Long,
    val senderUsername: Username,
    val senderNickname: Nickname
)