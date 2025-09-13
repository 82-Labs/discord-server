package com.jydev.discord.security

import com.jydev.discord.security.CustomAuthenticationProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.ProviderNotFoundException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@Component
class CustomReactiveAuthenticationManager(
    private val providers: List<CustomAuthenticationProvider>
) : ReactiveAuthenticationManager {
    
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        if (authentication.isAuthenticated) {
            return Mono.just(authentication)
        }
        
        return Flux.fromIterable(providers)
            .filter { provider -> provider.supports(authentication) }
            .next()
            .switchIfEmpty(
                Mono.error(
                    ProviderNotFoundException(
                        "${authentication.javaClass.simpleName}에 대한 인증 제공자를 찾을 수 없습니다"
                    )
                )
            )
            .flatMap { provider ->
                logger.debug { 
                    "${provider.javaClass.simpleName} 제공자로 인증 시도 중" 
                }
                provider.authenticate(authentication)
            }
            .doOnSuccess { result ->
                logger.debug { 
                    "인증 성공: ${result.name}" 
                }
            }
            .doOnError { error ->
                logger.warn { 
                    "인증 실패: ${error.message}" 
                }
            }
    }
}