package com.jydev.discord.domain.user

@JvmInline
value class Username(val value: String) {

    init {
        require(value.isNotBlank()) { "사용자명은 공백일 수 없습니다." }
        require(value.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            "사용자명은 영문자, 숫자, 밑줄(_)만 사용할 수 있습니다."
        }
        require(value.length in 2..30) { "사용자명은 2~30자 사이여야 합니다." }
    }
}