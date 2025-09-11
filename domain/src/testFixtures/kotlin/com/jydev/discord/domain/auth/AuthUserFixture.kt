package com.jydev.discord.domain.auth

import com.jydev.discord.domain.user.UserRole

object AuthUserFixture {
    
    fun createUser(
        userId: Long = 1L,
        roles: List<UserRole> = listOf(UserRole.USER)
    ): User = User(userId, roles)
    
    fun createAdmin(
        userId: Long = 2L,
        roles: List<UserRole> = listOf(UserRole.ADMIN, UserRole.USER)
    ): User = User(userId, roles)
    
    fun createTemporalUser(
        authCredentialId: Long = 999L
    ): TemporalUser = TemporalUser(authCredentialId)
    
    fun createUserWithoutRoles(
        userId: Long = 6L
    ): User = User(userId, emptyList())
    
    fun createMultiRoleUser(
        userId: Long = 12345L,
        roles: List<UserRole> = listOf(UserRole.USER, UserRole.ADMIN)
    ): User = User(userId, roles)
}