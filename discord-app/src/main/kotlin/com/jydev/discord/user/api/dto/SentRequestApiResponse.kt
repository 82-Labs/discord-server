package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "보낸 친구 요청 목록 응답")
data class SentRequestsApiResponse(
    @field:Schema(description = "보낸 친구 요청 목록")
    val content: List<SentRequestItem>
)

@Schema(description = "보낸 친구 요청 정보")
data class SentRequestItem(
    
    @field:Schema(description = "요청 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "요청 받은 사용자 ID", example = "3")
    val receiverId: Long,
    
    @field:Schema(description = "요청 받은 사용자명", example = "jane_doe")
    val receiverUsername: String,
    
    @field:Schema(description = "요청 받은 사용자 닉네임", example = "Jane")
    val receiverNickname: String
)