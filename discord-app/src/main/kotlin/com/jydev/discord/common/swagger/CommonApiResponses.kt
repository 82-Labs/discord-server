package com.jydev.discord.common.swagger

import com.jydev.discord.common.web.ErrorResponse
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

/**
 * Swagger 공통 에러 응답 정의
 * 모든 API에 기본적으로 포함되는 400, 500 에러를 정의
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Bad Request",
                    value = """{"code": "E400000", "message": "잘못된 요청입니다."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Internal Server Error",
                    value = """{"code": "E500000", "message": "서버 내부 오류가 발생했습니다."}"""
                )]
            )]
        )
    ]
)
annotation class CommonErrorResponses

/**
 * 인증이 필요한 API의 공통 에러 응답
 * 400, 401, 403, 500 에러를 정의
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponses(
    value = [
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Bad Request",
                    value = """{"code": "E400000", "message": "잘못된 요청입니다."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Unauthorized",
                    value = """{"code": "E401000", "message": "인증이 필요합니다."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Forbidden",
                    value = """{"code": "E403000", "message": "접근 권한이 없습니다."}"""
                )]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류",
            content = [Content(
                schema = Schema(implementation = ErrorResponse::class),
                examples = [ExampleObject(
                    name = "Internal Server Error",
                    value = """{"code": "E500000", "message": "서버 내부 오류가 발생했습니다."}"""
                )]
            )]
        )
    ]
)
annotation class AuthenticatedApiResponses