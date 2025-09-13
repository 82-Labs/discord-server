package com.jydev.discord.auth.api

import com.jydev.discord.auth.api.dto.KakaoAuthRequest
import com.jydev.discord.auth.api.dto.TokenResponse
import com.jydev.discord.auth.application.AuthenticateUserUseCase
import com.jydev.discord.auth.application.dto.AuthCommand
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticateUserUseCase: AuthenticateUserUseCase
) : AuthControllerDocs {

    @PostMapping("/kakao")
    override suspend fun authenticateWithKakao(
        @Valid @RequestBody request: KakaoAuthRequest
    ): TokenResponse {
        val authCommand = AuthCommand.Kakao(code = request.code)
        val tokenInfo = authenticateUserUseCase(authCommand)

        return TokenResponse(
            accessToken = tokenInfo.accessToken,
            refreshToken = tokenInfo.refreshToken
        )
    }
}