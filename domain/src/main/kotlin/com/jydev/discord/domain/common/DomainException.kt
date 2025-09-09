package com.jydev.discord.domain.common

abstract class DomainException(
    val errorCode: ErrorCode,
    message: String = errorCode.message
) : RuntimeException(message)