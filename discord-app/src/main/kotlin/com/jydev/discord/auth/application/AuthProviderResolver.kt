package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthCommand
import com.jydev.discord.domain.auth.AuthProvider
import com.jydev.discord.domain.auth.ProviderType

interface AuthProviderResolver {
    suspend fun authenticate(request: AuthCommand): AuthProvider
    fun supports(provider: ProviderType): Boolean
}