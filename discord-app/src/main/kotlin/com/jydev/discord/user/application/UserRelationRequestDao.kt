package com.jydev.discord.user.application

import com.jydev.discord.user.application.dto.UserRelationRequestReadModel

interface UserRelationRequestDao {
    suspend fun findPendingReceivedRequests(receiverId: Long): List<UserRelationRequestReadModel>
    suspend fun findPendingSentRequests(senderId: Long): List<UserRelationRequestReadModel>
}