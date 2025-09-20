package com.jydev.discord.user.api.dto

import com.jydev.discord.domain.user.relation.UserRelationType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 관계 목록 응답")
data class UserRelationsApiResponse(
    @field:Schema(description = "사용자 관계 목록")
    val content: List<UserRelationItem>
)

@Schema(description = "사용자 관계 정보")
data class UserRelationItem(
    
    @field:Schema(description = "관계 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "관련 사용자 ID", example = "2")
    val userId: Long,
    
    @field:Schema(description = "관련 사용자명", example = "john_doe")
    val username: String,
    
    @field:Schema(description = "관련 사용자 닉네임", example = "John")
    val nickname: String,
    
    @field:Schema(description = "관계 유형", example = "FRIEND")
    val type: UserRelationType
)