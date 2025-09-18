package com.jydev.discord.user.application.dto

data class DeleteUserRelationCommand(
    val userId: Long,
    val targetUsername: String
)