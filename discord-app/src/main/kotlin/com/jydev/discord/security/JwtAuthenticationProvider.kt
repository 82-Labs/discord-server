package com.jydev.discord.security

import com.jydev.discord.domain.auth.jwt.JwtHelper
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationProvider(
    private val jwtHelper: JwtHelper
) : CustomAuthenticationProvider {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .filter(this::supports)
            .switchIfEmpty(Mono.error(IllegalArgumentException("유효하지 않은 Authentication")))
            .flatMap { auth -> Mono.justOrEmpty(auth.credentials as? String) }
            .flatMap { token -> Mono.just(jwtHelper.getAuthUser(token)) }
            .flatMap { authUser -> Mono.just(AuthUserAuthentication(authUser)) }
    }

    override fun supports(authentication: Authentication): Boolean {
        return authentication is BearerTokenAuthentication
    }
}