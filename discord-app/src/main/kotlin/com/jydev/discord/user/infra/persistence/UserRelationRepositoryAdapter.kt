package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.UserStatus
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.*
import com.jydev.discord.user.application.UserRelationDao
import com.jydev.discord.user.application.dto.UserRelationReadModel
import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class UserRelationRepositoryAdapter(
    private val r2dbcUserRelationRepository: R2dbcUserRelationRepository,
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : UserRelationRepository, UserRelationDao {
    
    override suspend fun save(relation: UserRelation): UserRelation {
        return r2dbcUserRelationRepository.save(relation.toEntity()).toDomain()
    }
    
    override suspend fun saveAll(relations: List<UserRelation>): List<UserRelation> {
        val entities = relations.map { it.toEntity() }
        return r2dbcUserRelationRepository.saveAll(entities)
            .map { it.toDomain() }
            .toList()
    }
    
    override suspend fun findByUserIdAndType(userId: Long, type: UserRelationType): List<UserRelation> {
        return r2dbcUserRelationRepository.findByUserIdAndRelationType(userId, type.name)
            .map { it.toDomain() }
    }
    
    override suspend fun findByTarget(target: RelationTarget): UserRelation? {
        return r2dbcUserRelationRepository.findByUserIdAndRelatedUserId(
            target.userId, 
            target.targetUserId
        )?.toDomain()
    }
    
    override suspend fun deleteByTarget(target: RelationTarget) {
        r2dbcUserRelationRepository.deleteByUserIdAndRelatedUserId(
            target.userId,
            target.targetUserId
        )
    }
    
    override suspend fun deleteAllByUserId(userId: Long) {
        val query = Query.query(
            Criteria.where("user_id").`is`(userId)
                .or("related_user_id").`is`(userId)
        )
        template.delete(query, UserRelationEntity::class.java).awaitSingleOrNull()
    }
    
    // UserRelationDao 구현
    override suspend fun findUserRelationsWithUserInfo(userId: Long): List<UserRelationReadModel> {
        val sql = """
            SELECT 
                ur.id,
                ur.related_user_id,
                u.username as related_username,
                u.nickname as related_nickname,
                ur.relation_type,
                u.status as related_user_status
            FROM user_relation ur
            INNER JOIN users u ON ur.related_user_id = u.id
            WHERE ur.user_id = :userId
        """.trimIndent()
        
        return databaseClient.sql(sql)
            .bind("userId", userId)
            .map { row, _ -> row.toUserRelationReadModel() }
            .all()
            .collectList()
            .awaitSingle()
    }
    
    override suspend fun findUserRelationsWithUserInfoByType(
        userId: Long, 
        type: UserRelationType
    ): List<UserRelationReadModel> {
        val sql = """
            SELECT 
                ur.id,
                ur.related_user_id,
                u.username as related_username,
                u.nickname as related_nickname,
                ur.relation_type,
                u.status as related_user_status
            FROM user_relation ur
            INNER JOIN users u ON ur.related_user_id = u.id
            WHERE ur.user_id = :userId
            AND ur.relation_type = :relationType
        """.trimIndent()
        
        return databaseClient.sql(sql)
            .bind("userId", userId)
            .bind("relationType", type.name)
            .map { row, _ -> row.toUserRelationReadModel() }
            .all()
            .collectList()
            .awaitSingle()
    }
    
    private fun Row.toUserRelationReadModel(): UserRelationReadModel {
        val statusString = get("related_user_status", String::class.java)
        return UserRelationReadModel(
            id = get("id", Long::class.java)!!,
            relatedUserId = get("related_user_id", Long::class.java)!!,
            relatedUsername = Username(get("related_username", String::class.java)!!),
            relatedNickname = Nickname(get("related_nickname", String::class.java)!!),
            relationType = UserRelationType.valueOf(get("relation_type", String::class.java)!!),
            relatedUserStatus = statusString?.let { UserStatus.valueOf(it) } ?: UserStatus.OFFLINE
        )
    }
}