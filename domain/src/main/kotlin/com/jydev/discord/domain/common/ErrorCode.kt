package com.jydev.discord.domain.common

enum class ErrorCode(val code: String, val message: String) {
    // 공통 에러코드
    BAD_REQUEST("E400000", "잘못된 요청입니다."),
    UNAUTHORIZED("E401000", "인증이 필요합니다."),
    FORBIDDEN("E403000", "접근 권한이 없습니다."),
    NOT_FOUND("E404000", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT("E409000", "리소스 충돌이 발생했습니다."),
    INTERNAL_SERVER_ERROR("E500000", "서버 내부 오류가 발생했습니다."),
    
    // User 도메인 에러코드
    USERNAME_DUPLICATE("E400001", "이미 사용 중인 사용자명입니다."),
    ;
}