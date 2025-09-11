package com.jydev.discord.domain.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ProviderTypeTest : StringSpec({

    "ProviderType - KAKAO 값 확인" {
        // When
        val providerType = ProviderType.KAKAO
        
        // Then
        providerType shouldBe ProviderType.KAKAO
        providerType.name shouldBe "KAKAO"
    }

    "ProviderType - valueOf로 KAKAO 조회 시 정상 반환" {
        // When
        val providerType = ProviderType.valueOf("KAKAO")
        
        // Then
        providerType shouldBe ProviderType.KAKAO
    }

    "ProviderType - values()로 모든 값 조회 시 KAKAO 포함" {
        // When
        val allValues = ProviderType.values()
        
        // Then
        allValues shouldBe arrayOf(ProviderType.KAKAO)
        allValues.size shouldBe 1
    }

    "ProviderType - toString() 결과 확인" {
        // When
        val result = ProviderType.KAKAO.toString()
        
        // Then
        result shouldBe "KAKAO"
    }

    "ProviderType - ordinal 값 확인" {
        // When
        val ordinal = ProviderType.KAKAO.ordinal
        
        // Then
        ordinal shouldBe 0
    }

    "ProviderType 동등성 - 같은 enum 값 비교 시 동일함" {
        // Given
        val provider1 = ProviderType.KAKAO
        val provider2 = ProviderType.KAKAO
        
        // When & Then
        provider1 shouldBe provider2
        (provider1 === provider2) shouldBe true
    }

    "ProviderType enum 클래스 정보 확인" {
        // When
        val enumClass = ProviderType::class.java
        
        // Then
        enumClass.isEnum shouldBe true
        enumClass.enumConstants.size shouldBe 1
        enumClass.enumConstants[0] shouldBe ProviderType.KAKAO
    }
})