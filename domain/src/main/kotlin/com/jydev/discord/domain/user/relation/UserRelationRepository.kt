package com.jydev.discord.domain.user.relation

interface UserRelationRepository {
    suspend fun save(relation: UserRelation): UserRelation
    suspend fun saveAll(relations: List<UserRelation>): List<UserRelation>
    suspend fun findByUserIdAndType(userId: Long, type: UserRelationType): List<UserRelation>
    suspend fun findByTarget(target: RelationTarget): UserRelation?
    suspend fun deleteByTarget(target: RelationTarget)
    suspend fun deleteAllByUserId(userId: Long)
}