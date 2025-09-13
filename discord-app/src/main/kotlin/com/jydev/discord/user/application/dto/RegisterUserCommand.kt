package com.jydev.discord.user.application.dto

data class RegisterUserCommand(
    val authCredentialId : Long,
    val username : String
)
