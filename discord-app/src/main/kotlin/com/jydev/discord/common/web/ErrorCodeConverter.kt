package com.jydev.discord.common.web

import com.jydev.discord.domain.common.ErrorCode
import org.springframework.http.HttpStatus

fun ErrorCode.toHttpStatus(): HttpStatus = when (this) {
    ErrorCode.USERNAME_DUPLICATE -> HttpStatus.BAD_REQUEST
}