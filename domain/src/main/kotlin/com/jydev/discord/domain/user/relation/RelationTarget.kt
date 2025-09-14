package com.jydev.discord.domain.user.relation

class RelationTarget private constructor(
    val userId: Long,
    val targetUserId: Long
) {
    fun reversed(): RelationTarget {
        return RelationTarget(targetUserId, userId)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RelationTarget) return false
        return userId == other.userId && targetUserId == other.targetUserId
    }
    
    override fun hashCode(): Int {
        return 31 * userId.hashCode() + targetUserId.hashCode()
    }
    
    companion object {
        fun forRelation(userId: Long, targetUserId: Long): RelationTarget {
            require(userId != targetUserId) { "자기 자신과 관계를 맺을 수 없습니다." }
            return RelationTarget(userId, targetUserId)
        }
        
        fun forRequest(senderId: Long, receiverId: Long): RelationTarget {
            require(senderId != receiverId) { "자기 자신에게 관계 요청을 보낼 수 없습니다." }
            return RelationTarget(senderId, receiverId)
        }
    }
}