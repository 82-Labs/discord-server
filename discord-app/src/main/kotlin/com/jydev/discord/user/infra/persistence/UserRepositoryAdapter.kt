package com.jydev.discord.user.infra.persistence

import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.Username
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryAdapter(
    private val r2dbcUserRepository: R2dbcUserRepository
) : UserRepository {

    override suspend fun findByUserId(userId: Long): User? {
        return r2dbcUserRepository.findById(userId)?.toDomain()
    }

    override suspend fun findByUsername(username: Username): User? {
        return r2dbcUserRepository.findByUsername(username.value)?.toDomain()
    }

    override suspend fun existsByUsername(username: Username): Boolean {
        return r2dbcUserRepository.existsByUsername(username.value)
    }

    override suspend fun save(user: User): User {
        return r2dbcUserRepository.save(user.toEntity()).toDomain()
    }

    override suspend fun deleteByUserId(userId: Long) {
        r2dbcUserRepository.deleteById(userId)
    }
}