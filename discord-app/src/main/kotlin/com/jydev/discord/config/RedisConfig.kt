package com.jydev.discord.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.jydev.discord.auth.infra.persistence.RefreshTokenDocument
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    
    @Bean
    fun refreshTokenRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper
    ): ReactiveRedisTemplate<String, RefreshTokenDocument> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = Jackson2JsonRedisSerializer(objectMapper, RefreshTokenDocument::class.java)
        
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, RefreshTokenDocument>(keySerializer)
            .value(valueSerializer)
            .build()
        
        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}