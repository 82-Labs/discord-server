package com.jydev.discord.security

import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

/**
 * 커스텀 인증 제공자 인터페이스
 * 다양한 인증 방식을 지원하기 위한 확장 가능한 구조
 */
interface CustomAuthenticationProvider {
    
    /**
     * 실제 인증 로직을 수행
     * @param authentication 인증되지 않은 인증 객체
     * @return 인증된 인증 객체
     */
    fun authenticate(authentication: Authentication): Mono<Authentication>
    
    /**
     * 해당 제공자가 주어진 인증 객체를 처리할 수 있는지 확인
     * @param authentication 검증할 인증 객체
     * @return 처리 가능 여부
     */
    fun supports(authentication: Authentication): Boolean
}