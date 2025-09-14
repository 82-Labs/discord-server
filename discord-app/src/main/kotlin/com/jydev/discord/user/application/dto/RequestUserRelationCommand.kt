package com.jydev.discord.user.application.dto

data class RequestUserRelationCommand(
    val senderId: Long,
    val receiverUsername: String
)