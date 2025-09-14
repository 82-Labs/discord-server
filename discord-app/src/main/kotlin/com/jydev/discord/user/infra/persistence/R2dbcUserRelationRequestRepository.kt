package com.jydev.discord.user.infra.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface R2dbcUserRelationRequestRepository : CoroutineCrudRepository<UserRelationRequestEntity, Long> {
    
    suspend fun findBySenderIdAndReceiverIdAndStatus(senderId: Long, receiverId: Long, status: String): UserRelationRequestEntity?
    
    suspend fun findByReceiverIdAndStatus(receiverId: Long, status: String): List<UserRelationRequestEntity>
}