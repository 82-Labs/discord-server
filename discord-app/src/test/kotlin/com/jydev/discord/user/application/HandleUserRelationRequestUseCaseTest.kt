package com.jydev.discord.user.application

import com.jydev.discord.domain.user.relation.*
import com.jydev.discord.user.application.dto.HandleUserRelationRequestCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking

class HandleUserRelationRequestUseCaseTest : DescribeSpec({

    val userRelationRequestRepository = mockk<UserRelationRequestRepository>()
    val userRelationRepository = mockk<UserRelationRepository>()

    val handleUserRelationRequestUseCase = HandleUserRelationRequestUseCase(
        userRelationRequestRepository = userRelationRequestRepository,
        userRelationRepository = userRelationRepository
    )

    beforeTest {
        clearAllMocks()
    }

    describe("HandleUserRelationRequestUseCase") {
        context("친구 요청 수락") {
            it("수신자가 요청을 수락하면 양방향 친구 관계가 생성된다") {
                // Given
                val requestId = 1L
                val senderId = 10L
                val receiverId = 20L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = receiverId,
                    action = UserRelationRequestAction.ACCEPT
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                val relation1 = UserRelation(
                    target = RelationTarget.forRelation(senderId, receiverId),
                    relationType = UserRelationType.FRIEND
                )
                val relation2 = UserRelation(
                    target = RelationTarget.forRelation(receiverId, senderId),
                    relationType = UserRelationType.FRIEND
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request
                coEvery { userRelationRepository.saveAll(any<List<UserRelation>>()) } returns listOf(relation1, relation2)
                coEvery { userRelationRequestRepository.save(any()) } returns request

                // When
                runBlocking {
                    handleUserRelationRequestUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                    userRelationRepository.saveAll(match<List<UserRelation>> { 
                        it.size == 2
                    })
                    userRelationRequestRepository.save(match {
                        it.status == UserRelationRequestStatus.ACCEPTED
                    })
                }
            }

            it("수신자가 아닌 사용자가 수락하면 예외가 발생한다") {
                // Given
                val requestId = 1L
                val senderId = 10L
                val receiverId = 20L
                val wrongUserId = 30L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = wrongUserId,
                    action = UserRelationRequestAction.ACCEPT
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking {
                        handleUserRelationRequestUseCase(command)
                    }
                }

                exception.message shouldBe "해당 요청의 수신자가 아닙니다."

                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                }
                coVerify(exactly = 0) {
                    userRelationRepository.saveAll(any<List<UserRelation>>())
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("친구 요청 거절") {
            it("수신자가 요청을 거절하면 상태가 REJECTED로 변경된다") {
                // Given
                val requestId = 2L
                val senderId = 10L
                val receiverId = 20L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = receiverId,
                    action = UserRelationRequestAction.REJECT
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request
                coEvery { userRelationRequestRepository.save(any()) } returns request

                // When
                runBlocking {
                    handleUserRelationRequestUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                    userRelationRequestRepository.save(match {
                        it.status == UserRelationRequestStatus.REJECTED
                    })
                }
                coVerify(exactly = 0) {
                    userRelationRepository.saveAll(any<List<UserRelation>>())
                }
            }

            it("수신자가 아닌 사용자가 거절하면 예외가 발생한다") {
                // Given
                val requestId = 2L
                val senderId = 10L
                val receiverId = 20L
                val wrongUserId = 30L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = wrongUserId,
                    action = UserRelationRequestAction.REJECT
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking {
                        handleUserRelationRequestUseCase(command)
                    }
                }

                exception.message shouldBe "해당 요청의 수신자가 아닙니다."

                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                }
                coVerify(exactly = 0) {
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("친구 요청 취소") {
            it("발신자가 요청을 취소하면 상태가 CANCELED로 변경된다") {
                // Given
                val requestId = 3L
                val senderId = 10L
                val receiverId = 20L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = senderId,
                    action = UserRelationRequestAction.CANCEL
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request
                coEvery { userRelationRequestRepository.save(any()) } returns request

                // When
                runBlocking {
                    handleUserRelationRequestUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                    userRelationRequestRepository.save(match {
                        it.status == UserRelationRequestStatus.CANCELED
                    })
                }
                coVerify(exactly = 0) {
                    userRelationRepository.saveAll(any<List<UserRelation>>())
                }
            }

            it("발신자가 아닌 사용자가 취소하면 예외가 발생한다") {
                // Given
                val requestId = 3L
                val senderId = 10L
                val receiverId = 20L
                val wrongUserId = 30L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = wrongUserId,
                    action = UserRelationRequestAction.CANCEL
                )

                val request = UserRelationRequest(
                    id = requestId,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns request

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking {
                        handleUserRelationRequestUseCase(command)
                    }
                }

                exception.message shouldBe "해당 요청의 발신자가 아닙니다."

                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                }
                coVerify(exactly = 0) {
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("요청이 존재하지 않는 경우") {
            it("IllegalArgumentException이 발생한다") {
                // Given
                val requestId = 999L
                val command = HandleUserRelationRequestCommand(
                    requestId = requestId,
                    userId = 10L,
                    action = UserRelationRequestAction.ACCEPT
                )

                coEvery { userRelationRequestRepository.findById(requestId) } returns null

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking {
                        handleUserRelationRequestUseCase(command)
                    }
                }

                exception.message shouldBe "요청을 찾을 수 없습니다."

                coVerify(exactly = 1) {
                    userRelationRequestRepository.findById(requestId)
                }
                coVerify(exactly = 0) {
                    userRelationRepository.saveAll(any<List<UserRelation>>())
                    userRelationRequestRepository.save(any())
                }
            }
        }
    }
})