package com.jydev.discord.common.web

import com.jydev.discord.domain.common.DomainException
import jakarta.validation.ConstraintViolationException
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.NotAcceptableStatusException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    suspend fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = ex.errorCode.code,
            message = ex.errorCode.message
        )
        return ResponseEntity
            .status(ex.errorCode.toHttpStatus())
            .body(errorResponse)
    }

    @ExceptionHandler(WebExchangeBindException::class)
    suspend fun handleWebExchangeBindException(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400000",
            message = "입력값이 올바르지 않습니다."
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    suspend fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val message = ex.constraintViolations.firstOrNull()?.message ?: "유효성 검사에 실패했습니다."
        val errorResponse = ErrorResponse(
            code = "E400000",
            message = message
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(ServerWebInputException::class)
    suspend fun handleServerWebInputException(ex: ServerWebInputException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400000",
            message = "입력값이 올바르지 않습니다."
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(DecodingException::class)
    suspend fun handleDecodingException(ex: DecodingException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E400000",
            message = "요청 형식이 올바르지 않습니다."
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException::class)
    suspend fun handleUnsupportedMediaTypeStatusException(ex: UnsupportedMediaTypeStatusException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E415000",
            message = "지원하지 않는 미디어 타입입니다."
        )
        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(errorResponse)
    }

    @ExceptionHandler(NotAcceptableStatusException::class)
    suspend fun handleNotAcceptableStatusException(ex: NotAcceptableStatusException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E406000",
            message = "허용되지 않는 요청입니다."
        )
        return ResponseEntity
            .status(HttpStatus.NOT_ACCEPTABLE)
            .body(errorResponse)
    }

    @ExceptionHandler(MethodNotAllowedException::class)
    suspend fun handleMethodNotAllowedException(ex: MethodNotAllowedException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E405000",
            message = "지원하지 않는 HTTP 메소드입니다."
        )
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(errorResponse)
    }

    @ExceptionHandler(ResponseStatusException::class)
    suspend fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ErrorResponse> {
        val statusCode = ex.statusCode.value()
        val errorResponse = ErrorResponse(
            code = "E${statusCode}000",
            message = ex.reason ?: "요청 처리 중 오류가 발생했습니다."
        )
        return ResponseEntity
            .status(ex.statusCode)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    suspend fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "E500000",
            message = "서버 내부 오류가 발생했습니다."
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}