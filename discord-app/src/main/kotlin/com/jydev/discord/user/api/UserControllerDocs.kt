package com.jydev.discord.user.api

import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.user.api.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "User", description = "사용자 관련 API")
interface UserControllerDocs {

    @Operation(
        summary = "Register User",
        description = """
            임시 가입 상태의 사용자를 정식 사용자로 등록합니다.
            
            **전제 조건:**
            - 카카오 로그인을 통해 임시 가입 상태여야 함
            - 아직 정식 계정이 생성되지 않은 상태여야 함
            
            **가능한 에러:**
            - 이미 계정이 생성 되어있습니다
            - 인증 정보가 존재하지 않습니다
            - 이미 사용 중인 사용자명입니다
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 등록 성공",
                content = [Content(schema = Schema(implementation = RegisterUserApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun registerUser(
        authUser: AuthUser,
        request: RegisterUserApiRequest
    ): RegisterUserApiResponse
    
    @Operation(
        summary = "Get Current User",
        description = """
            현재 로그인한 사용자의 정보를 조회합니다.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 정보 조회 성공",
                content = [Content(schema = Schema(implementation = UserApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getMe(
        authUser: AuthUser.User
    ): UserApiResponse
}