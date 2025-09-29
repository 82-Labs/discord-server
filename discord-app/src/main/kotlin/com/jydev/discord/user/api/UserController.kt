package com.jydev.discord.user.api

import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.user.api.dto.*
import com.jydev.discord.user.application.*
import com.jydev.discord.user.application.dto.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*


private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val userDao: UserDao
) : UserControllerDocs {

    @PostMapping("/register")
    override suspend fun registerUser(
        @CurrentUser authUser: AuthUser,
        @Valid @RequestBody request: RegisterUserApiRequest
    ): RegisterUserApiResponse {

        return when (authUser) {
            is AuthUser.User -> {
                logger.warn { "이미 가입된 유저 : $authUser" }
                throw IllegalArgumentException("이미 가입된 유저입니다. ${authUser.id}")
            }

            is AuthUser.TemporalUser -> {
                val command = RegisterUserCommand(
                    authCredentialId = authUser.authCredentialId,
                    username = request.username
                )

                val result = registerUserUseCase(command)

                RegisterUserApiResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken
                )
            }
        }
    }
    
    @GetMapping("/me")
    override suspend fun getMe(
        @CurrentUser authUser: AuthUser.User
    ): UserApiResponse {
        val user = userDao.findById(authUser.userId) 
            ?: throw IllegalStateException("사용자를 찾을 수 없습니다: ${authUser.userId}")
        
        return UserApiResponse(
            id = user.id,
            username = user.username.value,
            nickname = user.nickname.value,
            status = user.status
        )
    }
}