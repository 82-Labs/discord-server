package com.jydev.discord.user.application

import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.*
import com.jydev.discord.user.application.dto.RequestUserRelationCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RequestUserRelationUseCase(
    private val userRepository: UserRepository,
    private val userRelationRequestRepository: UserRelationRequestRepository,
    private val userRelationRepository: UserRelationRepository
) {
    
    @Transactional
    suspend operator fun invoke(command: RequestUserRelationCommand) {
        val senderId = command.senderId
        val receiverUsername = command.receiverUsername
        
        // 1. 수신자가 존재하는지 확인
        val receiver = userRepository.findByUsername(Username(receiverUsername))
            ?: throw IllegalArgumentException("수신자를 찾을 수 없습니다: $receiverUsername")
        
        val receiverId = receiver.id!!
        
        // 2. 이미 관계가 존재하는지 확인
        val existingRelation = userRelationRepository.findByTarget(
            RelationTarget.forRelation(senderId, receiverId)
        )
        if (existingRelation != null) {
            when (existingRelation.relationType) {
                UserRelationType.FRIEND -> throw IllegalStateException("이미 친구 관계입니다.")
                UserRelationType.BLOCKED -> throw IllegalStateException("차단된 사용자입니다.")
            }
        }
        
        // 3. 이미 보낸 요청이 있는지 확인
        val existingRequest = userRelationRequestRepository.findByRequester(
            RelationTarget.forRequest(senderId, receiverId)
        )
        if (existingRequest != null && existingRequest.isPending()) {
            throw IllegalStateException("이미 대기중인 요청이 있습니다.")
        }
        
        // 4. 상대방이 나에게 보낸 요청이 있는지 확인 (자동 수락)
        val reverseRequest = userRelationRequestRepository.findByRequester(
            RelationTarget.forRequest(receiverId, senderId)
        )
        if (reverseRequest != null && reverseRequest.isPending()) {
            // 상대방이 이미 나에게 요청을 보낸 경우, 자동으로 양쪽 수락
            val (relation1, relation2) = reverseRequest.accept()
            userRelationRepository.saveAll(listOf(relation1, relation2))
            userRelationRequestRepository.save(reverseRequest)
            return
        }
        
        // 5. 새로운 요청 생성
        val request = UserRelationRequest.create(senderId, receiverId)
        userRelationRequestRepository.save(request)
    }
}