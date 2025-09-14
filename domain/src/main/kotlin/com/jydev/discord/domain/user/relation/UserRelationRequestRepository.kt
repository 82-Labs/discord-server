package com.jydev.discord.domain.user.relation

interface UserRelationRequestRepository {
    suspend fun save(relationRequest: UserRelationRequest): UserRelationRequest
    suspend fun findById(id: Long): UserRelationRequest?
    suspend fun findByRequester(requester: RelationTarget): UserRelationRequest?
    
    // 받은 대기중인 요청 목록
    suspend fun findPendingByReceiverId(receiverId: Long): List<UserRelationRequest>
}