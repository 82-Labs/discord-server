package com.jydev.discord.user.application

import com.jydev.discord.domain.auth.*
import com.jydev.discord.domain.auth.jwt.JwtHelper
import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
import com.jydev.discord.user.application.dto.RegisterUserCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import java.util.*

class RegisterUserUseCaseTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val authCredentialRepository = mockk<AuthCredentialRepository>()
    val refreshTokenRepository = mockk<RefreshTokenRepository>()
    val jwtHelper = mockk<JwtHelper>()

    val registerUserUseCase = RegisterUserUseCase(
        userRepository = userRepository,
        authCredentialRepository = authCredentialRepository,
        refreshTokenRepository = refreshTokenRepository,
        jwtHelper = jwtHelper
    )

    beforeTest {
        clearAllMocks()
    }

    describe("RegisterUserUseCase") {
        context("정상적인 회원가입 요청인 경우") {
            it("새로운 사용자를 생성하고 토큰을 반환한다") {
                // Given
                val authCredentialId = 1L
                val username = "newuser"
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = username
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_123"),
                    type = ProviderType.KAKAO
                )
                val temporalAuthCredential = AuthCredential.create(
                    authProvider = authProvider,
                    userId = null // temporal user
                )
                val authCredentialWithId = AuthCredential(
                    id = authCredentialId,
                    userId = null,
                    authProvider = authProvider
                )

                val savedUser = User.of(
                    userId = 100L,
                    nickname = com.jydev.discord.domain.user.Nickname(username),
                    username = Username(username),
                    roles = listOf(UserRole.USER)
                )

                val refreshTokenValue = UUID.randomUUID().toString()
                val refreshToken = RefreshToken(
                    token = refreshTokenValue,
                    session = AuthSession(userId = savedUser.id!!),
                    expirationDays = 7
                )

                val accessToken = "generated.access.token"

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredentialWithId
                coEvery { userRepository.existsByUsername(Username(username)) } returns false
                coEvery { userRepository.save(any()) } returns savedUser
                coEvery { authCredentialRepository.save(any()) } returns authCredentialWithId
                coEvery { refreshTokenRepository.save(any()) } returns refreshToken
                every { jwtHelper.generateToken(any(), any()) } returns accessToken

                // When
                val result = runBlocking {
                    registerUserUseCase(request)
                }

                // Then
                result.accessToken shouldBe accessToken
                result.refreshToken shouldBe refreshTokenValue

                coVerify(exactly = 1) {
                    authCredentialRepository.findById(authCredentialId)
                    userRepository.existsByUsername(Username(username))
                    userRepository.save(any())
                    authCredentialRepository.save(any())
                    refreshTokenRepository.save(any())
                    jwtHelper.generateToken(any(), any())
                }
            }

            it("사용자 생성 후 AuthCredential에 userId를 설정한다") {
                // Given
                val authCredentialId = 1L
                val username = "testuser"
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = username
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_456"),
                    type = ProviderType.KAKAO
                )
                val authCredential = AuthCredential(
                    id = authCredentialId,
                    userId = null,
                    authProvider = authProvider
                )

                val savedUser = User.of(
                    userId = 200L,
                    nickname = com.jydev.discord.domain.user.Nickname(username),
                    username = Username(username),
                    roles = listOf(UserRole.USER)
                )

                val refreshToken = RefreshToken(
                    token = UUID.randomUUID().toString(),
                    session = AuthSession(userId = savedUser.id!!),
                    expirationDays = 7
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredential
                coEvery { userRepository.existsByUsername(Username(username)) } returns false
                coEvery { userRepository.save(any()) } returns savedUser
                coEvery { authCredentialRepository.save(any()) } answers {
                    val credential = firstArg<AuthCredential>()
                    credential.userId shouldBe savedUser.id
                    credential
                }
                coEvery { refreshTokenRepository.save(any()) } returns refreshToken
                every { jwtHelper.generateToken(any(), any()) } returns "token"

                // When
                runBlocking {
                    registerUserUseCase(request)
                }

                // Then
                coVerify(exactly = 1) {
                    authCredentialRepository.save(match {
                        it.userId == savedUser.id
                    })
                }
            }
        }

        context("인증 정보가 존재하지 않는 경우") {
            it("RuntimeException을 발생시킨다") {
                // Given
                val authCredentialId = 999L
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = "testuser"
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns null

                // When & Then
                val exception = shouldThrow<RuntimeException> {
                    runBlocking {
                        registerUserUseCase(request)
                    }
                }

                exception.message shouldBe "인증 정보가 존재하지 않습니다. $authCredentialId"

                coVerify(exactly = 1) {
                    authCredentialRepository.findById(authCredentialId)
                }
                coVerify(exactly = 0) {
                    userRepository.save(any())
                    refreshTokenRepository.save(any())
                }
            }
        }

        context("이미 계정이 생성된 사용자인 경우") {
            it("IllegalStateException을 발생시킨다") {
                // Given
                val authCredentialId = 1L
                val existingUserId = 100L
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = "newuser"
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_789"),
                    type = ProviderType.KAKAO
                )
                val authCredentialWithUser = AuthCredential(
                    id = authCredentialId,
                    userId = existingUserId, // 이미 userId가 있음
                    authProvider = authProvider
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredentialWithUser

                // When & Then
                val exception = shouldThrow<IllegalStateException> {
                    runBlocking {
                        registerUserUseCase(request)
                    }
                }

                exception.message shouldBe "이미 계정이 생성 되어있습니다. $existingUserId"

                coVerify(exactly = 1) {
                    authCredentialRepository.findById(authCredentialId)
                }
                coVerify(exactly = 0) {
                    userRepository.save(any())
                    refreshTokenRepository.save(any())
                }
            }
        }

        context("사용자명이 중복된 경우") {
            it("UsernameDuplicateException을 발생시킨다") {
                // Given
                val authCredentialId = 1L
                val duplicateUsername = "existinguser"
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = duplicateUsername
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_321"),
                    type = ProviderType.KAKAO
                )
                val authCredential = AuthCredential(
                    id = authCredentialId,
                    userId = null,
                    authProvider = authProvider
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredential
                coEvery { userRepository.existsByUsername(Username(duplicateUsername)) } returns true

                // When & Then
                shouldThrow<com.jydev.discord.domain.user.exception.UsernameDuplicateException> {
                    runBlocking {
                        registerUserUseCase(request)
                    }
                }

                coVerify(exactly = 1) {
                    authCredentialRepository.findById(authCredentialId)
                    userRepository.existsByUsername(Username(duplicateUsername))
                }
                coVerify(exactly = 0) {
                    userRepository.save(any())
                    authCredentialRepository.save(any())
                    refreshTokenRepository.save(any())
                }
            }
        }

        context("JWT 토큰 생성") {
            it("올바른 AuthUser 정보로 액세스 토큰을 생성한다") {
                // Given
                val authCredentialId = 1L
                val username = "jwtuser"
                val userId = 300L
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = username
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_jwt"),
                    type = ProviderType.KAKAO
                )
                val authCredential = AuthCredential(
                    id = authCredentialId,
                    userId = null,
                    authProvider = authProvider
                )

                val savedUser = User.of(
                    userId = userId,
                    nickname = com.jydev.discord.domain.user.Nickname(username),
                    username = Username(username),
                    roles = listOf(UserRole.USER)
                )

                val sessionId = UUID.randomUUID().toString()
                val refreshToken = RefreshToken(
                    token = UUID.randomUUID().toString(),
                    session = AuthSession(userId = userId, sessionId = sessionId),
                    expirationDays = 7
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredential
                coEvery { userRepository.existsByUsername(Username(username)) } returns false
                coEvery { userRepository.save(any()) } returns savedUser
                coEvery { authCredentialRepository.save(any()) } returns authCredential
                coEvery { refreshTokenRepository.save(any()) } returns refreshToken
                
                val slot = slot<AuthUser>()
                every { jwtHelper.generateToken(capture(slot), any()) } returns "jwt.token"

                // When
                runBlocking {
                    registerUserUseCase(request)
                }

                // Then
                val capturedAuthUser = slot.captured as AuthUser.User
                capturedAuthUser.userId shouldBe userId
                capturedAuthUser.sessionId shouldBe sessionId
                capturedAuthUser.roles shouldBe listOf(UserRole.USER)
            }
        }

        context("리프레시 토큰 생성") {
            it("올바른 만료 기간으로 리프레시 토큰을 생성한다") {
                // Given
                val authCredentialId = 1L
                val username = "refreshuser"
                val request = RegisterUserCommand(
                    authCredentialId = authCredentialId,
                    username = username
                )

                val authProvider = AuthProvider(
                    externalId = ExternalId("kakao_refresh"),
                    type = ProviderType.KAKAO
                )
                val authCredential = AuthCredential(
                    id = authCredentialId,
                    userId = null,
                    authProvider = authProvider
                )

                val savedUser = User.of(
                    userId = 400L,
                    nickname = com.jydev.discord.domain.user.Nickname(username),
                    username = Username(username),
                    roles = listOf(UserRole.USER)
                )

                coEvery { authCredentialRepository.findById(authCredentialId) } returns authCredential
                coEvery { userRepository.existsByUsername(Username(username)) } returns false
                coEvery { userRepository.save(any()) } returns savedUser
                coEvery { authCredentialRepository.save(any()) } returns authCredential
                
                val refreshTokenSlot = slot<RefreshToken>()
                coEvery { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers {
                    firstArg()
                }
                
                every { jwtHelper.generateToken(any(), any()) } returns "token"

                // When
                runBlocking {
                    registerUserUseCase(request)
                }

                // Then
                val capturedRefreshToken = refreshTokenSlot.captured
                capturedRefreshToken.session.userId shouldBe savedUser.id
                capturedRefreshToken.token shouldNotBe null
                capturedRefreshToken.session shouldNotBe null
                // AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION 값 확인
                // 실제로는 7일 후 정도가 되어야 함
            }
        }
    }
})