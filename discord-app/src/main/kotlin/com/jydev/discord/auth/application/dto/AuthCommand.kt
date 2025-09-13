package com.jydev.discord.auth.application.dto

import com.jydev.discord.domain.auth.ProviderType

sealed class AuthCommand(
    val provider: ProviderType
) {

    data class Kakao(val code: String) : AuthCommand(ProviderType.KAKAO)
}