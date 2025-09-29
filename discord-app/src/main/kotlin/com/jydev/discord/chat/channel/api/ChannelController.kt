package com.jydev.discord.chat.channel.api

import com.jydev.discord.chat.channel.api.dto.ChannelUserResponse
import com.jydev.discord.chat.channel.api.dto.CreateDirectMessageChannelRequest
import com.jydev.discord.chat.channel.api.dto.DirectMessageChannelResponse
import com.jydev.discord.chat.channel.api.dto.DirectMessageChannelsResponse
import com.jydev.discord.chat.channel.api.dto.UpdateChannelVisibilityRequest
import com.jydev.discord.chat.channel.application.CreateOrGetDirectMessageChannelUseCase
import com.jydev.discord.chat.channel.application.DirectMessageChannelDao
import com.jydev.discord.chat.channel.application.HideDirectMessageChannelUseCase
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.user.application.UserDao
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/channels")
class ChannelController(
    private val createOrGetDirectMessageChannelUseCase: CreateOrGetDirectMessageChannelUseCase,
    private val directMessageChannelDao: DirectMessageChannelDao,
    private val hideDirectMessageChannelUseCase: HideDirectMessageChannelUseCase,
    private val userDao: UserDao
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
        
        // 사용자 정보 조회
        val users = userDao.findByIdIn(channel.userIds)
        val userMap = users.associateBy { it.id }
        
        val channelUsers = channel.userIds.mapNotNull { userId ->
            userMap[userId]?.let { user ->
                ChannelUserResponse(
                    userId = user.id,
                    username = user.username.value,
                    nickname = user.nickname.value,
                    status = user.status
                )
            }
        }
        
        return DirectMessageChannelResponse(
            channelId = channel.channelId,
            users = channelUsers,
            hidden = false  // 새로 생성/조회된 채널은 항상 표시 상태
        )
    }
    
    @GetMapping("/direct-messages")
    override suspend fun getDirectMessageChannels(
        @CurrentUser user: AuthUser.User
    ): DirectMessageChannelsResponse {
        val channels = directMessageChannelDao.findAllChannelsByUserId(user.userId)
        
        // 모든 채널의 사용자 ID 수집
        val allUserIds = channels.flatMap { it.userIds }.toSet()
        
        // 사용자 정보 한 번에 조회
        val users = userDao.findByIdIn(allUserIds)
        val userMap = users.associateBy { it.id }
        
        val items = channels.map { channel ->
            val channelUsers = channel.userIds.mapNotNull { userId ->
                userMap[userId]?.let { user ->
                    ChannelUserResponse(
                        userId = user.id,
                        username = user.username.value,
                        nickname = user.nickname.value,
                        status = user.status
                    )
                }
            }
            
            DirectMessageChannelResponse(
                channelId = channel.channelId,
                users = channelUsers,
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