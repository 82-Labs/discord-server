package com.jydev.discord.auth.api

import com.jydev.discord.auth.api.dto.KakaoAuthRequest
import com.jydev.discord.auth.api.dto.TokenResponse
import com.jydev.discord.common.swagger.CommonErrorResponses
import io.swagger.v3.oas.annotations.Operation
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
        """
    )
    @CommonErrorResponses
    suspend fun authenticateWithKakao(
        request: KakaoAuthRequest
    ): TokenResponse
}