package com.jydev.discord.user.application

import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.RelationTarget
import com.jydev.discord.domain.user.relation.UserRelationRepository
import com.jydev.discord.domain.user.relation.UserRelationType
import com.jydev.discord.user.application.dto.DeleteUserRelationCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteUserRelationUseCase(
    private val userRepository: UserRepository,
    private val userRelationRepository: UserRelationRepository
) {
    
    @Transactional
    suspend operator fun invoke(command: DeleteUserRelationCommand) {
        val userId = command.userId
        val targetUsername = command.targetUsername
        
        // 1. 대상 사용자 조회
        val targetUser = userRepository.findByUsername(Username(targetUsername))
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $targetUsername")
        
        val targetUserId = targetUser.id!!
        
        // 2. 관계가 존재하는지 확인
        val relation = userRelationRepository.findByTarget(
            RelationTarget.forRelation(userId, targetUserId)
        ) ?: throw IllegalArgumentException("관계가 존재하지 않습니다.")
        
        // 3. 친구 관계인지 확인
        if (relation.relationType != UserRelationType.FRIEND) {
            throw IllegalStateException("친구 관계가 아닙니다.")
        }
        
        // 4. 양방향 관계 삭제
        userRelationRepository.deleteByTarget(
            RelationTarget.forRelation(userId, targetUserId)
        )
        userRelationRepository.deleteByTarget(
            RelationTarget.forRelation(targetUserId, userId)
        )
    }
}