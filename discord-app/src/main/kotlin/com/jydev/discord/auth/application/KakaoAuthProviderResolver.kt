package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthCommand
import com.jydev.discord.auth.infra.external.KakaoAuthClient
import com.jydev.discord.domain.auth.AuthProvider
import com.jydev.discord.domain.auth.ExternalId
import com.jydev.discord.domain.auth.ProviderType
import org.springframework.stereotype.Component

@Component
class KakaoAuthProviderResolver(
    private val kakaoAuthClient: KakaoAuthClient
) : AuthProviderResolver {
    override suspend fun authenticate(request: AuthCommand): AuthProvider {
        if (request !is AuthCommand.Kakao) {
            throw IllegalArgumentException("지원하지 않는 타입 : $request")
        }

        val result = kakaoAuthClient.authenticate(request.code)
        return AuthProvider(
            type = ProviderType.KAKAO,
            externalId = ExternalId(result.userId.toString())
        )
    }

    override fun supports(provider: ProviderType): Boolean {
        return provider == ProviderType.KAKAO
    }
}