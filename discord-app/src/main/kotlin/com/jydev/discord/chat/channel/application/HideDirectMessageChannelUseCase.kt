package com.jydev.discord.chat.application

import com.jydev.discord.domain.chat.UserDirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannelRepository
import org.springframework.stereotype.Service

@Service
class HideDirectMessageChannelUseCase(
    private val userDirectMessageChannelRepository: UserDirectMessageChannelRepository
) {
    
    suspend operator fun invoke(userId: Long, channelId: Long, hide: Boolean) {
        val userChannel = userDirectMessageChannelRepository.findByUserIdAndChannelId(userId, channelId)
            ?: UserDirectMessageChannel(
                userId = userId,
                channelId = channelId,
                isHidden = false
            )
        
        val updatedChannel = if (hide) {
            userChannel.hide()
        } else {
            userChannel.show()
        }
        
        userDirectMessageChannelRepository.save(updatedChannel)
    }
}