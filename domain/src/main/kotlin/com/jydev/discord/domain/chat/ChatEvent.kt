package com.jydev.discord.domain.chat

import com.jydev.discord.domain.user.UserStatus
import java.time.Instant

/**
 * 채팅 도메인에서 발생하는 이벤트
 * 사용자의 채팅 관련 행동과 상태 변경을 나타냄
 */
sealed class ChatEvent {
    abstract val senderId: Long
    abstract val timestamp: Instant
    
    /**
     * 연결 상태 확인 메시지
     * Health Check와 함께 현재 사용자 상태도 전송
     * @param status 현재 사용자 상태 (선택, 없으면 서버가 현재 상태 사용)
     */
    data class HealthCheck(
        val status: UserStatus? = null,
        override val senderId: Long,
        override val timestamp: Instant = Instant.now()
    ) : ChatEvent()
    
    /**
     * 메시지 전송 (채널/DM 통합)
     * @param channelId 대상 채널 ID (DM 채널 포함)
     * @param content 메시지 내용
     * @param mentions 멘션된 사용자 ID 목록
     */
    data class SendMessage(
        val channelId: Long,
        val content: String,
        val mentions: List<Long> = emptyList(),
        override val senderId: Long,
        override val timestamp: Instant = Instant.now()
    ) : ChatEvent()
    
    /**
     * 사용자 상태 업데이트
     * @param status 변경할 상태
     */
    data class UpdateUserStatus(
        val status: UserStatus,
        override val senderId: Long,
        override val timestamp: Instant = Instant.now()
    ) : ChatEvent()
    
    /**
     * 사용자 타이핑 상태
     * 서버는 단순 전달만 담당, TTL은 클라이언트에서 처리
     * @param channelId 타이핑 중인 채널 ID (DM 채널 포함)
     */
    data class UserTyping(
        val channelId: String,
        override val senderId: Long,
        override val timestamp: Instant = Instant.now()
    ) : ChatEvent()
}