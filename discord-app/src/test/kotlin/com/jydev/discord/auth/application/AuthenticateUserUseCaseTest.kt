package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.common.transaction.Tx
import com.jydev.discord.common.transaction.TxAdvice
import com.jydev.discord.domain.auth.*
import com.jydev.discord.domain.auth.jwt.JwtHelper
import com.jydev.discord.domain.user.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.springframework.test.util.ReflectionTestUtils
import java.time.temporal.ChronoUnit

class AuthenticateUserUseCaseTest : DescribeSpec({

    describe("AuthenticateUserUseCase 실행") {
        lateinit var mockAuthProviderResolver: AuthProviderResolver
        lateinit var mockAuthCredentialRepository: AuthCredentialRepository
        lateinit var mockUserRepository: UserRepository
        lateinit var mockRefreshTokenRepository: RefreshTokenRepository
        lateinit var mockJwtHelper: JwtHelper
        lateinit var mockTxAdvice: TxAdvice
        lateinit var useCase: AuthenticateUserUseCase

        beforeEach {
            mockAuthProviderResolver = mockk()
            mockAuthCredentialRepository = mockk()
            mockUserRepository = mockk()
            mockRefreshTokenRepository = mockk()
            mockJwtHelper = mockk()
            mockTxAdvice = mockk()

            // Tx 초기화를 위한 설정
            val tx = Tx(mockTxAdvice)
            
            // TxAdvice가 실제로 블록을 실행하도록 설정
            coEvery { mockTxAdvice.write(any<suspend () -> Any>()) } coAnswers {
                val block = firstArg<suspend () -> Any>()
                block()
            }

            useCase = AuthenticateUserUseCase(
                authProviderResolvers = listOf(mockAuthProviderResolver),
                authCredentialRepository = mockAuthCredentialRepository,
                userRepository = mockUserRepository,
                refreshTokenRepository = mockRefreshTokenRepository,
                jwtHelper = mockJwtHelper
            )
        }

        afterEach {
            clearAllMocks()
        }

        context("기존 사용자가 인증할 때") {
            it("액세스 토큰과 리프레시 토큰을 포함한 TokenInfo를 반환한다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao123")
                )
                val authCredential = AuthCredential(
                    id = 1L,
                    userId = 100L,
                    authProvider = authProvider
                )
                val user = User.of(
                    userId = 100L,
                    nickname = Nickname("테스트유저"),
                    username = Username("testuser"),
                    roles = listOf(UserRole.USER)
                )
                val accessToken = "generated-access-token"

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns authCredential
                coEvery { mockUserRepository.findByUserId(100L) } returns user
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.User>(),
                        expiration = AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns accessToken

                // when
                val result = runBlocking { useCase(authRequest) }

                // then
                result.accessToken shouldBe accessToken
                result.refreshToken shouldNotBe null
                
                coVerify(exactly = 1) {
                    mockAuthProviderResolver.authenticate(authRequest)
                    mockAuthCredentialRepository.findByAuthProvider(authProvider)
                    mockUserRepository.findByUserId(100L)
                    mockRefreshTokenRepository.save(any())
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.User>(),
                        expiration = AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                }
            }

            it("사용자가 없으면 RuntimeException을 던진다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao123")
                )
                val authCredential = AuthCredential(
                    id = 1L,
                    userId = 100L,
                    authProvider = authProvider
                )

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns authCredential
                coEvery { mockUserRepository.findByUserId(100L) } returns null

                // when & then
                val exception = shouldThrow<RuntimeException> {
                    runBlocking { useCase(authRequest) }
                }
                exception.message shouldBe "User가 존재하지 않습니다. userId : 100"
            }
        }

        context("신규 사용자(임시 사용자)가 인증할 때") {
            it("리프레시 토큰 없이 임시 액세스 토큰만 반환한다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao456")
                )
                val temporalAccessToken = "temporal-access-token"

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns null
                coEvery { mockAuthCredentialRepository.save(any()) } answers {
                    val credential = firstArg<AuthCredential>()
                    AuthCredential(
                        id = 2L,
                        userId = credential.userId,
                        authProvider = credential.authProvider
                    )
                }
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.TemporalUser>(),
                        expiration = AuthenticateUserUseCase.TEMPORAL_ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns temporalAccessToken

                // when
                val result = runBlocking { useCase(authRequest) }

                // then
                result.accessToken shouldBe temporalAccessToken
                result.refreshToken shouldBe null
                
                coVerify(exactly = 1) {
                    mockAuthProviderResolver.authenticate(authRequest)
                    mockAuthCredentialRepository.findByAuthProvider(authProvider)
                    mockAuthCredentialRepository.save(any())
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.TemporalUser>(),
                        expiration = AuthenticateUserUseCase.TEMPORAL_ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                }
                
                coVerify(exactly = 0) {
                    mockRefreshTokenRepository.save(any())
                    mockUserRepository.findByUserId(any())
                }
            }
        }

        context("Provider 지원") {
            it("지원하지 않는 Provider인 경우 IllegalArgumentException을 던진다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                
                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns false

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking { useCase(authRequest) }
                }
                exception.message shouldBe "지원하는 Provider가 없습니다 : KAKAO"
            }

            it("여러 Provider가 있을 때 적절한 Provider를 찾아 사용한다") {
                // given
                val mockKakaoResolver = mockk<AuthProviderResolver>()
                val mockNaverResolver = mockk<AuthProviderResolver>()
                
                val useCaseWithMultipleResolvers = AuthenticateUserUseCase(
                    authProviderResolvers = listOf(mockNaverResolver, mockKakaoResolver),
                    authCredentialRepository = mockAuthCredentialRepository,
                    userRepository = mockUserRepository,
                    refreshTokenRepository = mockRefreshTokenRepository,
                    jwtHelper = mockJwtHelper
                )
                
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao789")
                )
                val authCredential = AuthCredential(
                    id = 3L,
                    userId = 300L,
                    authProvider = authProvider
                )
                val user = User.of(
                    userId = 300L,
                    nickname = Nickname("멀티유저"),
                    username = Username("multiuser"),
                    roles = listOf(UserRole.USER, UserRole.ADMIN)
                )
                val accessToken = "multi-access-token"

                every { mockNaverResolver.supports(ProviderType.KAKAO) } returns false
                every { mockKakaoResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockKakaoResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns authCredential
                coEvery { mockUserRepository.findByUserId(300L) } returns user
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.User>(),
                        expiration = AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns accessToken

                // when
                val result = runBlocking { useCaseWithMultipleResolvers(authRequest) }

                // then
                result.accessToken shouldBe accessToken
                result.refreshToken shouldNotBe null
                
                coVerify(exactly = 1) {
                    mockKakaoResolver.authenticate(authRequest)
                }
                
                coVerify(exactly = 0) {
                    mockNaverResolver.authenticate(any())
                }
            }
        }

        context("RefreshToken 생성") {
            it("기존 사용자 인증 시 RefreshToken이 올바르게 생성된다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao999")
                )
                val authCredential = AuthCredential(
                    id = 4L,
                    userId = 400L,
                    authProvider = authProvider
                )
                val user = User.of(
                    userId = 400L,
                    nickname = Nickname("리프레시유저"),
                    username = Username("refreshuser"),
                    roles = listOf(UserRole.USER)
                )
                val accessToken = "refresh-access-token"
                var savedRefreshToken: RefreshToken? = null

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns authCredential
                coEvery { mockUserRepository.findByUserId(400L) } returns user
                coEvery { mockRefreshTokenRepository.save(any()) } answers {
                    savedRefreshToken = firstArg()
                    firstArg()
                }
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.User>(),
                        expiration = AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                } returns accessToken

                // when
                val result = runBlocking { useCase(authRequest) }

                // then
                savedRefreshToken shouldNotBe null
                savedRefreshToken?.session?.userId shouldBe 400L
                result.refreshToken shouldBe savedRefreshToken?.token
                
                coVerify(exactly = 1) {
                    mockRefreshTokenRepository.save(match {
                        it.session.userId == 400L
                    })
                }
            }
        }

        context("AuthUser 타입별 토큰 만료 시간") {
            it("일반 사용자는 ACCESS_TOKEN_EXPIRATION 시간으로 토큰이 생성된다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao111")
                )
                val authCredential = AuthCredential(
                    id = 5L,
                    userId = 500L,
                    authProvider = authProvider
                )
                val user = User.of(
                    userId = 500L,
                    nickname = Nickname("일반유저"),
                    username = Username("normaluser"),
                    roles = listOf(UserRole.USER)
                )
                val accessToken = "normal-access-token"

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns authCredential
                coEvery { mockUserRepository.findByUserId(500L) } returns user
                coEvery { mockRefreshTokenRepository.save(any()) } returnsArgument 0
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any(),
                        expiration = any(),
                        unit = any()
                    )
                } returns accessToken

                // when
                runBlocking { useCase(authRequest) }

                // then
                coVerify(exactly = 1) {
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.User>(),
                        expiration = AuthenticateUserUseCase.ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                }
            }

            it("임시 사용자는 TEMPORAL_ACCESS_TOKEN_EXPIRATION 시간으로 토큰이 생성된다") {
                // given
                val authRequest = AuthRequest.Kakao(code = "test-code")
                val authProvider = AuthProvider(
                    type = ProviderType.KAKAO,
                    externalId = ExternalId("kakao222")
                )
                val temporalAccessToken = "temporal-token"

                every { mockAuthProviderResolver.supports(ProviderType.KAKAO) } returns true
                coEvery { mockAuthProviderResolver.authenticate(authRequest) } returns authProvider
                coEvery { mockAuthCredentialRepository.findByAuthProvider(authProvider) } returns null
                coEvery { mockAuthCredentialRepository.save(any()) } answers {
                    val credential = firstArg<AuthCredential>()
                    AuthCredential(
                        id = 6L,
                        userId = credential.userId,
                        authProvider = credential.authProvider
                    )
                }
                coEvery { 
                    mockJwtHelper.generateToken(
                        authUser = any(),
                        expiration = any(),
                        unit = any()
                    )
                } returns temporalAccessToken

                // when
                runBlocking { useCase(authRequest) }

                // then
                coVerify(exactly = 1) {
                    mockJwtHelper.generateToken(
                        authUser = any<AuthUser.TemporalUser>(),
                        expiration = AuthenticateUserUseCase.TEMPORAL_ACCESS_TOKEN_EXPIRATION,
                        unit = ChronoUnit.MINUTES
                    )
                }
            }
        }
    }
})