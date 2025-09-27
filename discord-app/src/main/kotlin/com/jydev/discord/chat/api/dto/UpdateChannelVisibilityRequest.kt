package com.jydev.discord.chat.api.dto

import jakarta.validation.constraints.NotNull

data class UpdateChannelVisibilityRequest(
    @field:NotNull
    val hide: Boolean
)