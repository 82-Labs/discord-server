package com.jydev.discord.user.api

import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationType
import com.jydev.discord.user.api.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "User Relation", description = "사용자 관계(친구, 차단 등) 관련 API")
interface UserRelationControllerDocs {

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
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "친구 요청 전송 성공"
            )
        ]
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
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "친구 요청 처리 성공"
            )
        ]
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
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "받은 친구 요청 목록 조회 성공",
                content = [Content(schema = Schema(implementation = ReceivedRequestsApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getReceivedRequests(
        authUser: AuthUser.User
    ): ReceivedRequestsApiResponse

    @Operation(
        summary = "Get Sent Friend Requests",
        description = """
            현재 사용자가 보낸 대기 중인 친구 요청 목록을 조회합니다.
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "보낸 친구 요청 목록 조회 성공",
                content = [Content(schema = Schema(implementation = SentRequestsApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getSentRequests(
        authUser: AuthUser.User
    ): SentRequestsApiResponse
    
    @Operation(
        summary = "Get Friends List",
        description = """
            현재 사용자의 친구 목록을 조회합니다.
            
            **반환 정보:**
            - 친구의 ID, 사용자명, 닉네임
            - 친구의 현재 상태 (온라인/오프라인 등)
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "친구 목록 조회 성공",
                content = [Content(schema = Schema(implementation = UserRelationsApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getFriends(
        authUser: AuthUser.User
    ): UserRelationsApiResponse

    @Operation(
        summary = "Get User Relations",
        description = """
            사용자의 관계 목록을 타입별로 조회합니다.
            
            **관계 타입:**
            - FRIEND: 친구 목록
            - BLOCKED: 차단 목록
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 관계 목록 조회 성공",
                content = [Content(schema = Schema(implementation = UserRelationsApiResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getUserRelations(
        authUser: AuthUser.User,
        type: UserRelationType
    ): UserRelationsApiResponse

    @Operation(
        summary = "Delete User Relation",
        description = """
            사용자 관계(친구)를 삭제합니다.
            
            **가능한 에러:**
            - 사용자를 찾을 수 없습니다
            - 관계가 존재하지 않습니다
            - 친구 관계가 아닙니다
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 관계 삭제 성공"
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun deleteUserRelation(
        authUser: AuthUser.User,
        targetUsername: String
    )
}