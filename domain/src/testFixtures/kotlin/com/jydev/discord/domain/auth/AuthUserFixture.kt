package com.jydev.discord.domain.auth

import com.jydev.discord.domain.user.UserRole

object AuthUserFixture {
    
    fun createUser(
        userId: Long = 1L,
        sessionId: String = "session-$userId",
        roles: List<UserRole> = listOf(UserRole.USER)
    ): AuthUser.User = AuthUser.User(userId, sessionId, roles)
    
    fun createAdmin(
        userId: Long = 2L,
        sessionId: String = "admin-session-$userId",
        roles: List<UserRole> = listOf(UserRole.ADMIN, UserRole.USER)
    ): AuthUser.User = AuthUser.User(userId, sessionId, roles)
    
    fun createTemporalUser(
        authCredentialId: Long = 999L
    ): AuthUser.TemporalUser = AuthUser.TemporalUser(authCredentialId)
    
    fun createUserWithoutRoles(
        userId: Long = 6L,
        sessionId: String = "session-$userId"
    ): AuthUser.User = AuthUser.User(userId, sessionId, emptyList())
    
    fun createMultiRoleUser(
        userId: Long = 12345L,
        sessionId: String = "multi-role-session-$userId",
        roles: List<UserRole> = listOf(UserRole.USER, UserRole.ADMIN)
    ): AuthUser.User = AuthUser.User(userId, sessionId, roles)
}