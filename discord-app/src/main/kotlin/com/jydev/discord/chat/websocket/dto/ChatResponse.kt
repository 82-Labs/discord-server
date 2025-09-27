package com.jydev.discord.chat.websocket.dto

import java.time.Instant

/**
 * WebSocket을 통해 클라이언트에게 전송되는 응답 메시지
 */
sealed class ChatResponse {
    abstract val success: Boolean
    abstract val timestamp: Instant
    
    /**
     * 요청 처리 성공
     */
    data class Success(
        override val success: Boolean = true,
        override val timestamp: Instant = Instant.now()
    ) : ChatResponse()
    
    /**
     * 요청 처리 실패
     */
    data class Error(
        val message: String,
        val code: String? = null,
        override val success: Boolean = false,
        override val timestamp: Instant = Instant.now()
    ) : ChatResponse()
}