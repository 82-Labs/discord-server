package com.jydev.discord.common.web

import com.jydev.discord.domain.common.ErrorCode
import org.springframework.http.HttpStatus

fun ErrorCode.toHttpStatus(): HttpStatus = when (this) {
    ErrorCode.BAD_REQUEST -> HttpStatus.BAD_REQUEST
    ErrorCode.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED
    ErrorCode.FORBIDDEN -> HttpStatus.FORBIDDEN
    ErrorCode.NOT_FOUND -> HttpStatus.NOT_FOUND
    ErrorCode.CONFLICT -> HttpStatus.CONFLICT
    ErrorCode.INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR
    ErrorCode.USERNAME_DUPLICATE -> HttpStatus.BAD_REQUEST
    ErrorCode.TOKEN_EXPIRED -> HttpStatus.UNAUTHORIZED
}