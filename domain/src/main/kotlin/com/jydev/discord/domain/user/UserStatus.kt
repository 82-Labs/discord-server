package com.jydev.discord.domain.user

/**
 * 사용자 상태
 * User 도메인의 핵심 속성으로 DB에 저장되며 실시간으로 전파됨
 */
enum class UserStatus(
    val displayName: String,
    val isVisible: Boolean = true  // 다른 사용자에게 보이는지 여부
) {
    /**
     * 상태 없음 - 기본값, 실제로는 온라인으로 표시
     * Health Check 시 네트워크 트래픽 최소화용
     */
    NONE("상태 없음", true),
    
    /**
     * 온라인 - 활동 중
     */
    ONLINE("온라인", true),
    
    /**
     * 자리 비움 - 일정 시간 활동 없음 (자동 또는 수동)
     */
    IDLE("자리 비움", true),
    
    /**
     * 방해 금지 - 알림 무음
     */
    DO_NOT_DISTURB("방해 금지", true),
    
    /**
     * 오프라인 표시 - 실제로는 온라인이지만 오프라인으로 보임
     */
    INVISIBLE("오프라인 표시", false),
    
    /**
     * 오프라인 - 연결 끊김
     */
    OFFLINE("오프라인", false);
    
    /**
     * 다른 사용자에게 표시되는 상태
     */
    fun getDisplayStatus(): UserStatus {
        return when (this) {
            NONE -> ONLINE
            INVISIBLE -> OFFLINE
            else -> this
        }
    }
    
    /**
     * 실시간 전파가 필요한지 여부
     */
    fun shouldBroadcast(): Boolean {
        return this != NONE
    }
}