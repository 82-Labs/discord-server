package com.jydev.discord.chat.websocket.handler

import com.jydev.discord.chat.websocket.dto.ChatResponse
import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.chat.ChatEvent
import kotlin.reflect.KClass

interface MessageHandler<T : ChatEvent> {
    /**
     * 이 핸들러가 처리할 수 있는 이벤트 타입
     */
    val supportedEventType: KClass<T>
    
    /**
     * 이 핸들러가 처리할 수 있는 이벤트인지 확인
     */
    fun supports(event: ChatEvent): Boolean = 
        supportedEventType.isInstance(event)
    
    /**
     * 메시지 처리
     * @return 클라이언트에게 전송할 응답
     */
    suspend fun handle(event: T, authUser: AuthUser.User): ChatResponse
}