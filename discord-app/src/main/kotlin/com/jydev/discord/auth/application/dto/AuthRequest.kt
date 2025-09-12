package com.jydev.discord.auth.application.dto

import com.jydev.discord.domain.auth.ProviderType

sealed class AuthRequest(
    val provider: ProviderType
) {

    data class Kakao(val code: String) : AuthRequest(ProviderType.KAKAO)
}