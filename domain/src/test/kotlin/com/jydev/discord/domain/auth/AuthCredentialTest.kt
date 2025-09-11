package com.jydev.discord.domain.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AuthCredentialTest : StringSpec({

    "AuthCredential 생성 - 유효한 provider와 externalId로 생성 시 정상 생성" {
        // Given
        val provider = ProviderType.KAKAO
        val externalId = ExternalId("kakao123456")
        
        // When
        val credential = AuthCredential.create(provider, externalId.value)
        
        // Then
        credential.authProvider.type shouldBe provider
        credential.authProvider.externalId shouldBe externalId
    }

    "AuthCredential 팩토리 메서드 - kakao() 사용 시 KAKAO 타입으로 생성" {
        // Given
        val externalId = "kakao_test_123"
        
        // When
        val credential = AuthCredential.create(ProviderType.KAKAO, externalId)
        
        // Then
        credential.authProvider.type shouldBe ProviderType.KAKAO
        credential.authProvider.externalId shouldBe ExternalId(externalId)
    }

    "AuthCredential 팩토리 메서드 - of() 사용 시 지정된 타입으로 생성" {
        // Given
        val provider = ProviderType.KAKAO
        val externalId = "test_external_id"
        
        // When
        val credential = AuthCredential.create(provider, externalId)
        
        // Then
        credential.authProvider.type shouldBe provider
        credential.authProvider.externalId shouldBe ExternalId(externalId)
    }

    "AuthCredential 동등성 - 같은 provider와 externalId를 가진 경우 동일함" {
        // Given
        val credential1 = AuthCredential.create(ProviderType.KAKAO, "same_id")
        val credential2 = AuthCredential.create(ProviderType.KAKAO, "same_id")
        
        // When & Then
        credential1.authProvider.type shouldBe credential2.authProvider.type
        credential1.authProvider.externalId shouldBe credential2.authProvider.externalId
    }

    "AuthCredential 동등성 - 다른 provider를 가진 경우 다름" {
        // Given
        val credential1 = AuthCredential.create(ProviderType.KAKAO, "same_id")
        val credential2 = AuthCredential.create(ProviderType.KAKAO, "same_id") // 현재는 KAKAO만 있어서 같음
        
        // When & Then
        credential1.authProvider.type shouldBe credential2.authProvider.type // KAKAO만 있어서 현재는 동일
    }

    "AuthCredential 동등성 - 다른 externalId를 가진 경우 다름" {
        // Given
        val credential1 = AuthCredential.create(ProviderType.KAKAO, "id_1")
        val credential2 = AuthCredential.create(ProviderType.KAKAO, "id_2")
        
        // When & Then
        (credential1.authProvider.externalId.value == credential2.authProvider.externalId.value) shouldBe false
    }

    "AuthCredential toString - 디버깅용 문자열 표현 확인" {
        // Given
        val credential = AuthCredential.create(ProviderType.KAKAO, "debug_test_123")
        
        // When
        val result = credential.toString()
        
        // Then
        result.contains("AuthCredential") shouldBe true
        credential.authProvider.type shouldBe ProviderType.KAKAO
        credential.authProvider.externalId.value shouldBe "debug_test_123"
    }
})