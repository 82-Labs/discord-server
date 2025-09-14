package com.jydev.discord.domain.user.relation

class UserRelation(
    val id: Long? = null,
    val target: RelationTarget,
    val relationType: UserRelationType = UserRelationType.FRIEND
) {
    val userId: Long get() = target.userId
    val relatedUserId: Long get() = target.targetUserId
    
    companion object {
        fun createFriend(userId: Long, friendId: Long): UserRelation {
            return UserRelation(
                target = RelationTarget.forRelation(userId, friendId),
                relationType = UserRelationType.FRIEND
            )
        }
        
        // 친구 관계는 양방향이므로 두 개의 UserRelation 객체를 생성
        fun createBidirectionalFriend(userId: Long, friendId: Long): Pair<UserRelation, UserRelation> {
            val target = RelationTarget.forRelation(userId, friendId)
            return Pair(
                UserRelation(target = target, relationType = UserRelationType.FRIEND),
                UserRelation(target = target.reversed(), relationType = UserRelationType.FRIEND)
            )
        }
        
        fun createBlock(userId: Long, blockedUserId: Long): UserRelation {
            return UserRelation(
                target = RelationTarget.forRelation(userId, blockedUserId),
                relationType = UserRelationType.BLOCKED
            )
        }
    }
}