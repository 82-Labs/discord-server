package com.jydev.discord.chat.api.dto

import jakarta.validation.constraints.Size

data class CreateDirectMessageChannelRequest(
    @field:Size(min = 1, message = "최소 1명 이상의 다른 사용자가 필요합니다")
    val userIds: Set<Long>
)