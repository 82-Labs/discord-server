package com.jydev.discord.user.api

import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.user.api.dto.RegisterUserApiRequest
import com.jydev.discord.user.api.dto.RegisterUserApiResponse
import com.jydev.discord.user.application.RegisterUserUseCase
import com.jydev.discord.user.application.dto.RegisterUserCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase
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
}