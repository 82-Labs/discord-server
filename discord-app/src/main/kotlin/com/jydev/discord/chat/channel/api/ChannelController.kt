package com.jydev.discord.chat.channel.api

import com.jydev.discord.chat.channel.api.dto.CreateDirectMessageChannelRequest
import com.jydev.discord.chat.channel.api.dto.DirectMessageChannelResponse
import com.jydev.discord.chat.channel.api.dto.DirectMessageChannelsResponse
import com.jydev.discord.chat.channel.api.dto.UpdateChannelVisibilityRequest
import com.jydev.discord.chat.channel.application.CreateOrGetDirectMessageChannelUseCase
import com.jydev.discord.chat.channel.application.DirectMessageChannelDao
import com.jydev.discord.chat.channel.application.HideDirectMessageChannelUseCase
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.domain.auth.AuthUser
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/channels")
class ChannelController(
    private val createOrGetDirectMessageChannelUseCase: CreateOrGetDirectMessageChannelUseCase,
    private val directMessageChannelDao: DirectMessageChannelDao,
    private val hideDirectMessageChannelUseCase: HideDirectMessageChannelUseCase
) : ChannelControllerDocs {
    
    @PostMapping("/direct-messages")
    override suspend fun createOrGetDirectMessageChannel(
        @Valid @RequestBody request: CreateDirectMessageChannelRequest,
        @CurrentUser user: AuthUser.User
    ): DirectMessageChannelResponse {
        val targetUserIds = request.userIds + user.userId
        val channel = createOrGetDirectMessageChannelUseCase(
            requesterId = user.userId,
            targetUserIds = targetUserIds
        )
        
        return DirectMessageChannelResponse(
            channelId = channel.channelId,
            userIds = channel.userIds,
            hidden = false  // 새로 생성/조회된 채널은 항상 표시 상태
        )
    }
    
    @GetMapping("/direct-messages")
    override suspend fun getDirectMessageChannels(
        @CurrentUser user: AuthUser.User
    ): DirectMessageChannelsResponse {
        val channels = directMessageChannelDao.findAllChannelsByUserId(user.userId)
        
        val items = channels.map { channel ->
            DirectMessageChannelResponse(
                channelId = channel.channelId,
                userIds = channel.userIds,
                hidden = channel.hidden
            )
        }
        
        return DirectMessageChannelsResponse(content = items)
    }
    
    @PatchMapping("/direct-messages/{channelId}/visibility")
    override suspend fun updateDirectMessageChannelVisibility(
        @PathVariable channelId: Long,
        @Valid @RequestBody request: UpdateChannelVisibilityRequest,
        @CurrentUser user: AuthUser.User
    ) {
        hideDirectMessageChannelUseCase(
            userId = user.userId,
            channelId = channelId,
            hide = request.hide
        )
    }
}