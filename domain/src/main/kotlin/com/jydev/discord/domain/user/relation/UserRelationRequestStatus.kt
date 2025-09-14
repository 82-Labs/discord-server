package com.jydev.discord.domain.user.relation

enum class UserRelationRequestStatus(val description: String) {
    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    CANCELED("취소됨")
}