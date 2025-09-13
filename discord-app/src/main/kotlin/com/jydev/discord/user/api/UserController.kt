package com.jydev.discord.user.api

import com.jydev.discord.common.web.ErrorResponse
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.user.api.dto.RegisterUserApiRequest
import com.jydev.discord.user.api.dto.RegisterUserApiResponse
import com.jydev.discord.user.application.RegisterUserUseCase
import com.jydev.discord.user.application.dto.RegisterUserCommand
import io.github.oshai.kotlinlogging.KotlinLogging
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


private val logger = KotlinLogging.logger {}

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase
) {

    @Operation(
        summary = "사용자 등록",
        description = """
            임시 가입 상태의 사용자를 정식 사용자로 등록합니다.
            
            **전제 조건:**
            - 카카오 로그인을 통해 임시 가입 상태여야 함 (authCredentialId 보유)
            - 아직 정식 계정이 생성되지 않은 상태여야 함
            
            **주의 사항:**
            - 사용자명은 전체 시스템에서 유일해야 함
            - 한 번 설정한 사용자명은 변경 가능하나 중복 검사를 거침
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "사용자 등록 성공",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = RegisterUserApiResponse::class)
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
    @PostMapping("/register")
    suspend fun registerUser(
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