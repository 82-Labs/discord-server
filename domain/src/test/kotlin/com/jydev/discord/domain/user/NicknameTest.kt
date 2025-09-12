package com.jydev.discord.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class NicknameTest : StringSpec({

    "Nickname 생성 - 유효한 닉네임으로 생성 시 정상 생성" {
        // Given & When
        val nickname = Nickname("사용자1")
        
        // Then
        nickname.value shouldBe "사용자1"
    }

    "Nickname 생성 - 2자 최소 길이 닉네임 생성 시 정상 생성" {
        // Given & When
        val nickname = Nickname("AB")
        
        // Then
        nickname.value shouldBe "AB"
    }

    "Nickname 생성 - 30자 최대 길이 닉네임 생성 시 정상 생성" {
        // Given
        val longNickname = "A".repeat(30)
        
        // When
        val nickname = Nickname(longNickname)
        
        // Then
        nickname.value shouldBe longNickname
    }

    "Nickname 생성 - 빈 문자열로 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Nickname("")
        }
    }

    "Nickname 생성 - 공백만 있는 문자열로 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Nickname("   ")
        }
    }

    "Nickname 생성 - 1자 닉네임 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Nickname("A")
        }
    }

    "Nickname 생성 - 31자 닉네임 생성 시 IllegalArgumentException 발생" {
        // Given
        val tooLongNickname = "A".repeat(31)
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Nickname(tooLongNickname)
        }
    }

    "Nickname 동등성 - 같은 값을 가진 Nickname 비교 시 동일함" {
        // Given
        val nickname1 = Nickname("테스터")
        val nickname2 = Nickname("테스터")
        
        // When & Then
        nickname1 shouldBe nickname2
    }

    "Nickname 동등성 - 다른 값을 가진 Nickname 비교 시 다름" {
        // Given
        val nickname1 = Nickname("사용자1")
        val nickname2 = Nickname("사용자2")
        
        // When & Then
        (nickname1 == nickname2) shouldBe false
    }
})