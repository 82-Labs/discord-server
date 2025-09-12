package com.jydev.discord.domain.user

@JvmInline
value class Nickname(val value: String) {
    init {
        require(value.isNotBlank()) { "닉네임은 공백일 수 없습니다." }
        require(value.length in 2..30) { "닉네임은 2~30자 사이여야 합니다." }
    }
}