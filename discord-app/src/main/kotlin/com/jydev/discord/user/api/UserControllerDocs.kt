package com.jydev.discord.user.api

import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.user.api.dto.RegisterUserApiRequest
import com.jydev.discord.user.api.dto.RegisterUserApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
@Tag(name = "User", description = "사용자 관련 API")
interface UserControllerDocs {

    @Operation(
        summary = "Register User",
        description = """
            임시 가입 상태의 사용자를 정식 사용자로 등록합니다.
            
            **전제 조건:**
            - 카카오 로그인을 통해 임시 가입 상태여야 함 (authCredentialId 보유)
            - 아직 정식 계정이 생성되지 않은 상태여야 함
            
            **주의 사항:**
            - 사용자명은 전체 시스템에서 유일해야 함
            - 한 번 설정한 사용자명은 변경 가능하나 중복 검사를 거침
            
            **가능한 에러 코드:**
            - `E400000`: 이미 가입된 유저입니다
            - `E400001`: 이미 사용 중인 사용자명입니다
        """
    )
    @AuthenticatedApiResponses
    suspend fun registerUser(
        authUser: AuthUser,
        request: RegisterUserApiRequest
    ): RegisterUserApiResponse
}