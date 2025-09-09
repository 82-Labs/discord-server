package com.jydev.discord.common.time

import java.time.Instant

interface CurrentTime {
    fun now(): Instant
}