package com.jydev.discord.user.application

import com.jydev.discord.domain.user.relation.UserRelationType
import com.jydev.discord.user.application.dto.UserRelationReadModel

interface UserRelationDao {
    suspend fun findUserRelationsWithUserInfo(userId: Long): List<UserRelationReadModel>
    suspend fun findUserRelationsWithUserInfoByType(userId: Long, type: UserRelationType): List<UserRelationReadModel>
}