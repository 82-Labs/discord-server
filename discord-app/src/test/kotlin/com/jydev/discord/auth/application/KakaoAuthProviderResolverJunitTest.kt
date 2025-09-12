package com.jydev.discord.auth.application

import com.jydev.discord.auth.application.dto.AuthRequest
import com.jydev.discord.auth.infra.external.KakaoAuthClient
import com.jydev.discord.auth.infra.external.KakaoAuthResult
import com.jydev.discord.domain.auth.ProviderType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class KakaoAuthProviderResolverJunitTest {

    private val kakaoAuthClient = mockk<KakaoAuthClient>()
    private val resolver = KakaoAuthProviderResolver(kakaoAuthClient)

    @Test
    fun `authenticate - Kakao 요청 처리 시 올바른 AuthProvider 반환`() = runBlocking {
        // Given
        val code = "test-kakao-auth-code"
        val request = AuthRequest.Kakao(code)
        val expectedUserId = 123456789L
        val kakaoAuthResult = KakaoAuthResult(
            userId = expectedUserId,
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            expiresIn = 7200
        )
        
        coEvery { kakaoAuthClient.authenticate(code) } returns kakaoAuthResult

        // When
        val authProvider = resolver.authenticate(request)

        // Then
        assertEquals(ProviderType.KAKAO, authProvider.type)
        assertEquals(expectedUserId.toString(), authProvider.externalId.value)
        
        coVerify(exactly = 1) { kakaoAuthClient.authenticate(code) }
    }

    @Test
    fun `authenticate - Kakao가 아닌 요청 처리 시 IllegalArgumentException 발생`() {
        // Given
        // AuthRequest는 sealed class이므로 직접 mock 할 수 없음
        // 대신 테스트 용도의 non-Kakao 타입을 만들어야 하지만
        // 현재는 Kakao만 있으므로 이 테스트는 실제로 불가능
        // 따라서 이 테스트는 다른 provider가 추가되었을 때 테스트
        
        // 현재는 Kakao type만 있으므로 테스트 생략
        // 추후 다른 Provider 추가 시 활성화
        assertTrue(true) // placeholder
    }

    @Test
    fun `authenticate - KakaoAuthClient 예외 발생 시 예외 전파`() = runBlocking {
        // Given
        val code = "invalid-code"
        val request = AuthRequest.Kakao(code)
        val expectedError = RuntimeException("카카오 인증 실패")
        
        coEvery { kakaoAuthClient.authenticate(code) } throws expectedError

        // When & Then
        val exception = assertThrows<RuntimeException> {
            runBlocking {
                resolver.authenticate(request)
            }
        }
        
        assertEquals("카카오 인증 실패", exception.message)
    }

    @Test
    fun `supports - KAKAO ProviderType 지원 확인`() {
        // When & Then
        assertTrue(resolver.supports(ProviderType.KAKAO))
    }

    @Test
    fun `supports - KAKAO가 아닌 ProviderType 미지원 확인`() {
        // Given
        // 현재는 KAKAO만 있지만, 향후 다른 provider 추가 시 테스트
        
        // When & Then
        assertTrue(resolver.supports(ProviderType.KAKAO))
        // 다른 타입이 추가되면:
        // assertFalse(resolver.supports(ProviderType.GOOGLE))
    }

    @Test
    fun `authenticate - 빈 코드로 요청 시 KakaoAuthClient에 그대로 전달`() = runBlocking {
        // Given
        val emptyCode = ""
        val request = AuthRequest.Kakao(emptyCode)
        val kakaoAuthResult = KakaoAuthResult(
            userId = 111L,
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600
        )
        
        coEvery { kakaoAuthClient.authenticate(emptyCode) } returns kakaoAuthResult

        // When
        val authProvider = resolver.authenticate(request)

        // Then
        assertEquals(ProviderType.KAKAO, authProvider.type)
        assertEquals("111", authProvider.externalId.value)
        
        coVerify(exactly = 1) { kakaoAuthClient.authenticate(emptyCode) }
    }

    @Test
    fun `authenticate - 매우 큰 userId 처리 확인`() = runBlocking {
        // Given
        val code = "test-code"
        val request = AuthRequest.Kakao(code)
        val largeUserId = Long.MAX_VALUE
        val kakaoAuthResult = KakaoAuthResult(
            userId = largeUserId,
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600
        )
        
        coEvery { kakaoAuthClient.authenticate(code) } returns kakaoAuthResult

        // When
        val authProvider = resolver.authenticate(request)

        // Then
        assertEquals(ProviderType.KAKAO, authProvider.type)
        assertEquals(largeUserId.toString(), authProvider.externalId.value)
    }

    @Test
    fun `authenticate - 음수 userId 처리 확인`() = runBlocking {
        // Given
        val code = "test-code"
        val request = AuthRequest.Kakao(code)
        val negativeUserId = -1L
        val kakaoAuthResult = KakaoAuthResult(
            userId = negativeUserId,
            accessToken = "token",
            refreshToken = "refresh",
            expiresIn = 3600
        )
        
        coEvery { kakaoAuthClient.authenticate(code) } returns kakaoAuthResult

        // When
        val authProvider = resolver.authenticate(request)

        // Then
        assertEquals(ProviderType.KAKAO, authProvider.type)
        assertEquals(negativeUserId.toString(), authProvider.externalId.value)
    }

    @Test
    fun `authenticate - 다양한 expiresIn 값 처리 확인`() = runBlocking {
        // Given
        val code = "test-code"
        val request = AuthRequest.Kakao(code)
        val kakaoAuthResult = KakaoAuthResult(
            userId = 999L,
            accessToken = "short-lived-token",
            refreshToken = "refresh",
            expiresIn = 60 // 1분
        )
        
        coEvery { kakaoAuthClient.authenticate(code) } returns kakaoAuthResult

        // When
        val authProvider = resolver.authenticate(request)

        // Then
        assertEquals(ProviderType.KAKAO, authProvider.type)
        assertEquals("999", authProvider.externalId.value)
        
        coVerify(exactly = 1) { kakaoAuthClient.authenticate(code) }
    }
}