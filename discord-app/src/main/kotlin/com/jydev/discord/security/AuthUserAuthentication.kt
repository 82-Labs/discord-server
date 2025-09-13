package com.jydev.discord.security

import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.user.UserRole
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * 인증된 사용자를 나타내는 인증 객체
 * JWT 토큰 검증 후 사용
 */
class AuthUserAuthentication(
    private val authUser: AuthUser
) : Authentication {

    private var authenticated: Boolean = true
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authUser.roles.map { role ->
            SimpleGrantedAuthority("ROLE_${role.name}")
        }
    }
    
    override fun getCredentials(): String = authUser.id.toString()
    
    override fun getDetails(): Any? = null
    
    override fun getPrincipal(): AuthUser = authUser
    
    override fun isAuthenticated(): Boolean = true
    
    override fun setAuthenticated(isAuthenticated: Boolean) {
        this.authenticated = isAuthenticated
    }
    
    override fun getName(): String {
        return when (authUser) {
            is AuthUser.User -> "사용자:${authUser.userId}"
            is AuthUser.TemporalUser -> "임시사용자:${authUser.authCredentialId}"
        }
    }
}