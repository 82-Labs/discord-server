package com.jydev.discord.chat.channel.infra.persistence

import com.jydev.discord.chat.application.DirectMessageChannelDao
import com.jydev.discord.chat.application.dto.DirectMessageChannelReadModel
import com.jydev.discord.domain.chat.DirectMessageChannel
import com.jydev.discord.domain.chat.DirectMessageChannelRepository
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class DirectMessageChannelRepositoryAdapter(
    private val mongoRepository: ReactiveMongoDirectMessageChannelRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
    private val userChannelRepository: R2dbcUserDirectMessageChannelRepository
) : DirectMessageChannelRepository, DirectMessageChannelDao {
    
    override suspend fun save(channel: DirectMessageChannel): DirectMessageChannel {
        val document = channel.toDocument()
        mongoRepository.save(document).awaitSingle()
        return channel
    }
    
    override suspend fun deleteById(channelId: Long) {
        mongoRepository.deleteById(channelId).awaitSingleOrNull()
    }
    
    override suspend fun findByUserIds(userIds: Set<Long>): DirectMessageChannelReadModel? {
        val query = Query()
        query.addCriteria(
            Criteria().andOperator(
                Criteria.where("userIds").all(userIds),
                Criteria.where("userIds").size(userIds.size)
            )
        )
        
        return mongoTemplate.findOne(query, DirectMessageChannelDocument::class.java)
            .awaitSingleOrNull()
            ?.toReadModel()
    }
    
    override suspend fun findVisibleChannelsByUserId(userId: Long): List<DirectMessageChannelReadModel> {
        // R2DBC에서 사용자의 보이는 채널 ID 목록 조회
        val visibleUserChannels = userChannelRepository.findVisibleByUserId(userId)
        
        if (visibleUserChannels.isEmpty()) {
            return emptyList()
        }
        
        val channelIds = visibleUserChannels.map { it.channelId }
        
        // MongoDB에서 채널 정보 조회
        val channels = mongoRepository.findAllById(channelIds)
            .collectList()
            .awaitSingleOrNull()
            ?: emptyList()
        
        return channels.map { it.toReadModel() }
    }
}