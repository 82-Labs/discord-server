package com.jydev.discord.domain.chat

/**
 * 사용자별 DM 채널 관리
 * 사용자가 자신의 DM 목록에서 채널을 숨기거나 관리할 수 있음
 * @param id 엔티티 ID (null이면 새로 생성)
 * @param userId 사용자 ID
 * @param channelId DM 채널 ID
 * @param isHidden 채널 숨김 여부 (목록에서 숨기기)
 */
data class UserDirectMessageChannel(
    val id: Long? = null,
    val userId: Long,
    val channelId: Long,
    val isHidden: Boolean = false
) {
    fun hide(): UserDirectMessageChannel = copy(isHidden = true)
    
    fun show(): UserDirectMessageChannel = copy(isHidden = false)
}