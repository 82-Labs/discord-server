package com.jydev.discord.domain.user

import com.jydev.discord.domain.user.exception.UsernameDuplicateException

class User private constructor(
    val id: Long?,
    nickname: Nickname,
    username: Username,
    roles: List<UserRole>,
) {
    var nickname: Nickname = nickname
        private set

    var username: Username = username
        private set

    var roles: List<UserRole> = roles
        private set

    fun updateNickname(newNickname: Nickname) {
        this.nickname = newNickname
    }

    suspend fun updateUsername(newUsername: Username, checkDuplicate: suspend (Username) -> Boolean) {
        if (checkDuplicate(newUsername)) {
            throw UsernameDuplicateException()
        }
        this.username = newUsername
    }

    fun updateRoles(newRoles: List<UserRole>) {
        this.roles = newRoles
    }

    companion object {
        fun of(userId: Long?, nickname: Nickname, username: Username, roles: List<UserRole>): User {
            return User(
                id = userId,
                nickname = nickname,
                username = username,
                roles = roles
            )
        }

        suspend fun create(username : Username, roles: List<UserRole>, checkDuplicate: suspend (Username) -> Boolean): User {

            if (checkDuplicate(username)) {
                throw UsernameDuplicateException()
            }

            return User(
                id = null,
                nickname = Nickname(username.value),
                username = username,
                roles = roles
            )
        }
    }
}