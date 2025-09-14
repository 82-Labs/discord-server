package com.jydev.discord.domain.user.relation

class UserRelationRequest(
    val id: Long? = null,
    val requester: RelationTarget,
    status: UserRelationRequestStatus = UserRelationRequestStatus.PENDING
) {
    var status = status
        private set
    
    val senderId: Long get() = requester.userId
    val receiverId: Long get() = requester.targetUserId
    
    fun accept(): Pair<UserRelation, UserRelation> {
        require(status == UserRelationRequestStatus.PENDING) { "대기중인 요청만 수락할 수 있습니다." }
        status = UserRelationRequestStatus.ACCEPTED
        // 관계 요청이 수락되면 양방향 친구 관계 생성
        return UserRelation.createBidirectionalFriend(senderId, receiverId)
    }
    
    fun reject() {
        require(status == UserRelationRequestStatus.PENDING) { "대기중인 요청만 거절할 수 있습니다." }
        status = UserRelationRequestStatus.REJECTED
    }
    
    fun cancel() {
        require(status == UserRelationRequestStatus.PENDING) { "대기중인 요청만 취소할 수 있습니다." }
        status = UserRelationRequestStatus.CANCELED
    }
    
    fun isPending(): Boolean = status == UserRelationRequestStatus.PENDING
    
    fun isAccepted(): Boolean = status == UserRelationRequestStatus.ACCEPTED
    
    companion object {
        fun create(senderId: Long, receiverId: Long): UserRelationRequest {
            return UserRelationRequest(
                requester = RelationTarget.forRequest(senderId, receiverId)
            )
        }
    }
}