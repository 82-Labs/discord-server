package com.jydev.discord.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

/**
 * 인증되지 않은 Bearer 토큰을 나타내는 인증 객체
 * JWT 토큰 검증 전 사용
 */
class BearerTokenAuthentication(
    private val token: String
) : Authentication {
    
    private var authenticated: Boolean = false
    
    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    
    override fun getCredentials(): String = token
    
    override fun getDetails(): Any? = null
    
    override fun getPrincipal(): String = token
    
    override fun isAuthenticated(): Boolean = authenticated
    
    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.authenticated = isAuthenticated
    }
    
    override fun getName(): String = "Bearer 토큰"
}