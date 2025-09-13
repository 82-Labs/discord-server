package com.jydev.discord.user.application

import com.jydev.discord.auth.application.AuthenticateUserUseCase
import com.jydev.discord.domain.auth.AuthCredentialRepository
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.auth.RefreshToken
import com.jydev.discord.domain.auth.RefreshTokenRepository
import com.jydev.discord.domain.auth.jwt.JwtHelper
import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
import com.jydev.discord.user.application.dto.RegisterUserCommand
import com.jydev.discord.user.application.dto.RegisterUserResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val authCredentialRepository: AuthCredentialRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtHelper: JwtHelper,
) {

    @Transactional
    suspend operator fun invoke(request: RegisterUserCommand): RegisterUserResult {

        val authCredential = authCredentialRepository.findById(request.authCredentialId)
            ?: throw RuntimeException("인증 정보가 존재하지 않습니다. ${request.authCredentialId}")

        if(authCredential.isNotTemporal()) {
            throw IllegalStateException("이미 계정이 생성 되어있습니다. ${authCredential.userId}")
        }

        val user = User.create(
            username = Username(request.username),
            roles = listOf(UserRole.USER),
            checkDuplicate = userRepository::existsByUsername
        ).let {
            userRepository.save(it)
        }

        authCredential.initialUser(userId = user.id!!)
        authCredentialRepository.save(authCredential)

        val refreshToken = RefreshToken.create(
            userId = user.id!!,
            expirationDays = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION
        ).let {
            refreshTokenRepository.save(it)
        }

        val authUser = AuthUser.User(
            userId = user.id!!,
            sessionId = refreshToken.session.sessionId,
            roles = user.roles
        )

        val accessToken = jwtHelper.generateToken(authUser, AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION)

        return RegisterUserResult(
            accessToken = accessToken,
            refreshToken = refreshToken.token
        )
    }
}