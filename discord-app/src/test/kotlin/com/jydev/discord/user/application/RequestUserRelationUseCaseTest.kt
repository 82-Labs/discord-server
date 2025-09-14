package com.jydev.discord.user.application

import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.*
import com.jydev.discord.user.application.dto.RequestUserRelationCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.runBlocking

class RequestUserRelationUseCaseTest : DescribeSpec({

    val userRepository = mockk<UserRepository>()
    val userRelationRequestRepository = mockk<UserRelationRequestRepository>()
    val userRelationRepository = mockk<UserRelationRepository>()

    val requestUserRelationUseCase = RequestUserRelationUseCase(
        userRepository = userRepository,
        userRelationRequestRepository = userRelationRequestRepository,
        userRelationRepository = userRelationRepository
    )

    beforeTest {
        clearAllMocks()
    }

    describe("RequestUserRelationUseCase") {
        context("정상적인 친구 요청인 경우") {
            it("새로운 친구 요청을 생성한다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val newRequest = UserRelationRequest.create(senderId, receiverId)

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns null
                coEvery { userRelationRequestRepository.findByRequester(any()) } returns null
                coEvery { userRelationRequestRepository.save(any()) } returns newRequest

                // When
                runBlocking {
                    requestUserRelationUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(
                        RelationTarget.forRelation(senderId, receiverId)
                    )
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    )
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(receiverId, senderId)
                    )
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("수신자가 존재하지 않는 경우") {
            it("IllegalArgumentException을 발생시킨다") {
                // Given
                val senderId = 1L
                val receiverUsername = "nonexistent_user"
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns null

                // When & Then
                val exception = shouldThrow<IllegalArgumentException> {
                    runBlocking {
                        requestUserRelationUseCase(command)
                    }
                }

                exception.message shouldBe "수신자를 찾을 수 없습니다: $receiverUsername"

                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                }
                coVerify(exactly = 0) {
                    userRelationRepository.findByTarget(any())
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("이미 친구 관계인 경우") {
            it("IllegalStateException을 발생시킨다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val existingRelation = UserRelation(
                    id = 100L,
                    target = RelationTarget.forRelation(senderId, receiverId),
                    relationType = UserRelationType.FRIEND
                )

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns existingRelation

                // When & Then
                val exception = shouldThrow<IllegalStateException> {
                    runBlocking {
                        requestUserRelationUseCase(command)
                    }
                }

                exception.message shouldBe "이미 친구 관계입니다."

                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(
                        RelationTarget.forRelation(senderId, receiverId)
                    )
                }
                coVerify(exactly = 0) {
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("차단된 관계인 경우") {
            it("IllegalStateException을 발생시킨다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val blockedRelation = UserRelation(
                    id = 100L,
                    target = RelationTarget.forRelation(senderId, receiverId),
                    relationType = UserRelationType.BLOCKED
                )

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns blockedRelation

                // When & Then
                val exception = shouldThrow<IllegalStateException> {
                    runBlocking {
                        requestUserRelationUseCase(command)
                    }
                }

                exception.message shouldBe "차단된 사용자입니다."

                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(
                        RelationTarget.forRelation(senderId, receiverId)
                    )
                }
                coVerify(exactly = 0) {
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("이미 대기중인 요청이 있는 경우") {
            it("IllegalStateException을 발생시킨다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val existingRequest = UserRelationRequest(
                    id = 50L,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.PENDING
                )

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns null
                coEvery { 
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    ) 
                } returns existingRequest

                // When & Then
                val exception = shouldThrow<IllegalStateException> {
                    runBlocking {
                        requestUserRelationUseCase(command)
                    }
                }

                exception.message shouldBe "이미 대기중인 요청이 있습니다."

                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(any())
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    )
                }
                coVerify(exactly = 0) {
                    userRelationRequestRepository.save(any())
                }
            }
        }

        context("상대방이 이미 나에게 요청을 보낸 경우") {
            it("자동으로 양방향 친구 관계를 생성한다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val reverseRequest = UserRelationRequest(
                    id = 60L,
                    requester = RelationTarget.forRequest(receiverId, senderId),
                    status = UserRelationRequestStatus.PENDING
                )

                val relation1 = UserRelation(
                    target = RelationTarget.forRelation(receiverId, senderId),
                    relationType = UserRelationType.FRIEND
                )
                val relation2 = UserRelation(
                    target = RelationTarget.forRelation(senderId, receiverId),
                    relationType = UserRelationType.FRIEND
                )

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns null
                coEvery { 
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    ) 
                } returns null
                coEvery { 
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(receiverId, senderId)
                    ) 
                } returns reverseRequest
                coEvery { userRelationRepository.saveAll(any<List<UserRelation>>()) } returns listOf(relation1, relation2)
                coEvery { userRelationRequestRepository.save(reverseRequest) } returns reverseRequest

                // When
                runBlocking {
                    requestUserRelationUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(any())
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    )
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(receiverId, senderId)
                    )
                    userRelationRepository.saveAll(match<List<UserRelation>> { 
                        it.size == 2
                    })
                    userRelationRequestRepository.save(match {
                        it.status == UserRelationRequestStatus.ACCEPTED
                    })
                }
            }
        }

        context("거절된 요청이 있는 경우") {
            it("새로운 요청을 생성한다") {
                // Given
                val senderId = 1L
                val receiverUsername = "receiver123"
                val receiverId = 2L
                val command = RequestUserRelationCommand(
                    senderId = senderId,
                    receiverUsername = receiverUsername
                )

                val receiver = User.of(
                    userId = receiverId,
                    nickname = com.jydev.discord.domain.user.Nickname("receiver"),
                    username = Username(receiverUsername),
                    roles = listOf(UserRole.USER)
                )

                val rejectedRequest = UserRelationRequest(
                    id = 70L,
                    requester = RelationTarget.forRequest(senderId, receiverId),
                    status = UserRelationRequestStatus.REJECTED
                )

                val newRequest = UserRelationRequest.create(senderId, receiverId)

                coEvery { userRepository.findByUsername(Username(receiverUsername)) } returns receiver
                coEvery { userRelationRepository.findByTarget(any()) } returns null
                coEvery { 
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    ) 
                } returns rejectedRequest
                coEvery { 
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(receiverId, senderId)
                    ) 
                } returns null
                coEvery { userRelationRequestRepository.save(any()) } returns newRequest

                // When
                runBlocking {
                    requestUserRelationUseCase(command)
                }

                // Then
                coVerify(exactly = 1) {
                    userRepository.findByUsername(Username(receiverUsername))
                    userRelationRepository.findByTarget(any())
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(senderId, receiverId)
                    )
                    userRelationRequestRepository.findByRequester(
                        RelationTarget.forRequest(receiverId, senderId)
                    )
                    userRelationRequestRepository.save(any())
                }
            }
        }
    }
})