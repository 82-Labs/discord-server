package com.jydev.discord.domain.auth

object AuthUserFixture {
    
    fun createUser(
        userId: Long = 1L,
        roles: List<String> = listOf("USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createAdmin(
        userId: Long = 2L,
        roles: List<String> = listOf("ADMIN", "USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createModerator(
        userId: Long = 3L,
        roles: List<String> = listOf("MODERATOR", "USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createPremiumUser(
        userId: Long = 4L,
        roles: List<String> = listOf("PREMIUM", "USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createBetaTester(
        userId: Long = 5L,
        roles: List<String> = listOf("BETA_TESTER", "USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createUserWithoutRoles(
        userId: Long = 6L
    ): AuthUser = AuthUser(userId, emptyList())
    
    fun createSuperAdmin(
        userId: Long = 100L,
        roles: List<String> = listOf("SUPER_ADMIN", "ADMIN", "MODERATOR", "USER")
    ): AuthUser = AuthUser(userId, roles)
    
    fun createMultiRoleUser(
        userId: Long = 12345L,
        roles: List<String> = listOf("USER", "PREMIUM", "BETA_TESTER")
    ): AuthUser = AuthUser(userId, roles)
}