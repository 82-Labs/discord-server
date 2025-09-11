package com.jydev.discord.domain.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AuthCredentialTest : StringSpec({

    "AuthCredential 생성 - 유효한 provider와 externalId로 생성 시 정상 생성" {
        // Given
        val provider = ProviderType.KAKAO
        val externalId = ExternalId("kakao123456")
        val authProvider = AuthProvider(provider, externalId)
        
        // When
        val credential = AuthCredential.create(authProvider)
        
        // Then
        credential.authProvider.type shouldBe provider
        credential.authProvider.externalId shouldBe externalId
    }

    "AuthCredential 팩토리 메서드 - KAKAO 타입으로 생성" {
        // Given
        val externalId = "kakao_test_123"
        val authProvider = AuthProvider(ProviderType.KAKAO, ExternalId(externalId))
        
        // When
        val credential = AuthCredential.create(authProvider)
        
        // Then
        credential.authProvider.type shouldBe ProviderType.KAKAO
        credential.authProvider.externalId shouldBe ExternalId(externalId)
    }

    "AuthCredential 팩토리 메서드 - userId와 함께 생성" {
        // Given
        val provider = ProviderType.KAKAO
        val externalId = "test_external_id"
        val userId = 123L
        val authProvider = AuthProvider(provider, ExternalId(externalId))
        
        // When
        val credential = AuthCredential.create(authProvider, userId)
        
        // Then
        credential.authProvider.type shouldBe provider
        credential.authProvider.externalId shouldBe ExternalId(externalId)
        credential.userId shouldBe userId
    }

    "AuthCredential 동등성 - 같은 provider와 externalId를 가진 경우 동일함" {
        // Given
        val authProvider1 = AuthProvider(ProviderType.KAKAO, ExternalId("same_id"))
        val authProvider2 = AuthProvider(ProviderType.KAKAO, ExternalId("same_id"))
        val credential1 = AuthCredential.create(authProvider1)
        val credential2 = AuthCredential.create(authProvider2)
        
        // When & Then
        credential1.authProvider.type shouldBe credential2.authProvider.type
        credential1.authProvider.externalId shouldBe credential2.authProvider.externalId
    }

    "AuthCredential 동등성 - 다른 externalId를 가진 경우 다름" {
        // Given
        val authProvider1 = AuthProvider(ProviderType.KAKAO, ExternalId("id_1"))
        val authProvider2 = AuthProvider(ProviderType.KAKAO, ExternalId("id_2"))
        val credential1 = AuthCredential.create(authProvider1)
        val credential2 = AuthCredential.create(authProvider2)
        
        // When & Then
        (credential1.authProvider.externalId.value == credential2.authProvider.externalId.value) shouldBe false
    }

    "AuthCredential toString - 디버깅용 문자열 표현 확인" {
        // Given
        val authProvider = AuthProvider(ProviderType.KAKAO, ExternalId("debug_test_123"))
        val credential = AuthCredential.create(authProvider)
        
        // When
        val result = credential.toString()
        
        // Then
        result.contains("AuthCredential") shouldBe true
        credential.authProvider.type shouldBe ProviderType.KAKAO
        credential.authProvider.externalId.value shouldBe "debug_test_123"
    }

    "AuthCredential userId 없이 생성" {
        // Given
        val authProvider = AuthProvider(ProviderType.KAKAO, ExternalId("no_user_id"))
        
        // When
        val credential = AuthCredential.create(authProvider)
        
        // Then
        credential.userId shouldBe null
        credential.authProvider.type shouldBe ProviderType.KAKAO
        credential.authProvider.externalId.value shouldBe "no_user_id"
    }
})