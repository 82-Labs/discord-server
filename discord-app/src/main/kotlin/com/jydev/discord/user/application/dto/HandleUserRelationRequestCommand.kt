package com.jydev.discord.user.application.dto

import com.jydev.discord.domain.user.relation.UserRelationRequestAction

data class HandleUserRelationRequestCommand(
    val requestId: Long,
    val userId: Long,
    val action: UserRelationRequestAction
)