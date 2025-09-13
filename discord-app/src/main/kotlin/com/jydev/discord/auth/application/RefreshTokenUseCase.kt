package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.RefreshTokenCommand
import com.jydev.discord.auth.application.dto.RefreshTokenResult
import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.auth.RefreshToken
import com.jydev.discord.domain.auth.RefreshTokenRepository
import com.jydev.discord.domain.auth.exception.TokenExpiredException
import com.jydev.discord.domain.auth.jwt.JwtHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class RefreshTokenUseCase(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtHelper: JwtHelper,
    private val currentTime: CurrentTime
) {

    suspend operator fun invoke(command : RefreshTokenCommand) : RefreshTokenResult {
        when(val authUser = jwtHelper.getAuthUserIgnoringExpiration(command.accessToken)) {
            is AuthUser.TemporalUser -> {
                throw IllegalArgumentException("임시유저는 토큰 갱신을 할 수 없습니다.")
            }
            is AuthUser.User -> {
                val refreshToken = refreshTokenRepository.findByUserId(authUser.userId)
                    ?.takeIf { hasSecurityIssue(authUser, it).not() && it.isExpired(currentTime).not()}
                    ?.also { it.refresh(AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION) }
                    ?.also { refreshTokenRepository.save(it) }
                    ?: throw TokenExpiredException()

                val accessToken = jwtHelper.generateToken(
                    authUser = authUser,
                    expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                    unit = ChronoUnit.MINUTES
                )

                return RefreshTokenResult(
                    accessToken = accessToken,
                    refreshToken = refreshToken.token
                )
            }
        }
    }

    private suspend fun hasSecurityIssue(authUser: AuthUser.User, token: RefreshToken): Boolean {
        val isSessionMismatch = authUser.sessionId != token.session.sessionId
        val isUserMismatch = authUser.userId != token.session.userId
        
        when {
            isUserMismatch -> {
                // 사용자 불일치: 두 사용자의 토큰 모두 삭제 (보안 위협)
                logger.info { "토큰 보안 문제 발생 (사용자 불일치) : ${authUser.userId}, ${token.session.userId}" }
                refreshTokenRepository.deleteByUserId(authUser.userId)
                refreshTokenRepository.deleteByUserId(token.session.userId)
                return true
            }
            isSessionMismatch -> {
                // 세션만 불일치: 현재 사용자의 토큰만 삭제
                logger.info { "토큰 보안 문제 발생 (세션 불일치) : ${authUser.sessionId}, ${token.session.sessionId}" }
                refreshTokenRepository.deleteByUserId(authUser.userId)
                return true
            }
            else -> return false
        }
    }
}