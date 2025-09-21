package com.jydev.discord.domain.user

import com.jydev.discord.domain.user.exception.UsernameDuplicateException

class User private constructor(
    val id: Long?,
    nickname: Nickname,
    username: Username,
    roles: List<UserRole>,
    status: UserStatus = UserStatus.NONE
) {
    var nickname: Nickname = nickname
        private set

    var username: Username = username
        private set

    var roles: List<UserRole> = roles
        private set
    
    var status: UserStatus = status
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
    
    fun updateStatus(newStatus: UserStatus) {
        this.status = newStatus
    }

    companion object {
        fun of(
            userId: Long?, 
            nickname: Nickname, 
            username: Username, 
            roles: List<UserRole>,
            status: UserStatus = UserStatus.NONE
        ): User {
            return User(
                id = userId,
                nickname = nickname,
                username = username,
                roles = roles,
                status = status
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