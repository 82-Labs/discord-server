package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.RelationTarget
import com.jydev.discord.domain.user.relation.UserRelationRequest
import com.jydev.discord.domain.user.relation.UserRelationRequestRepository
import com.jydev.discord.domain.user.relation.UserRelationRequestStatus
import com.jydev.discord.user.application.UserRelationRequestDao
import com.jydev.discord.user.application.dto.ReceivedRequestReadModel
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository  
class UserRelationRequestRepositoryAdapter(
    private val r2dbcUserRelationRequestRepository: R2dbcUserRelationRequestRepository,
    private val databaseClient: DatabaseClient
) : UserRelationRequestRepository, UserRelationRequestDao {
    
    override suspend fun save(relationRequest: UserRelationRequest): UserRelationRequest {
        return r2dbcUserRelationRequestRepository.save(relationRequest.toEntity()).toDomain()
    }
    
    override suspend fun findById(id: Long): UserRelationRequest? {
        return r2dbcUserRelationRequestRepository.findById(id)?.toDomain()
    }
    
    override suspend fun findByRequester(requester: RelationTarget): UserRelationRequest? {
        return r2dbcUserRelationRequestRepository.findBySenderIdAndReceiverIdAndStatus(
            requester.userId,
            requester.targetUserId,
            UserRelationRequestStatus.PENDING.name
        )?.toDomain()
    }
    
    override suspend fun findPendingByReceiverId(receiverId: Long): List<UserRelationRequest> {
        return r2dbcUserRelationRequestRepository.findByReceiverIdAndStatus(
            receiverId,
            UserRelationRequestStatus.PENDING.name
        ).map { it.toDomain() }
    }

    override suspend fun findPendingReceivedRequests(receiverId: Long): List<ReceivedRequestReadModel> {
        val sql = """
            SELECT 
                urr.id,
                urr.sender_id,
                s.username as sender_username,
                s.nickname as sender_nickname
            FROM user_relation_request urr
            INNER JOIN users s ON urr.sender_id = s.id
            WHERE urr.receiver_id = :receiverId
            AND urr.status = 'PENDING'
        """.trimIndent()
        
        return databaseClient.sql(sql)
            .bind("receiverId", receiverId)
            .map { row, _ -> row.toReceivedRequestReadModel() }
            .all()
            .collectList()
            .awaitSingle()
    }
    
    private fun Row.toReceivedRequestReadModel(): ReceivedRequestReadModel {
        return ReceivedRequestReadModel(
            id = get("id", Long::class.java)!!,
            senderId = get("sender_id", Long::class.java)!!,
            senderUsername = Username(get("sender_username", String::class.java)!!),
            senderNickname = Nickname(get("sender_nickname", String::class.java)!!)
        )
    }
}