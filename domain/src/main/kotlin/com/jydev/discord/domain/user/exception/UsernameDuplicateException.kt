package com.jydev.discord.domain.user.exception

import com.jydev.discord.domain.common.DomainException
import com.jydev.discord.domain.common.ErrorCode

class UsernameDuplicateException : DomainException(ErrorCode.USERNAME_DUPLICATE)