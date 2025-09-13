package com.jydev.discord.security

import com.jydev.discord.config.properties.SecurityProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * JWT 인증을 처리하는 WebFilter
 * Authorization 헤더의 Bearer 토큰을 추출하여 인증 처리
 * publicUrls는 인증을 건너뜀
 */
@Component
class JwtAuthenticationWebFilter(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val authenticationConverter: BearerTokenAuthenticationConverter,
    private val securityProperties: SecurityProperties
) : WebFilter {

    // publicUrls를 제외한 모든 경로에 인증 시도
    private val requestMatcher: ServerWebExchangeMatcher by lazy {
        if (securityProperties.publicUrls.isEmpty()) {
            ServerWebExchangeMatchers.anyExchange()
        } else {
            val publicMatchers = securityProperties.publicUrls.map {
                ServerWebExchangeMatchers.pathMatchers(it)
            }
            NegatedServerWebExchangeMatcher(OrServerWebExchangeMatcher(publicMatchers))
        }
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return requestMatcher.matches(exchange)
            .filter { matchResult -> matchResult.isMatch }
            .flatMap { authenticationConverter.convert(exchange) }
            .switchIfEmpty(chain.filter(exchange).then(Mono.empty()))
            .flatMap { authentication -> authenticationManager.authenticate(authentication) }
            .flatMap { authentication ->
                onAuthenticationSuccess(authentication, WebFilterExchange(exchange, chain))
            }
            .then()
    }

    private fun onAuthenticationSuccess(
        authentication: Authentication,
        filterExchange: WebFilterExchange
    ): Mono<Void> {
        val exchange = filterExchange.exchange
        val securityContext = SecurityContextImpl(authentication)

        logger.debug { "인증 성공: ${authentication.name}" }

        return filterExchange.chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
    }
}