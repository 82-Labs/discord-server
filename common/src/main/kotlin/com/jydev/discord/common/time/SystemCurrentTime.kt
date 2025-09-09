package com.jydev.discord.common.time

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SystemCurrentTime : CurrentTime {
    override fun now(): Instant = Instant.now()
}