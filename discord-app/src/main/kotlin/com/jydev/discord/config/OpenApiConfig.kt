package com.jydev.discord.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openAPI(): OpenAPI {
        val info = Info()
            .title("Discord API")
            .version("1.0.0")
            .description("Discord API")

        return OpenAPI()
            .servers(
                listOf(Server().url(LOCAL_URL).description("Local (개발)"))
            )
            .components(
                Components().addSecuritySchemes(
                    "AccessToken",
                    SecurityScheme().type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .info(info)
    }

    companion object {
        const val LOCAL_URL: String = "http://localhost:8080"
    }
}
