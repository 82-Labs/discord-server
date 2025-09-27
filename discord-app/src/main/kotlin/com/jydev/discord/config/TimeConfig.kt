package com.jydev.discord.config

import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.common.time.SystemCurrentTime
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TimeConfig {
    
    @Bean
    fun currentTime(): CurrentTime {
        return SystemCurrentTime()
    }
}