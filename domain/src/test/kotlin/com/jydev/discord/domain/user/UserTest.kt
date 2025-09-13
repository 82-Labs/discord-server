package com.jydev.discord.domain.user

import com.jydev.discord.domain.user.exception.UsernameDuplicateException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

class UserTest : StringSpec({

    "User 생성 - 유효한 정보로 사용자 생성 시 정상 생성" {
        // Given
        val username = Username("test_user")
        val roles = listOf(UserRole.USER)
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        val user = User.create(username, roles, checkDuplicate)
        
        // Then
        user.id shouldBe null
        user.nickname.value shouldBe username.value
        user.username shouldBe username
        user.roles shouldBe roles
    }

    "User 생성 - 최소 길이 사용자명으로 생성 시 정상 생성" {
        // Given
        val username = Username("ab")
        val roles = listOf(UserRole.USER)
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        val user = User.create(username, roles, checkDuplicate)
        
        // Then
        user.nickname.value shouldBe username.value
        user.username shouldBe username
        user.roles shouldBe roles
    }

    "User 생성 - 최대 길이 사용자명으로 생성 시 정상 생성" {
        // Given
        val longUsername = Username("a".repeat(30))
        val roles = listOf(UserRole.USER)
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        val user = User.create(longUsername, roles, checkDuplicate)
        
        // Then
        user.nickname.value shouldBe longUsername.value
        user.username shouldBe longUsername
        user.roles shouldBe roles
    }

    "User 생성 - 중복된 사용자명으로 생성 시 UsernameDuplicateException 발생" {
        // Given
        val username = Username("duplicate_user")
        val roles = listOf(UserRole.USER)
        val checkDuplicate: suspend (Username) -> Boolean = { true } // 중복됨
        
        // When & Then
        shouldThrow<UsernameDuplicateException> {
            runBlocking {
                User.create(username, roles, checkDuplicate)
            }
        }
    }

    "updateNickname - 새로운 닉네임으로 업데이트 시 닉네임 변경됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newNickname = Nickname("새로운닉네임")
        
        // When
        user.updateNickname(newNickname)
        
        // Then
        user.nickname shouldBe newNickname
    }

    "updateNickname - 동일한 닉네임으로 업데이트 시 정상 처리" {
        // Given
        val user = UserFixture.createKakaoUser()
        val sameNickname = user.nickname
        
        // When
        user.updateNickname(sameNickname)
        
        // Then
        user.nickname shouldBe sameNickname
    }

    "updateUsername - 새로운 사용자명으로 업데이트 시 사용자명 변경됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newUsername = Username("new_username")
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        user.updateUsername(newUsername, checkDuplicate)
        
        // Then
        user.username shouldBe newUsername
    }

    "updateUsername - 동일한 사용자명으로 업데이트 시 정상 처리" {
        // Given
        val user = UserFixture.createKakaoUser()
        val sameUsername = user.username
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        user.updateUsername(sameUsername, checkDuplicate)
        
        // Then
        user.username shouldBe sameUsername
    }

    "updateUsername - 중복된 사용자명으로 업데이트 시 UsernameDuplicateException 발생" {
        // Given
        val user = UserFixture.createKakaoUser()
        val duplicateUsername = Username("duplicate_user")
        val checkDuplicate: suspend (Username) -> Boolean = { true }
        
        // When & Then
        shouldThrow<UsernameDuplicateException> {
            runBlocking {
                user.updateUsername(duplicateUsername, checkDuplicate)
            }
        }
    }

    "updateUsername - 중복체크 함수가 호출되는지 확인" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newUsername = Username("new_user")
        var checkCalled = false
        val checkDuplicate: suspend (Username) -> Boolean = { 
            checkCalled = true
            false 
        }
        
        // When
        user.updateUsername(newUsername, checkDuplicate)
        
        // Then
        checkCalled shouldBe true
    }

    "User 불변성 - userId는 변경 불가능" {
        // Given
        val user = UserFixture.createKakaoUser()
        val originalId = user.id
        
        // When
        user.updateNickname(Nickname("변경된닉네임"))
        user.updateUsername(Username("changed_user")) { false }
        
        // Then
        user.id shouldBe originalId
    }

    "User 연속 업데이트 - 닉네임과 사용자명을 연속으로 업데이트 시 모두 반영됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newNickname = Nickname("새닉네임")
        val newUsername = Username("new_username")
        val checkDuplicate: suspend (Username) -> Boolean = { false }
        
        // When
        user.updateNickname(newNickname)
        user.updateUsername(newUsername, checkDuplicate)
        
        // Then
        user.nickname shouldBe newNickname
        user.username shouldBe newUsername
    }

    "UserFixture 테스트 - 다양한 픽스처로 사용자 생성 시 올바른 데이터 설정" {
        // When
        val kakaoUser = UserFixture.createKakaoUser()
        val adminUser = UserFixture.createAdminUser()
        
        // Then
        kakaoUser.nickname.value shouldBe "테스트사용자"
        kakaoUser.username.value shouldBe "testuser"
        kakaoUser.roles shouldBe listOf(UserRole.USER)
        
        adminUser.nickname.value shouldBe "관리자"
        adminUser.username.value shouldBe "admin_user"
        adminUser.roles shouldBe listOf(UserRole.ADMIN, UserRole.USER)
    }

    "updateRoles - 권한 업데이트" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newRoles = listOf(UserRole.ADMIN, UserRole.USER)
        
        // When
        user.updateRoles(newRoles)
        
        // Then
        user.roles shouldBe newRoles
    }
})