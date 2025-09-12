package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.auth.application.dto.TokenInfo
import com.jydev.discord.common.transaction.Tx
import com.jydev.discord.domain.auth.*
import com.jydev.discord.domain.auth.jwt.JwtHelper
import com.jydev.discord.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

@Service
class AuthenticateUserUseCase(
    private val authProviderResolvers: List<AuthProviderResolver>,
    private val authCredentialRepository: AuthCredentialRepository,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtHelper: JwtHelper
) {
    companion object {
        const val REFRESH_TOKEN_EXPIRATION = 14L
        const val TEMPORAL_ACCESS_TOKEN_EXPIRATION = 60L
        const val ACCESS_TOKEN_EXPIRATION = 15L
    }

    suspend operator fun invoke(authRequest: AuthRequest): TokenInfo {
        val resolver = authProviderResolvers.find { it.supports(authRequest.provider) }
            ?: throw IllegalArgumentException("지원하는 Provider가 없습니다 : ${authRequest.provider}")

        val authProvider = resolver.authenticate(authRequest)

        return Tx.write {
            val authCredential = authCredentialRepository.findByAuthProvider(authProvider)
                ?: authProvider.createNewAuthCredential()

            val (authUser, refreshToken) = if (authCredential.isNotTemporal()) {
                val user = userRepository.findByUserId(authCredential.userId!!)
                    ?: throw RuntimeException("User가 존재하지 않습니다. userId : ${authCredential.userId}")

                val refreshToken = RefreshToken.create(
                    userId = user.id!!,
                    expirationDays = REFRESH_TOKEN_EXPIRATION
                ).also {
                    refreshTokenRepository.save(it)
                }

                val authUser = AuthUser.User(
                    userId = user.id!!,
                    roles = user.roles,
                    sessionId = refreshToken.session.sessionId
                )

                authUser to refreshToken
            } else {
                AuthUser.TemporalUser(authCredential.id!!) to null
            }

            val accessToken = jwtHelper.generateToken(
                authUser = authUser,
                expiration = authUser.getExpiration(),
                unit = ChronoUnit.MINUTES
            )

            TokenInfo(accessToken = accessToken, refreshToken = refreshToken?.token)
        }
    }

    private fun AuthUser.getExpiration(): Long =
        when (this) {
            is AuthUser.TemporalUser -> TEMPORAL_ACCESS_TOKEN_EXPIRATION
            is AuthUser.User -> ACCESS_TOKEN_EXPIRATION
        }

    private suspend fun AuthProvider.createNewAuthCredential(): AuthCredential {
        val credential = AuthCredential.create(authProvider = this)
        return authCredentialRepository.save(credential)
    }
}