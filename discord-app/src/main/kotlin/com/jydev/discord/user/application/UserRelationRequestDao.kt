package com.jydev.discord.user.application

import com.jydev.discord.user.application.dto.ReceivedRequestReadModel

interface UserRelationRequestDao {
    suspend fun findPendingReceivedRequests(receiverId: Long): List<ReceivedRequestReadModel>
}