package com.jydev.discord.chat.channel.infra.persistence

import com.jydev.discord.domain.chat.UserDirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannelRepository
import org.springframework.stereotype.Repository

@Repository
class UserDirectMessageChannelRepositoryAdapter(
    private val r2dbcRepository: R2dbcUserDirectMessageChannelRepository
) : UserDirectMessageChannelRepository {
    
    override suspend fun findByUserIdAndChannelId(userId: Long, channelId: Long): UserDirectMessageChannel? {
        return r2dbcRepository.findByUserIdAndChannelId(userId, channelId)?.toDomain()
    }
    
    override suspend fun save(userChannel: UserDirectMessageChannel): UserDirectMessageChannel {
        val entity = userChannel.toEntity()
        val savedEntity = r2dbcRepository.save(entity)
        return savedEntity.toDomain()
    }
}