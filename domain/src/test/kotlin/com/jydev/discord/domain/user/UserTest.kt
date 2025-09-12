package com.jydev.discord.domain.user

import com.jydev.discord.domain.user.exception.UsernameDuplicateException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

class UserTest : StringSpec({

    "User 생성 - 유효한 정보로 사용자 생성 시 정상 생성" {
        // Given
        val nickname = Nickname("테스트사용자")
        val username = Username("test_user")
        val roles = listOf(UserRole.USER)
        
        // When
        val user = User.create(nickname, username, roles)
        
        // Then
        user.id shouldBe null
        user.nickname shouldBe nickname
        user.username shouldBe username
        user.roles shouldBe roles
    }

    "User 생성 - 최소 길이 닉네임과 사용자명으로 생성 시 정상 생성" {
        // Given
        val nickname = Nickname("AB")
        val username = Username("ab")
        val roles = listOf(UserRole.USER)
        
        // When
        val user = User.create(nickname, username, roles)
        
        // Then
        user.nickname shouldBe nickname
        user.username shouldBe username
        user.roles shouldBe roles
    }

    "User 생성 - 최대 길이 닉네임과 사용자명으로 생성 시 정상 생성" {
        // Given
        val longNickname = Nickname("A".repeat(30))
        val longUsername = Username("a".repeat(30))
        val roles = listOf(UserRole.USER)
        
        // When
        val user = User.create(longNickname, longUsername, roles)
        
        // Then
        user.nickname shouldBe longNickname
        user.username shouldBe longUsername
        user.roles shouldBe roles
    }

    "updateNickname - 새로운 닉네임으로 업데이트 시 닉네임 변경됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val originalNickname = user.nickname
        val newNickname = Nickname("새로운닉네임")
        
        // When
        user.updateNickname(newNickname)
        
        // Then
        user.nickname shouldBe newNickname
        user.nickname shouldBe newNickname
        (user.nickname == originalNickname) shouldBe false
    }

    "updateNickname - 동일한 닉네임으로 업데이트 시 정상 처리" {
        // Given
        val user = UserFixture.createKakaoUser()
        val currentNickname = user.nickname
        
        // When
        user.updateNickname(currentNickname)
        
        // Then
        user.nickname shouldBe currentNickname
    }

    "updateUsername - 새로운 사용자명으로 업데이트 시 사용자명 변경됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val originalUsername = user.username
        val newUsername = Username("new_username")
        
        // When
        runBlocking {
            user.updateUsername(newUsername) { false } // 중복 없음
        }
        
        // Then
        user.username shouldBe newUsername
        (user.username == originalUsername) shouldBe false
    }

    "updateUsername - 동일한 사용자명으로 업데이트 시 정상 처리" {
        // Given
        val user = UserFixture.createKakaoUser()
        val currentUsername = user.username
        
        // When
        runBlocking {
            user.updateUsername(currentUsername) { false } // 중복 없음
        }
        
        // Then
        user.username shouldBe currentUsername
    }

    "updateUsername - 중복된 사용자명으로 업데이트 시 UsernameDuplicateException 발생" {
        // Given
        val user = UserFixture.createKakaoUser()
        val duplicateUsername = Username("duplicate_user")
        
        // When & Then
        runBlocking {
            shouldThrow<UsernameDuplicateException> {
                user.updateUsername(duplicateUsername) { true } // 중복 있음
            }
        }
    }

    "updateUsername - 중복체크 함수가 호출되는지 확인" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newUsername = Username("check_called")
        var checkCalled = false
        
        // When
        runBlocking {
            user.updateUsername(newUsername) { username ->
                checkCalled = true
                username shouldBe newUsername // 올바른 사용자명이 전달되는지 확인
                false // 중복 없음
            }
        }
        
        // Then
        checkCalled shouldBe true
        user.username shouldBe newUsername
    }

    "User 불변성 - userId는 변경 불가능" {
        // Given
        val user = UserFixture.createKakaoUser()
        val originalUserId = user.id
        
        // When - 다른 작업들을 수행해도
        user.updateNickname(Nickname("변경된닉네임"))
        runBlocking {
            user.updateUsername(Username("changed_username")) { false }
        }
        
        // Then - userId는 변경되지 않음
        user.id shouldBe originalUserId
    }

    "User 연속 업데이트 - 닉네임과 사용자명을 연속으로 업데이트 시 모두 반영됨" {
        // Given
        val user = UserFixture.createKakaoUser()
        val newNickname = Nickname("연속업데이트닉네임")
        val newUsername = Username("continuous_update")
        
        // When
        user.updateNickname(newNickname)
        runBlocking {
            user.updateUsername(newUsername) { false }
        }
        
        // Then
        user.nickname shouldBe newNickname
        user.username shouldBe newUsername
    }

    "UserFixture 테스트 - 다양한 픽스처로 사용자 생성 시 올바른 데이터 설정" {
        // When
        val kakaoUser = UserFixture.createKakaoUser()
        val longNicknameUser = UserFixture.createUserWithLongNickname()
        val shortNicknameUser = UserFixture.createUserWithShortNickname()
        val numberUsernameUser = UserFixture.createUserWithNumberUsername()
        val underscoreUsernameUser = UserFixture.createUserWithUnderscoreUsername()
        val adminUser = UserFixture.createAdminUser()
        
        // Then
        kakaoUser.id shouldBe null
        kakaoUser.roles shouldBe listOf(UserRole.USER)
        longNicknameUser.nickname.value.length shouldBe 30
        shortNicknameUser.nickname.value shouldBe "짧음"
        numberUsernameUser.username.value shouldBe "123456789"
        underscoreUsernameUser.username.value shouldBe "test_user_name_123"
        adminUser.id shouldBe null
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