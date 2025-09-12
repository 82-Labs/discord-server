package com.jydev.discord.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class UsernameTest : StringSpec({

    "Username 생성 - 유효한 사용자명으로 생성 시 정상 생성" {
        // Given & When
        val username = Username("user123")
        
        // Then
        username.value shouldBe "user123"
    }

    "Username 생성 - 영문자만 포함한 사용자명 생성 시 정상 생성" {
        // Given & When
        val username = Username("username")
        
        // Then
        username.value shouldBe "username"
    }

    "Username 생성 - 숫자만 포함한 사용자명 생성 시 정상 생성" {
        // Given & When
        val username = Username("123456")
        
        // Then
        username.value shouldBe "123456"
    }

    "Username 생성 - 언더스코어만 포함한 사용자명 생성 시 정상 생성" {
        // Given & When
        val username = Username("user_name_123")
        
        // Then
        username.value shouldBe "user_name_123"
    }

    "Username 생성 - 2자 최소 길이 사용자명 생성 시 정상 생성" {
        // Given & When
        val username = Username("ab")
        
        // Then
        username.value shouldBe "ab"
    }

    "Username 생성 - 30자 최대 길이 사용자명 생성 시 정상 생성" {
        // Given
        val longUsername = "a".repeat(30)
        
        // When
        val username = Username(longUsername)
        
        // Then
        username.value shouldBe longUsername
    }

    "Username 생성 - 빈 문자열로 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("")
        }
    }

    "Username 생성 - 공백만 있는 문자열로 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("   ")
        }
    }

    "Username 생성 - 1자 사용자명 생성 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("a")
        }
    }

    "Username 생성 - 31자 사용자명 생성 시 IllegalArgumentException 발생" {
        // Given
        val tooLongUsername = "a".repeat(31)
        
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username(tooLongUsername)
        }
    }

    "Username 생성 - 특수문자 포함 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("user@name")
        }
    }

    "Username 생성 - 공백 포함 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("user name")
        }
    }

    "Username 생성 - 하이픈 포함 시 IllegalArgumentException 발생" {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            Username("user-name")
        }
    }

    "Username 동등성 - 같은 값을 가진 Username 비교 시 동일함" {
        // Given
        val username1 = Username("testuser")
        val username2 = Username("testuser")
        
        // When & Then
        username1 shouldBe username2
    }

    "Username 동등성 - 다른 값을 가진 Username 비교 시 다름" {
        // Given
        val username1 = Username("user1")
        val username2 = Username("user2")
        
        // When & Then
        (username1 == username2) shouldBe false
    }
})