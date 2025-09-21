package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.UserStatus
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.*

// User 매핑
fun UserEntity.toDomain(): User {
    return User.of(
        userId = id,
        nickname = Nickname(nickname),
        username = Username(username),
        roles = if (roles.isBlank()) emptyList() else roles.split(",").map { UserRole.valueOf(it) },
        status = UserStatus.valueOf(status)
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        nickname = nickname.value,
        username = username.value,
        roles = roles.joinToString(",") { it.name },
        status = status.name
    )
}

// UserRelation 매핑
fun UserRelation.toEntity(): UserRelationEntity {
    return UserRelationEntity(
        id = id,
        userId = userId,
        relatedUserId = relatedUserId,
        relationType = relationType.name
    )
}

fun UserRelationEntity.toDomain(): UserRelation {
    return UserRelation(
        id = id,
        target = RelationTarget.forRelation(userId, relatedUserId),
        relationType = UserRelationType.valueOf(relationType)
    )
}

// UserRelationRequest 매핑
fun UserRelationRequest.toEntity(): UserRelationRequestEntity {
    return UserRelationRequestEntity(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        status = status.name
    )
}

fun UserRelationRequestEntity.toDomain(): UserRelationRequest {
    return UserRelationRequest(
        id = id,
        requester = RelationTarget.forRequest(senderId, receiverId),
        status = UserRelationRequestStatus.valueOf(status)
    )
}