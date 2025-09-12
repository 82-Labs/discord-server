package com.jydev.discord.domain.user

object UserFixture {

    fun createKakaoUser(
        nickname: String = "테스트사용자",
        username: String = "testuser",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createUserWithLongNickname(
        nickname: String = "A".repeat(30),
        username: String = "longnickname_user",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createUserWithShortNickname(
        nickname: String = "짧음",
        username: String = "short_user",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createUserWithNumberUsername(
        nickname: String = "숫자사용자",
        username: String = "123456789",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createUserWithUnderscoreUsername(
        nickname: String = "언더스코어사용자",
        username: String = "test_user_name_123",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createUserWithUuidExternalId(
        nickname: String = "UUID사용자",
        username: String = "uuid_user",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }

    fun createAdminUser(
        nickname: String = "관리자",
        username: String = "admin_user",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.ADMIN, UserRole.USER)
        )
    }

    fun createTestUser(
        nickname: String = "테스트계정",
        username: String = "test_account",
    ): User {
        return User.create(
            nickname = Nickname(nickname),
            username = Username(username),
            roles = listOf(UserRole.USER)
        )
    }
}