package com.jydev.discord.auth.kakao.infra.external

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "kakao.auth")
class KakaoAuthProperties {
    lateinit var clientId: String
    lateinit var clientSecret: String
    lateinit var redirectUri: String
}