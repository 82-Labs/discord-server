package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.domain.auth.AuthProvider
import com.jydev.discord.domain.auth.ProviderType

interface AuthProviderResolver {
    suspend fun authenticate(request: AuthRequest): AuthProvider
    fun supports(provider: ProviderType): Boolean
}