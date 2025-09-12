package com.jydev.discord.user.infra

import com.jydev.discord.domain.user.Nickname
import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
import com.jydev.discord.user.infra.persistence.UserEntity
import com.jydev.discord.user.infra.persistence.toDomain
import com.jydev.discord.user.infra.persistence.toEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class UserMapperSimpleTest {

    @Test
    fun `UserEntity toDomain - 단일 role 변환 테스트`() {
        // Given
        val entity = UserEntity(
            id = 123L,
            nickname = "테스트사용자",
            username = "testuser",
            roles = "USER"
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(123L, domain.id)
        assertEquals(Nickname("테스트사용자"), domain.nickname)
        assertEquals(Username("testuser"), domain.username)
        assertEquals(listOf(UserRole.USER), domain.roles)
    }

    @Test
    fun `UserEntity toDomain - 다중 roles 쉼표 구분 변환 테스트`() {
        // Given
        val entity = UserEntity(
            id = 456L,
            nickname = "관리자",
            username = "admin_user", 
            roles = "ADMIN,USER,TEMPORAL"
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(456L, domain.id)
        assertEquals(Nickname("관리자"), domain.nickname)
        assertEquals(Username("admin_user"), domain.username)
        assertEquals(listOf(UserRole.ADMIN, UserRole.USER, UserRole.TEMPORAL), domain.roles)
    }

    @Test
    fun `UserEntity toDomain - 빈 문자열 roles 처리 테스트`() {
        // Given
        val entity = UserEntity(
            id = 789L,
            nickname = "새사용자",
            username = "newuser",
            roles = ""
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(789L, domain.id)
        assertEquals(Nickname("새사용자"), domain.nickname)
        assertEquals(Username("newuser"), domain.username)
        assertEquals(emptyList<UserRole>(), domain.roles)
    }

    @Test
    fun `UserEntity toDomain - 공백만 있는 roles 처리 테스트`() {
        // Given
        val entity = UserEntity(
            id = 321L,
            nickname = "빈역할사용자",
            username = "emptyrole_user",
            roles = "   "
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(321L, domain.id)
        assertEquals(Nickname("빈역할사용자"), domain.nickname)
        assertEquals(Username("emptyrole_user"), domain.username)
        assertEquals(emptyList<UserRole>(), domain.roles)
    }

    @Test
    fun `User toEntity - 다중 roles를 쉼표 구분 문자열로 변환 테스트`() {
        // Given
        val domain = User.of(
            userId = 222L,
            nickname = Nickname("슈퍼관리자"),
            username = Username("super_admin"),
            roles = listOf(UserRole.ADMIN, UserRole.USER)
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(222L, entity.id)
        assertEquals("슈퍼관리자", entity.nickname)
        assertEquals("super_admin", entity.username)
        assertEquals("ADMIN,USER", entity.roles)
    }

    @Test
    fun `User toEntity - 빈 roles 리스트 변환 테스트`() {
        // Given
        val domain = User.of(
            userId = 333L,
            nickname = Nickname("역할없음사용자"),
            username = Username("norole_user"),
            roles = emptyList()
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(333L, entity.id)
        assertEquals("역할없음사용자", entity.nickname)
        assertEquals("norole_user", entity.username)
        assertEquals("", entity.roles)
    }

    @Test
    fun `Mapper 양방향 변환 - roles 일관성 테스트`() {
        // Given
        val originalEntity = UserEntity(
            id = 999L,
            nickname = "테스트계정",
            username = "test_account",
            roles = "ADMIN,USER,TEMPORAL"
        )

        // When
        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        // Then
        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.nickname, convertedEntity.nickname)
        assertEquals(originalEntity.username, convertedEntity.username)
        assertEquals(originalEntity.roles, convertedEntity.roles)
    }
}