package com.jydev.discord.auth.kakao.applicaiton

import com.jydev.discord.auth.application.AuthProviderResolver
import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.auth.kakao.infra.external.KakaoAuthClient
import com.jydev.discord.domain.auth.AuthProvider
import com.jydev.discord.domain.auth.ExternalId
import com.jydev.discord.domain.auth.ProviderType
import org.springframework.stereotype.Component

@Component
class KakaoAuthProviderResolver(
    private val kakaoAuthClient: KakaoAuthClient
) : AuthProviderResolver {
    override suspend fun authenticate(request: AuthRequest): AuthProvider {
        if (request !is AuthRequest.Kakao) {
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