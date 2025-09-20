package com.jydev.discord.auth.api

import com.jydev.discord.auth.api.dto.KakaoAuthRequest
import com.jydev.discord.auth.api.dto.RefreshTokenRequest
import com.jydev.discord.auth.api.dto.RefreshTokenResponse
import com.jydev.discord.auth.api.dto.TokenResponse
import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.common.swagger.CommonErrorResponses
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Auth", description = "사용자 인증 관련 API")
interface AuthControllerDocs {

    @Operation(
        summary = "Kakao Login",
        description = """
            카카오 OAuth 인증 코드를 사용하여 로그인합니다.
            
            **처리 프로세스:**
            - 카카오 인증 코드 검증
            - 카카오 사용자 정보 조회
            - 기존 사용자: 정식 토큰 발급 (accessToken + refreshToken)
            - 신규 사용자: 임시 가입 처리 (accessToken만 발급, refreshToken은 null)
            
            **응답 분기:**
            - 정식 사용자: 두 토큰 모두 포함된 응답
            - 임시 가입 사용자: refreshToken이 null인 응답 (추가 회원가입 필요)
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "인증 성공",
                content = [Content(schema = Schema(implementation = TokenResponse::class))]
            )
        ]
    )
    @CommonErrorResponses
    suspend fun authenticateWithKakao(
        request: KakaoAuthRequest
    ): TokenResponse

    @Operation(
        summary = "Refresh Token",
        description = """
            리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.
            
            **처리 프로세스:**
            - 액세스 토큰에서 사용자 정보 추출 (만료 무시)
            - 리프레시 토큰 유효성 검증
            - 보안 검증 (세션/사용자 일치 여부)
            - 새로운 액세스 토큰 및 리프레시 토큰 발급
            
            **에러 케이스:**
            - 임시 사용자는 토큰 갱신 불가
            - 리프레시 토큰 만료 시 재로그인 필요
            - 세션/사용자 불일치 시 보안 위협으로 간주하여 토큰 삭제
            
            **보안 정책:**
            - 세션 불일치: 현재 사용자의 토큰 삭제
            - 사용자 불일치: 관련된 모든 사용자의 토큰 삭제
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 갱신 성공",
                content = [Content(schema = Schema(implementation = RefreshTokenResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun refreshToken(
        request: RefreshTokenRequest
    ): RefreshTokenResponse
}