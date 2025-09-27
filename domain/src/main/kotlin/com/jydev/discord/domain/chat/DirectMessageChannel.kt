package com.jydev.discord.domain.chat

/**
 * Direct Message 채널
 * 두 명 이상의 사용자 간 개인 메시지 채널
 * @param id 채널 고유 ID
 * @param userIds 참여자 ID 목록 (Set으로 중복 방지)
 */
data class DirectMessageChannel(
    val id: Long,
    val userIds: Set<Long>
) {
    init {
        require(userIds.size >= 2) { "DirectMessage 채널은 최소 2명의 사용자가 필요합니다" }
    }
}