package com.jydev.discord

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DiscordServerApplication

fun main(args: Array<String>) {
    runApplication<DiscordServerApplication>(*args)
}
