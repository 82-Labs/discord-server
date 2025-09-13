package com.jydev.discord.security

import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BearerTokenAuthenticationConverter {
    
    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
    
    fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(extractToken(exchange))
            .map { token -> BearerTokenAuthentication(token) }
    }
    
    private fun extractToken(exchange: ServerWebExchange): String? {
        val authorizationHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        
        return authorizationHeader?.let { header ->
            if (header.startsWith(BEARER_PREFIX, ignoreCase = true)) {
                val token = header.substring(BEARER_PREFIX.length).trim()
                // 빈 토큰은 null로 처리
                if (token.isEmpty()) null else token
            } else {
                null
            }
        }
    }
}