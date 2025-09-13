package com.jydev.discord.domain.user

import kotlinx.coroutines.runBlocking

object UserFixture {

    fun createKakaoUser(
        nickname: String = "테스트사용자",
        username: String = "testuser",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            // 닉네임을 지정된 값으로 업데이트
            if (user.nickname.value != nickname) {
                user.updateNickname(Nickname(nickname))
            }
            user
        }
    }

    fun createUserWithLongNickname(
        nickname: String = "A".repeat(30),
        username: String = "longnickname_user",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createUserWithShortNickname(
        nickname: String = "짧음",
        username: String = "short_user",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createUserWithNumberUsername(
        nickname: String = "숫자사용자",
        username: String = "123456789",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createUserWithUnderscoreUsername(
        nickname: String = "언더스코어사용자",
        username: String = "test_user_name_123",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createUserWithUuidExternalId(
        nickname: String = "UUID사용자",
        username: String = "uuid_user",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createAdminUser(
        nickname: String = "관리자",
        username: String = "admin_user",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.ADMIN, UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }

    fun createTestUser(
        nickname: String = "테스트계정",
        username: String = "test_account",
    ): User {
        return runBlocking {
            val user = User.create(
                username = Username(username),
                roles = listOf(UserRole.USER),
                checkDuplicate = { false }
            )
            user.updateNickname(Nickname(nickname))
            user
        }
    }
}