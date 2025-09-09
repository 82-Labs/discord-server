package com.jydev.discord.config

import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.jwt.JwtHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuthConfig {

    @Bean
    fun jwtHelper(
        @Value("\${app.jwt.secret-key}") secretKey: String,
        currentTime: CurrentTime
    ): JwtHelper {
        return JwtHelper(secretKey, currentTime)
    }
}