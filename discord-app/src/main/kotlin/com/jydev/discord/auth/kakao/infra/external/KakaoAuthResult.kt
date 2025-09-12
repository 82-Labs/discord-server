package com.jydev.discord.auth.kakao.infra.external

data class KakaoAuthResult(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)