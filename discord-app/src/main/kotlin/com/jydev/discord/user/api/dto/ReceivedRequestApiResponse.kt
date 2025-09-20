package com.jydev.discord.user.api.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "받은 친구 요청 목록 응답")
data class ReceivedRequestsApiResponse(
    @field:Schema(description = "받은 친구 요청 목록")
    val content: List<ReceivedRequestItem>
)

@Schema(description = "받은 친구 요청 정보")
data class ReceivedRequestItem(
    
    @field:Schema(description = "요청 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "요청 보낸 사용자 ID", example = "2")
    val senderId: Long,
    
    @field:Schema(description = "요청 보낸 사용자명", example = "john_doe")
    val senderUsername: String,
    
    @field:Schema(description = "요청 보낸 사용자 닉네임", example = "John")
    val senderNickname: String
)