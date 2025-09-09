package com.jydev.discord.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.jydev.discord.common.web.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : ServerAuthenticationEntryPoint {

    override fun commence(exchange: ServerWebExchange, ex: AuthenticationException): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)

        val errorResponse = ErrorResponse(
            code = "E401000",
            message = "인증이 필요합니다."
        )

        val buffer = response.bufferFactory().wrap(
            objectMapper.writeValueAsBytes(errorResponse)
        )

        return response.writeWith(Mono.just(buffer))
    }
}