package com.jydev.discord.user.infra.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface R2dbcUserRepository : CoroutineCrudRepository<UserEntity, Long> {
    
    suspend fun findByUsername(username: String): UserEntity?
    
    suspend fun existsByUsername(username: String): Boolean
}