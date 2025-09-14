package com.jydev.discord.user.api

import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationType
import com.jydev.discord.user.api.dto.*
import io.swagger.v3.oas.annotations.Operation
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
        """
    )
    @AuthenticatedApiResponses
    suspend fun registerUser(
        authUser: AuthUser,
        request: RegisterUserApiRequest
    ): RegisterUserApiResponse

    @Operation(
        summary = "Send Friend Request",
        description = """
            다른 사용자에게 친구 요청을 보냅니다.
            
            **가능한 에러:**
            - 수신자를 찾을 수 없습니다
            - 이미 친구 관계입니다
            - 차단된 사용자입니다
            - 이미 대기중인 요청이 있습니다
            
            **특별 처리:**
            - 상대방이 이미 나에게 요청을 보낸 경우 자동 수락
        """
    )
    @AuthenticatedApiResponses
    suspend fun requestUserRelation(
        authUser: AuthUser.User,
        request: RequestUserRelationApiRequest
    )

    @Operation(
        summary = "Handle Friend Request",
        description = """
            받은 친구 요청을 처리합니다.
            
            **액션 타입:**
            - ACCEPT: 요청 수락 (수신자만 가능)
            - REJECT: 요청 거절 (수신자만 가능)  
            - CANCEL: 요청 취소 (발신자만 가능)
            
            **가능한 에러:**
            - 요청을 찾을 수 없습니다
            - 해당 요청의 수신자가 아닙니다
            - 해당 요청의 발신자가 아닙니다
        """
    )
    @AuthenticatedApiResponses
    suspend fun handleUserRelationRequest(
        authUser: AuthUser.User,
        action: UserRelationRequestAction,
        request: HandleUserRelationRequestApiRequest
    )

    @Operation(
        summary = "Get Received Friend Requests",
        description = """
            현재 사용자가 받은 대기 중인 친구 요청 목록을 조회합니다.
        """
    )
    @AuthenticatedApiResponses
    suspend fun getReceivedRequests(
        authUser: AuthUser.User
    ): List<ReceivedRequestApiResponse>

    @Operation(
        summary = "Get User Relations",
        description = """
            사용자의 관계 목록을 타입별로 조회합니다.
            
            **관계 타입:**
            - FRIEND: 친구 목록
            - BLOCKED: 차단 목록
        """
    )
    @AuthenticatedApiResponses
    suspend fun getUserRelations(
        authUser: AuthUser.User,
        type: UserRelationType
    ): List<UserRelationApiResponse>
}