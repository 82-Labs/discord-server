package com.jydev.discord.user.application

import com.jydev.discord.domain.user.relation.UserRelationRepository
import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationRequestRepository
import com.jydev.discord.user.application.dto.HandleUserRelationRequestCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HandleUserRelationRequestUseCase(
    private val userRelationRequestRepository: UserRelationRequestRepository,
    private val userRelationRepository: UserRelationRepository
) {
    
    @Transactional
    suspend operator fun invoke(command: HandleUserRelationRequestCommand) {
        val (requestId, userId, action) = command
        
        // 1. 요청 조회
        val request = userRelationRequestRepository.findById(requestId)
            ?: throw IllegalArgumentException("요청을 찾을 수 없습니다.")
        
        // 2. 권한 확인 및 액션 처리
        when (action) {
            UserRelationRequestAction.ACCEPT -> {
                require(request.receiverId == userId) { "해당 요청의 수신자가 아닙니다." }
                
                // 요청 수락 및 양방향 관계 생성
                val (relation1, relation2) = request.accept()
                userRelationRepository.saveAll(listOf(relation1, relation2))
            }
            
            UserRelationRequestAction.REJECT -> {
                require(request.receiverId == userId) { "해당 요청의 수신자가 아닙니다." }
                request.reject()
            }
            
            UserRelationRequestAction.CANCEL -> {
                require(request.senderId == userId) { "해당 요청의 발신자가 아닙니다." }
                request.cancel()
            }
        }
        
        // 3. 요청 상태 업데이트
        userRelationRequestRepository.save(request)
    }
}