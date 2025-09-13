package com.jydev.discord.common.web

import com.jydev.discord.domain.common.DomainException
import io.github.oshai.kotlinlogging.KotlinLogging
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

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DomainException::class)
    suspend fun handleDomainException(ex: DomainException): ResponseEntity<ErrorResponse> {
        logger.warn { "도메인 예외 발생: ${ex.errorCode.code} - ${ex.errorCode.message}" }
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
        logger.warn { "입력값 바인딩 오류: ${ex.message}" }
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
        logger.warn { "유효성 검증 실패: $message" }
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
        logger.warn { "서버 입력 예외: ${ex.reason}" }
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
        logger.warn { "디코딩 오류: ${ex.message}" }
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
        logger.warn { "지원하지 않는 미디어 타입: ${ex.contentType}" }
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
        logger.warn { "허용되지 않는 요청: ${ex.reason}" }
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
        logger.warn { "지원하지 않는 HTTP 메소드: ${ex.httpMethod}" }
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
        logger.warn { "응답 상태 예외 [${ex.statusCode}]: ${ex.reason}" }
        val errorResponse = ErrorResponse(
            code = "E${statusCode}000",
            message = ex.reason ?: "요청 처리 중 오류가 발생했습니다."
        )
        return ResponseEntity
            .status(ex.statusCode)
            .body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    suspend fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn { "잘못된 인자: ${ex.message}" }
        val errorResponse = ErrorResponse(
            code = "E400000",
            message = ex.message ?: "잘못된 요청입니다."
        )
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(IllegalStateException::class)
    suspend fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.warn { "잘못된 상태: ${ex.message}" }
        val errorResponse = ErrorResponse(
            code = "E409000",
            message = ex.message ?: "요청을 처리할 수 없는 상태입니다."
        )
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    suspend fun handleGeneralException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error(ex) { "예상치 못한 오류 발생: ${ex.message}" }
        val errorResponse = ErrorResponse(
            code = "E500000",
            message = ex.message ?: "서버 내부 오류가 발생했습니다."
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}