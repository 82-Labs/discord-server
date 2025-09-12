package com.jydev.discord.domain.user

enum class UserRole(val description: String) {
    USER("일반 사용자"),
    ADMIN("관리자"),
    TEMPORAL("임시 사용자");
}