package com.jydev.discord.user.application

import com.jydev.discord.user.application.dto.UserReadModel

interface UserDao {
    suspend fun findById(userId: Long): UserReadModel?
}