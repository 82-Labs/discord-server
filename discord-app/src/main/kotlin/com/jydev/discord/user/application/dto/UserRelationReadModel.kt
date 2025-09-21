package com.jydev.discord.user.application.dto

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.UserStatus
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.UserRelationType

data class UserRelationReadModel(
    val id: Long,
    val relatedUserId: Long,
    val relatedUsername: Username,
    val relatedNickname: Nickname,
    val relationType: UserRelationType,
    val relatedUserStatus: UserStatus = UserStatus.OFFLINE
)