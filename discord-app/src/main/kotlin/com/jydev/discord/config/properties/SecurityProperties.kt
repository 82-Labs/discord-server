package com.jydev.discord.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.security")
class SecurityProperties {
    var corsAllowedOrigins: List<String> = emptyList()
    var publicUrls: List<String> = emptyList()
}