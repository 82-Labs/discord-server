package com.jydev.discord.chat.api

import com.jydev.discord.chat.api.dto.CreateDirectMessageChannelRequest
import com.jydev.discord.chat.api.dto.DirectMessageChannelResponse
import com.jydev.discord.chat.api.dto.DirectMessageChannelsResponse
import com.jydev.discord.chat.api.dto.UpdateChannelVisibilityRequest
import com.jydev.discord.common.swagger.AuthenticatedApiResponses
import com.jydev.discord.domain.auth.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@Tag(name = "Channel", description = "채널 관련 API")
interface ChannelControllerDocs {

    @Operation(
        summary = "Create or Get Direct Message Channel",
        description = """
            다이렉트 메시지 채널을 생성하거나 기존 채널을 조회합니다.
            
            **동작 방식:**
            - 요청한 사용자 목록에 대한 DM 채널이 이미 존재하는 경우 기존 채널을 반환
            - 존재하지 않는 경우 새로운 DM 채널을 생성하여 반환
            - 요청 사용자는 자동으로 채널 참가자에 포함됨
            
            **전제 조건:**
            - 최소 1명 이상의 다른 사용자와 채널을 생성해야 함
            - 요청한 사용자 ID들이 모두 유효한 사용자여야 함
            
            **가능한 에러:**
            - 사용자를 찾을 수 없습니다
            - 최소 2명의 사용자가 필요합니다
            - 유효하지 않은 사용자 ID입니다
            
            **특별 처리:**
            - 동일한 사용자 그룹에 대해서는 항상 동일한 채널을 반환
            - 사용자 순서와 관계없이 동일한 채널을 반환 (userIds는 Set으로 처리)
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "DM 채널 생성 또는 조회 성공",
                content = [Content(schema = Schema(implementation = DirectMessageChannelResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun createOrGetDirectMessageChannel(
        request: CreateDirectMessageChannelRequest,
        user: AuthUser.User
    ): DirectMessageChannelResponse
    
    @Operation(
        summary = "Get Direct Message Channels",
        description = """
            현재 사용자의 다이렉트 메시지 채널 목록을 조회합니다.
            
            **동작 방식:**
            - 사용자가 참여중인 모든 DM 채널 목록을 반환
            - 사용자가 숨김 처리한 채널은 제외됨
            - 채널 ID와 참여자 목록 정보를 포함하여 반환
            
            **필터링:**
            - isHidden이 false인 채널만 조회
            - 사용자가 명시적으로 숨기지 않은 채널만 표시
            
            **정렬:**
            - 채널 ID 순으로 정렬되어 반환
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "DM 채널 목록 조회 성공",
                content = [Content(schema = Schema(implementation = DirectMessageChannelsResponse::class))]
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun getDirectMessageChannels(
        user: AuthUser.User
    ): DirectMessageChannelsResponse
    
    @Operation(
        summary = "Update Direct Message Channel Visibility",
        description = """
            다이렉트 메시지 채널의 가시성을 업데이트합니다.
            
            **동작 방식:**
            - 사용자별로 DM 채널을 숨기거나 표시할 수 있음
            - 숨김 처리된 채널은 채널 목록 조회 시 제외됨
            - 다른 사용자에게는 영향을 주지 않음
            
            **요청 본문:**
            - `hide: true` - 채널을 숨김 처리
            - `hide: false` - 채널을 다시 표시
            
            **특별 처리:**
            - 처음 설정하는 경우 UserDirectMessageChannel 엔티티가 자동 생성됨
            - 이미 설정이 있는 경우 기존 설정을 업데이트
            
            **가능한 에러:**
            - 유효하지 않은 채널 ID입니다
            - 해당 채널에 참여하지 않은 사용자입니다
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "채널 가시성 업데이트 성공"
            )
        ]
    )
    @AuthenticatedApiResponses
    suspend fun updateDirectMessageChannelVisibility(
        channelId: Long,
        request: UpdateChannelVisibilityRequest,
        user: AuthUser.User
    )
}