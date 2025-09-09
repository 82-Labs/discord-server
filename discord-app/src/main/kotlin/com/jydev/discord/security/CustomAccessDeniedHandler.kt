package com.jydev.discord.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.jydev.discord.common.web.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : ServerAccessDeniedHandler {

    override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.FORBIDDEN
        response.headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)

        val errorResponse = ErrorResponse(
            code = "E403000",
            message = "접근 권한이 없습니다."
        )

        val buffer = response.bufferFactory().wrap(
            objectMapper.writeValueAsBytes(errorResponse)
        )

        return response.writeWith(Mono.just(buffer))
    }
}