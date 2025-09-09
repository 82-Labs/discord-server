package com.jydev.discord.common.web

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 코드", example = "E40000001")
    val code: String,
    
    @Schema(description = "에러 메시지", example = "잘못된 요청입니다.")
    val message: String
)