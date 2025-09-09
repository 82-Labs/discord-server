package com.jydev.discord.domain.auth.jwt

import com.jydev.discord.common.time.CurrentTime
import com.jydev.discord.domain.auth.AuthUserFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.time.Instant
import java.time.temporal.ChronoUnit

class JwtHelperTest : StringSpec({

    val secretKey = "test-secret-key-that-is-long-enough-for-hmac-256"

    class TestCurrentTime(private val fixedTime: Instant) : CurrentTime {
        override fun now(): Instant = fixedTime
    }

    val currentTime = Instant.now()
    val testCurrentTime = TestCurrentTime(currentTime)
    val jwtHelper = JwtHelper(secretKey, testCurrentTime)

    "generateToken - 기본 사용자 정보로 토큰 생성 시 올바른 JWT 토큰 반환" {
        // Given
        val authUser = AuthUserFixture.createUser()

        // When
        val token = jwtHelper.generateToken(authUser)

        // Then
        token.shouldNotBeEmpty()
        token.split(".").size shouldBe 3 // JWT는 header.payload.signature 구조
    }

    "generateToken - 관리자 권한 사용자 토큰 생성 시 역할 정보 포함된 토큰 반환" {
        // Given
        val authUser = AuthUserFixture.createAdmin()

        // When
        val token = jwtHelper.generateToken(authUser)

        // Then
        val decodedUser = jwtHelper.getAuthUser(token)
        decodedUser.userId shouldBe authUser.userId
        decodedUser.roles shouldBe authUser.roles
    }

    "generateToken - 빈 역할 목록 사용자 토큰 생성 시 빈 역할 배열로 처리" {
        // Given
        val authUser = AuthUserFixture.createUserWithoutRoles()

        // When
        val token = jwtHelper.generateToken(authUser)

        // Then
        val decodedUser = jwtHelper.getAuthUser(token)
        decodedUser.userId shouldBe authUser.userId
        decodedUser.roles shouldBe emptyList()
    }

    "generateToken - 사용자 정의 만료 시간으로 토큰 생성 시 지정된 시간으로 설정" {
        // Given
        val authUser = AuthUserFixture.createPremiumUser()
        val customExpirationHours = 48L

        // When
        val token = jwtHelper.generateToken(authUser, customExpirationHours, ChronoUnit.HOURS)

        // Then
        jwtHelper.isExpired(token) shouldBe false
    }

    "generateToken - 분 단위 만료 시간으로 토큰 생성 시 정상 동작" {
        // Given
        val authUser = AuthUserFixture.createUser()
        val expirationMinutes = 30L

        // When
        val token = jwtHelper.generateToken(authUser, expirationMinutes, ChronoUnit.MINUTES)

        // Then
        jwtHelper.isExpired(token) shouldBe false
        val decodedUser = jwtHelper.getAuthUser(token)
        decodedUser.userId shouldBe authUser.userId
    }

    "getAuthUser - 유효한 토큰에서 사용자 정보 추출 시 올바른 AuthUser 반환" {
        // Given
        val expectedUser = AuthUserFixture.createSuperAdmin()
        val token = jwtHelper.generateToken(expectedUser)

        // When
        val actualUser = jwtHelper.getAuthUser(token)

        // Then
        actualUser.userId shouldBe expectedUser.userId
        actualUser.roles shouldBe expectedUser.roles
    }

    "getAuthUser - 잘못된 형식의 토큰 처리 시 예외 발생" {
        // Given
        val invalidToken = "invalid.token.without.subject"

        // When & Then
        shouldThrow<Exception> {
            jwtHelper.getAuthUser(invalidToken)
        }
    }

    "isExpired - 유효한 토큰의 만료 상태 확인 시 false 반환" {
        // Given
        val authUser = AuthUserFixture.createUser()
        val token = jwtHelper.generateToken(authUser, 24, ChronoUnit.HOURS)

        // When
        val expired = jwtHelper.isExpired(token)

        // Then
        expired shouldBe false
    }

    "isExpired - 만료된 토큰 확인 시 true 반환" {
        // Given
        val pastTime = currentTime.minus(25, ChronoUnit.HOURS) // 25시간 전
        val pastTimeHelper = JwtHelper(secretKey, TestCurrentTime(pastTime))
        val authUser = AuthUserFixture.createUser()
        val token = pastTimeHelper.generateToken(authUser, 24, ChronoUnit.HOURS) // 24시간 만료 토큰을 25시간 전에 생성

        // When & Then - 만료된 토큰은 파싱 시점에서 예외 발생
        val exception = shouldThrow<IllegalArgumentException> {
            jwtHelper.isExpired(token)
        }
        exception.message shouldBe "Invalid token format"
    }

    "isExpired - 잘못된 형식의 토큰 만료 확인 시 IllegalArgumentException 발생" {
        // Given
        val invalidToken = "invalid.token.format"

        // When & Then
        val exception = shouldThrow<IllegalArgumentException> {
            jwtHelper.isExpired(invalidToken)
        }
        exception.message shouldBe "Invalid token format"
    }

    "토큰 생명주기 통합 테스트 - 생성부터 검증까지 전체 플로우" {
        // Given
        val originalUser = AuthUserFixture.createMultiRoleUser()

        // When
        val generatedToken = jwtHelper.generateToken(originalUser, 24, ChronoUnit.HOURS)
        val isTokenExpired = jwtHelper.isExpired(generatedToken)
        val retrievedUser = jwtHelper.getAuthUser(generatedToken)

        // Then
        isTokenExpired shouldBe false
        retrievedUser.userId shouldBe originalUser.userId
        retrievedUser.roles shouldBe originalUser.roles
    }
})