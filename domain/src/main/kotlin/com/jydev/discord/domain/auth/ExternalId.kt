package com.jydev.discord.domain.auth

@JvmInline
value class ExternalId(val value: String) {
    init {
        require(value.isNotEmpty()) { "외부 ID는 비어있을 수 없습니다." }
    }
}