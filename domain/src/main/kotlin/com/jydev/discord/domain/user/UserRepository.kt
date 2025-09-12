package com.jydev.discord.domain.user

interface UserRepository {

    suspend fun findByUserId(userId: Long): User?

    suspend fun findByUsername(username: Username): User?

    suspend fun existsByUsername(username: Username): Boolean

    suspend fun save(user: User): User

    suspend fun deleteByUserId(userId: Long)
}