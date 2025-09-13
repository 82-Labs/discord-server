package com.jydev.discord.domain.auth.exception

import com.jydev.discord.domain.common.DomainException
import com.jydev.discord.domain.common.ErrorCode

class TokenExpiredException : DomainException(ErrorCode.TOKEN_EXPIRED) {
}