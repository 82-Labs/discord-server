package com.jydev.discord.domain.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ExternalIdTest : StringSpec({

    "ExternalId 생성 - 유효한 외부 ID로 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("kakao_123456789")
        
        // Then
        externalId.value shouldBe "kakao_123456789"
    }

    "ExternalId 생성 - 숫자만 포함한 외부 ID 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("123456789")
        
        // Then
        externalId.value shouldBe "123456789"
    }

    "ExternalId 생성 - 문자와 숫자 혼합 외부 ID 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("user123abc")
        
        // Then
        externalId.value shouldBe "user123abc"
    }

    "ExternalId 생성 - UUID 형태 외부 ID 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("550e8400-e29b-41d4-a716-446655440000")
        
        // Then
        externalId.value shouldBe "550e8400-e29b-41d4-a716-446655440000"
    }

    "ExternalId 생성 - 긴 외부 ID 생성 시 정상 생성" {
        // Given
        val longId = "very_long_external_id_from_oauth_provider_123456789"
        
        // When
        val externalId = ExternalId(longId)
        
        // Then
        externalId.value shouldBe longId
    }

    "ExternalId 생성 - 빈 문자열로 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ExternalId("")
        }
    }

    "ExternalId 생성 - 공백만 있는 문자열은 정상 생성" {
        // Given & When
        val externalId = ExternalId("   ")
        
        // Then
        externalId.value shouldBe "   "
    }

    "ExternalId 생성 - 한 글자 외부 ID 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("1")
        
        // Then
        externalId.value shouldBe "1"
    }

    "ExternalId 생성 - 특수문자 포함 외부 ID 생성 시 정상 생성" {
        // Given & When
        val externalId = ExternalId("user@provider.com")
        
        // Then
        externalId.value shouldBe "user@provider.com"
    }

    "ExternalId 동등성 - 같은 값을 가진 ExternalId 비교 시 동일함" {
        // Given
        val externalId1 = ExternalId("12345")
        val externalId2 = ExternalId("12345")
        
        // When & Then
        externalId1 shouldBe externalId2
    }

    "ExternalId 동등성 - 다른 값을 가진 ExternalId 비교 시 다름" {
        // Given
        val externalId1 = ExternalId("12345")
        val externalId2 = ExternalId("67890")
        
        // When & Then
        (externalId1 == externalId2) shouldBe false
    }
})