package com.jydev.discord.auth.api

import com.jydev.discord.auth.api.dto.KakaoAuthRequest
import com.jydev.discord.auth.api.dto.TokenResponse
import com.jydev.discord.auth.application.AuthenticateUserUseCase
import com.jydev.discord.auth.application.dto.AuthCommand
import com.jydev.discord.common.web.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticateUserUseCase: AuthenticateUserUseCase
) {

    @Operation(
        summary = "Kakao Login",
        description = "카카오 OAuth 인증 코드를 사용하여 로그인합니다. 신규 사용자의 경우 임시 가입 처리되며, 이 경우 refreshToken이 null로 반환됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "카카오 로그인 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = TokenResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효성 검사 실패)",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "500",
                description = "서버 내부 오류",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            )
        ]
    )
    @PostMapping("/kakao")
    suspend fun authenticateWithKakao(
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