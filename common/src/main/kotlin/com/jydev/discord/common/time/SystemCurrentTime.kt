package com.jydev.discord.common.time

import java.time.Instant

class SystemCurrentTime : CurrentTime {
    override fun now(): Instant = Instant.now()
}