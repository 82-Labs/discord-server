package com.jydev.discord.user.infra.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface R2dbcUserRelationRepository : CoroutineCrudRepository<UserRelationEntity, Long> {
    
    suspend fun findByUserIdAndRelatedUserId(userId: Long, relatedUserId: Long): UserRelationEntity?
    
    suspend fun findByUserIdAndRelationType(userId: Long, relationType: String): List<UserRelationEntity>
    
    suspend fun deleteByUserIdAndRelatedUserId(userId: Long, relatedUserId: Long)
}