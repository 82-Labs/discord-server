package com.jydev.discord.auth.api

import com.jydev.discord.auth.application.AuthenticateUserUseCase
import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.auth.api.dto.KakaoAuthRequest
import com.jydev.discord.auth.api.dto.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "사용자 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticateUserUseCase: AuthenticateUserUseCase
) {
    
    @Operation(
        summary = "카카오 로그인",
        description = "카카오 OAuth 인증 코드를 사용하여 로그인합니다. 신규 사용자의 경우 임시 가입 처리되며, 이 경우 refreshToken이 null로 반환됩니다."
    )
    @PostMapping("/kakao")
    suspend fun authenticateWithKakao(
        @Valid @RequestBody request: KakaoAuthRequest
    ): ResponseEntity<TokenResponse> {
        val authRequest = AuthRequest.Kakao(code = request.code)
        val tokenInfo = authenticateUserUseCase(authRequest)
        
        return ResponseEntity.ok(
            TokenResponse(
                accessToken = tokenInfo.accessToken,
                refreshToken = tokenInfo.refreshToken
            )
        )
    }
}