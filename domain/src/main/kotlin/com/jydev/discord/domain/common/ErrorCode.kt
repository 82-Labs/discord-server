package com.jydev.discord.domain.common

enum class ErrorCode(val code: String, val message: String) {
    USERNAME_DUPLICATE("E400001", "이미 사용 중인 사용자명입니다."),
    ;
}