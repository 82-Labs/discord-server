package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.RefreshTokenCommand
import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.*
import com.jydev.discord.domain.auth.exception.TokenExpiredException
import com.jydev.discord.domain.auth.jwt.JwtHelper
import com.jydev.discord.domain.user.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class RefreshTokenUseCaseTest : DescribeSpec({

    describe("RefreshTokenUseCase 실행") {
        lateinit var mockRefreshTokenRepository: RefreshTokenRepository
        lateinit var mockJwtHelper: JwtHelper
        lateinit var mockCurrentTime: CurrentTime
        lateinit var useCase: RefreshTokenUseCase

        beforeEach {
            mockRefreshTokenRepository = mockk()
            mockJwtHelper = mockk()
            mockCurrentTime = mockk()

            useCase = RefreshTokenUseCase(
                refreshTokenRepository = mockRefreshTokenRepository,
                jwtHelper = mockJwtHelper,
                currentTime = mockCurrentTime
            )
        }

        afterEach {
            clearAllMocks()
        }

        context("정상적인 토큰 갱신") {
            it("유효한 리프레시 토큰으로 새로운 액세스 토큰과 리프레시 토큰을 발급한다") {
                // given
                val userId = 100L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "old-access-token",
                    refreshToken = "valid-refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = userId
                )
                
                val refreshToken = RefreshToken.of(
                    token = "valid-refresh-token",
                    session = session,
                    expiredAt = Instant.now().plus(7, ChronoUnit.DAYS)
                )
                
                val newAccessToken = "new-access-token"
                val currentDateTime = Instant.now()

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns refreshToken
                coEvery { mockCurrentTime.now() } returns currentDateTime
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery {
                    mockJwtHelper.generateToken(
                        authUser = authUser,
                        expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns newAccessToken

                // when
                val result = runBlocking { useCase(command) }

                // then
                result shouldNotBe null
                result.accessToken shouldBe newAccessToken
                result.refreshToken shouldBe refreshToken.token

                coVerify(exactly = 1) {
                    mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken)
                    mockRefreshTokenRepository.findByUserId(userId)
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(
                        authUser = authUser,
                        expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                }
            }

            it("만료되지 않은 리프레시 토큰은 정상적으로 처리된다") {
                // given
                val userId = 200L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER, UserRole.ADMIN)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = userId
                )
                
                val currentDateTime = Instant.now()
                val refreshToken = RefreshToken.of(
                    token = "refresh-token",
                    session = session,
                    expiredAt = currentDateTime.plus(1, ChronoUnit.HOURS) // 아직 만료되지 않음
                )
                
                val newAccessToken = "new-access-token"

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns refreshToken
                coEvery { mockCurrentTime.now() } returns currentDateTime
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery {
                    mockJwtHelper.generateToken(
                        authUser = authUser,
                        expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns newAccessToken

                // when
                val result = runBlocking { useCase(command) }

                // then
                result.accessToken shouldBe newAccessToken
                result.refreshToken shouldBe refreshToken.token
            }
        }

        context("임시 사용자 처리") {
            it("임시 사용자는 토큰 갱신을 할 수 없다") {
                // given
                val command = RefreshTokenCommand(
                    accessToken = "temporal-access-token",
                    refreshToken = "some-refresh-token"
                )
                
                val temporalUser = AuthUser.TemporalUser(
                    authCredentialId = 123L
                )

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns temporalUser

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking { useCase(command) }
                }
                exception.message shouldBe "임시유저는 토큰 갱신을 할 수 없습니다."

                coVerify(exactly = 1) {
                    mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken)
                }
                
                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.findByUserId(any())
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(any(), any(), any())
                }
            }
        }

        context("토큰 만료 처리") {
            it("만료된 리프레시 토큰으로 갱신 시도하면 TokenExpiredException을 던진다") {
                // given
                val userId = 300L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "expired-refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = userId
                )
                
                // 만료된 토큰: 1시간 전에 만료됨
                val expiredAt = Instant.now().minus(1, ChronoUnit.HOURS)
                val expiredRefreshToken = RefreshToken.of(
                    token = "expired-refresh-token",
                    session = session,
                    expiredAt = expiredAt
                )
                
                // CurrentTime을 현재 시간으로 설정 (토큰보다 늦은 시간)
                val currentDateTime = Instant.now()

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns expiredRefreshToken
                coEvery { mockCurrentTime.now() } returns currentDateTime

                // when & then
                shouldThrow<TokenExpiredException> {
                    runBlocking { useCase(command) }
                }

                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(any(), any(), any())
                }
            }

            it("리프레시 토큰이 존재하지 않으면 TokenExpiredException을 던진다") {
                // given
                val userId = 400L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "non-existent-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER)
                )

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns null

                // when & then
                shouldThrow<TokenExpiredException> {
                    runBlocking { useCase(command) }
                }

                coVerify(exactly = 1) {
                    mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken)
                    mockRefreshTokenRepository.findByUserId(userId)
                }
                
                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(any(), any(), any())
                }
            }
        }

        context("보안 검증 - 세션 불일치") {
            it("세션 ID가 불일치하면 토큰을 삭제하고 TokenExpiredException을 던진다") {
                // given
                val userId = 500L
                val authSessionId = UUID.randomUUID().toString()
                val tokenSessionId = UUID.randomUUID().toString() // 다른 세션 ID
                
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = authSessionId,
                    roles = listOf(UserRole.USER)
                )
                
                val session = AuthSession(
                    sessionId = tokenSessionId, // 다른 세션
                    userId = userId
                )
                
                val refreshToken = RefreshToken.of(
                    token = "refresh-token",
                    session = session,
                    expiredAt = Instant.now().plus(7, ChronoUnit.DAYS)
                )

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns refreshToken
                coEvery { mockRefreshTokenRepository.deleteByUserId(userId) } just Runs
                coEvery { mockCurrentTime.now() } returns Instant.now()

                // when & then
                shouldThrow<TokenExpiredException> {
                    runBlocking { useCase(command) }
                }

                coVerify(exactly = 1) {
                    mockRefreshTokenRepository.deleteByUserId(userId)
                }
                
                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(any(), any(), any())
                }
            }
        }

        context("보안 검증 - 사용자 불일치") {
            it("사용자 ID가 불일치하면 두 사용자의 토큰을 모두 삭제하고 TokenExpiredException을 던진다") {
                // given
                val authUserId = 600L
                val tokenUserId = 700L // 다른 사용자 ID
                val sessionId = UUID.randomUUID().toString()
                
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = authUserId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = tokenUserId // 다른 사용자
                )
                
                val refreshToken = RefreshToken.of(
                    token = "refresh-token",
                    session = session,
                    expiredAt = Instant.now().plus(7, ChronoUnit.DAYS)
                )

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(authUserId) } returns refreshToken
                coEvery { mockRefreshTokenRepository.deleteByUserId(any()) } just Runs
                coEvery { mockCurrentTime.now() } returns Instant.now()

                // when & then
                shouldThrow<TokenExpiredException> {
                    runBlocking { useCase(command) }
                }

                coVerify(exactly = 1) {
                    mockRefreshTokenRepository.deleteByUserId(authUserId)
                    mockRefreshTokenRepository.deleteByUserId(tokenUserId)
                }
                
                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(any(), any(), any())
                }
            }
        }

        context("리프레시 토큰 갱신") {
            it("토큰 갱신 시 refresh 메서드가 호출되고 새로운 만료 시간이 설정된다") {
                // given
                val userId = 800L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "access-token",
                    refreshToken = "refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = userId
                )
                
                val currentDateTime = Instant.now()
                val refreshToken = spyk(RefreshToken.of(
                    token = "refresh-token",
                    session = session,
                    expiredAt = currentDateTime.plus(1, ChronoUnit.DAYS)
                ))
                
                val newAccessToken = "new-access-token"

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns refreshToken
                coEvery { mockCurrentTime.now() } returns currentDateTime
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery {
                    mockJwtHelper.generateToken(
                        authUser = authUser,
                        expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns newAccessToken

                // when
                runBlocking { useCase(command) }

                // then
                verify(exactly = 1) {
                    refreshToken.refresh(AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION)
                }
                
                coVerify(exactly = 1) {
                    mockRefreshTokenRepository.save(refreshToken)
                }
            }
        }

        context("다양한 역할(Role)을 가진 사용자") {
            it("ADMIN 역할을 가진 사용자도 정상적으로 토큰을 갱신할 수 있다") {
                // given
                val userId = 900L
                val sessionId = UUID.randomUUID().toString()
                val command = RefreshTokenCommand(
                    accessToken = "admin-access-token",
                    refreshToken = "admin-refresh-token"
                )
                
                val authUser = AuthUser.User(
                    userId = userId,
                    sessionId = sessionId,
                    roles = listOf(UserRole.USER, UserRole.ADMIN)
                )
                
                val session = AuthSession(
                    sessionId = sessionId,
                    userId = userId
                )
                
                val refreshToken = RefreshToken.of(
                    token = "admin-refresh-token",
                    session = session,
                    expiredAt = Instant.now().plus(7, ChronoUnit.DAYS)
                )
                
                val newAccessToken = "new-admin-access-token"
                val currentDateTime = Instant.now()

                coEvery { mockJwtHelper.getAuthUserIgnoringExpiration(command.accessToken) } returns authUser
                coEvery { mockRefreshTokenRepository.findByUserId(userId) } returns refreshToken
                coEvery { mockCurrentTime.now() } returns currentDateTime
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery {
                    mockJwtHelper.generateToken(
                        authUser = authUser,
                        expiration = AuthenticateUserUseCase.REFRESH_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns newAccessToken

                // when
                val result = runBlocking { useCase(command) }

                // then
                result.accessToken shouldBe newAccessToken
                result.refreshToken shouldBe refreshToken.token
            }
        }
    }
})