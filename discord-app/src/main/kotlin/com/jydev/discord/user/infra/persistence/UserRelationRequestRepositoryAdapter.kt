package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.RelationTarget
import com.jydev.discord.domain.user.relation.UserRelationRequest
import com.jydev.discord.domain.user.relation.UserRelationRequestRepository
import com.jydev.discord.domain.user.relation.UserRelationRequestStatus
import com.jydev.discord.user.application.UserRelationRequestDao
import com.jydev.discord.user.application.dto.UserRelationRequestReadModel
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

    override suspend fun findPendingReceivedRequests(receiverId: Long): List<UserRelationRequestReadModel> {
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
            .map { row, _ -> row.toUserRelationRequestReadModel("sender_id", "sender_username", "sender_nickname") }
            .all()
            .collectList()
            .awaitSingle()
    }
    
    override suspend fun findPendingSentRequests(senderId: Long): List<UserRelationRequestReadModel> {
        val sql = """
            SELECT 
                urr.id,
                urr.receiver_id,
                r.username as receiver_username,
                r.nickname as receiver_nickname
            FROM user_relation_request urr
            INNER JOIN users r ON urr.receiver_id = r.id
            WHERE urr.sender_id = :senderId
            AND urr.status = 'PENDING'
        """.trimIndent()
        
        return databaseClient.sql(sql)
            .bind("senderId", senderId)
            .map { row, _ -> row.toUserRelationRequestReadModel("receiver_id", "receiver_username", "receiver_nickname") }
            .all()
            .collectList()
            .awaitSingle()
    }
    
    private fun Row.toUserRelationRequestReadModel(userIdColumn: String, usernameColumn: String, nicknameColumn: String): UserRelationRequestReadModel {
        return UserRelationRequestReadModel(
            id = get("id", Long::class.java)!!,
            userId = get(userIdColumn, Long::class.java)!!,
            username = Username(get(usernameColumn, String::class.java)!!),
            nickname = Nickname(get(nicknameColumn, String::class.java)!!)
        )
    }
}