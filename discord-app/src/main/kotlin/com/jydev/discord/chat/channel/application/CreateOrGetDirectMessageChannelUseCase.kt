package com.jydev.discord.chat.channel.application

import com.jydev.discord.chat.channel.application.dto.DirectMessageChannelReadModel
import com.jydev.discord.common.id.IdGenerator
import com.jydev.discord.domain.chat.DirectMessageChannel
import com.jydev.discord.domain.chat.DirectMessageChannelRepository
import com.jydev.discord.domain.chat.UserDirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannelRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateOrGetDirectMessageChannelUseCase(
    private val directMessageChannelDao: DirectMessageChannelDao,
    private val directMessageChannelRepository: DirectMessageChannelRepository,
    private val userDirectMessageChannelRepository: UserDirectMessageChannelRepository,
    private val idGenerator: IdGenerator
) {
    
    @Transactional
    suspend operator fun invoke(requesterId: Long, targetUserIds: Set<Long>): DirectMessageChannelReadModel {
        // targetUserIds에는 요청자를 포함한 모든 참여자가 포함됨
        require(requesterId in targetUserIds) { "요청자 ID가 채널 참여자 목록에 포함되어야 합니다" }
        
        // 1. 기존 채널 조회 또는 새로 생성
        val channel = directMessageChannelDao.findByUserIds(targetUserIds) ?: run {
            val newChannel = DirectMessageChannel(
                id = idGenerator.generateId(),
                userIds = targetUserIds
            )
            directMessageChannelRepository.save(newChannel)
            DirectMessageChannelReadModel(
                channelId = newChannel.id,
                userIds = newChannel.userIds,
                hidden = false  // 새로 생성된 채널은 항상 보임 상태
            )
        }
        
        // 2. 요청자의 채널 가시성 설정
        ensureChannelVisibleForUser(requesterId, channel.channelId)
        
        return channel
    }
    
    private suspend fun ensureChannelVisibleForUser(userId: Long, channelId: Long) {
        val existingChannel = userDirectMessageChannelRepository.findByUserIdAndChannelId(userId, channelId)
        
        if (existingChannel == null) {
            // 없으면 생성
            userDirectMessageChannelRepository.save(
                UserDirectMessageChannel(userId = userId, channelId = channelId, isHidden = false)
            )
        } else if (existingChannel.isHidden) {
            // 숨김 상태면 표시로 변경
            userDirectMessageChannelRepository.save(existingChannel.show())
        }
    }
}