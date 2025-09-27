package com.jydev.discord.config

import com.jydev.discord.common.id.IdGenerator
import com.jydev.discord.common.id.SnowflakeIdGenerator
import com.jydev.discord.common.time.CurrentTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IdGeneratorConfig {
    
    @Bean
    fun idGenerator(
        @Value("\${snowflake.machine-id:1}") machineId: Long,
        currentTime: CurrentTime
    ): IdGenerator {
        return SnowflakeIdGenerator(
            machineId = machineId,
            currentTime = currentTime
        )
    }
}