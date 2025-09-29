package com.jydev.discord.chat.channel.application

import com.jydev.discord.chat.channel.application.dto.DirectMessageChannelReadModel
import com.jydev.discord.common.id.IdGenerator
import com.jydev.discord.domain.chat.DirectMessageChannel
import com.jydev.discord.domain.chat.DirectMessageChannelRepository
import com.jydev.discord.domain.chat.UserDirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannelRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest

class CreateOrGetDirectMessageChannelUseCaseTest : DescribeSpec({
    
    lateinit var directMessageChannelDao: DirectMessageChannelDao
    lateinit var directMessageChannelRepository: DirectMessageChannelRepository
    lateinit var userDirectMessageChannelRepository: UserDirectMessageChannelRepository
    lateinit var idGenerator: IdGenerator
    lateinit var useCase: CreateOrGetDirectMessageChannelUseCase
    
    beforeEach {
        directMessageChannelDao = mockk()
        directMessageChannelRepository = mockk()
        userDirectMessageChannelRepository = mockk()
        idGenerator = mockk()
        useCase = CreateOrGetDirectMessageChannelUseCase(
            directMessageChannelDao,
            directMessageChannelRepository,
            userDirectMessageChannelRepository,
            idGenerator
        )
    }
    
    afterEach {
        clearAllMocks()
    }
    
    describe("invoke") {
        val requesterId = 1L
        val otherUserIds = setOf(2L, 3L)
        val targetUserIds = otherUserIds + requesterId  // includes requester
        
        context("기존 채널이 없는 경우") {
            val newChannelId = 100L
            
            beforeEach {
                coEvery { directMessageChannelDao.findByUserIds(targetUserIds) } returns null
                every { idGenerator.generateId() } returns newChannelId
            }
            
            it("새 채널을 생성하고 요청자의 가시성을 설정한다") {
                // given
                val capturedChannel = slot<DirectMessageChannel>()
                val capturedUserChannel = slot<UserDirectMessageChannel>()
                
                coEvery { directMessageChannelRepository.save(capture(capturedChannel)) } answers {
                    firstArg()
                }
                coEvery { userDirectMessageChannelRepository.findByUserIdAndChannelId(requesterId, newChannelId) } returns null
                coEvery { userDirectMessageChannelRepository.save(capture(capturedUserChannel)) } returns mockk()
                
                // when & then
                runTest {
                    val result = useCase(requesterId, targetUserIds)
                    
                    result.channelId shouldBe newChannelId
                    result.userIds shouldBe targetUserIds
                    
                    // 새 채널이 생성되었는지 검증
                    capturedChannel.captured.apply {
                        id shouldBe newChannelId
                        userIds shouldBe targetUserIds
                    }
                    
                    // 요청자의 가시성이 설정되었는지 검증
                    capturedUserChannel.captured.apply {
                        userId shouldBe requesterId
                        channelId shouldBe newChannelId
                        isHidden shouldBe false
                    }
                }
                
                coVerify(exactly = 1) { 
                    directMessageChannelRepository.save(any())
                    userDirectMessageChannelRepository.save(any())
                }
            }
        }
        
        context("기존 채널이 있는 경우") {
            val existingChannelId = 200L
            val existingChannel = DirectMessageChannelReadModel(
                channelId = existingChannelId,
                userIds = targetUserIds,
                hidden = false
            )
            
            beforeEach {
                coEvery { directMessageChannelDao.findByUserIds(targetUserIds) } returns existingChannel
            }
            
            context("요청자의 채널 설정이 없는 경우") {
                beforeEach {
                    coEvery { 
                        userDirectMessageChannelRepository.findByUserIdAndChannelId(requesterId, existingChannelId) 
                    } returns null
                }
                
                it("요청자의 가시성 설정을 새로 생성한다") {
                    // given
                    val capturedUserChannel = slot<UserDirectMessageChannel>()
                    coEvery { userDirectMessageChannelRepository.save(capture(capturedUserChannel)) } returns mockk()
                    
                    // when & then
                    runTest {
                        val result = useCase(requesterId, targetUserIds)
                        result shouldBe existingChannel
                        
                        capturedUserChannel.captured.apply {
                            userId shouldBe requesterId
                            channelId shouldBe existingChannelId
                            isHidden shouldBe false
                        }
                    }
                    
                    coVerify(exactly = 0) { directMessageChannelRepository.save(any()) }
                    coVerify(exactly = 1) { userDirectMessageChannelRepository.save(any()) }
                }
            }
            
            context("요청자의 채널이 숨김 상태인 경우") {
                val hiddenUserChannel = UserDirectMessageChannel(
                    userId = requesterId,
                    channelId = existingChannelId,
                    isHidden = true
                )
                
                beforeEach {
                    coEvery { 
                        userDirectMessageChannelRepository.findByUserIdAndChannelId(requesterId, existingChannelId) 
                    } returns hiddenUserChannel
                }
                
                it("채널을 표시 상태로 변경한다") {
                    // given
                    val capturedUserChannel = slot<UserDirectMessageChannel>()
                    coEvery { userDirectMessageChannelRepository.save(capture(capturedUserChannel)) } returns mockk()
                    
                    // when & then
                    runTest {
                        val result = useCase(requesterId, targetUserIds)
                        result shouldBe existingChannel
                        
                        capturedUserChannel.captured.apply {
                            userId shouldBe requesterId
                            channelId shouldBe existingChannelId
                            isHidden shouldBe false
                        }
                    }
                    
                    coVerify(exactly = 0) { directMessageChannelRepository.save(any()) }
                    coVerify(exactly = 1) { userDirectMessageChannelRepository.save(any()) }
                }
            }
            
            context("요청자의 채널이 이미 표시 상태인 경우") {
                val visibleUserChannel = UserDirectMessageChannel(
                    userId = requesterId,
                    channelId = existingChannelId,
                    isHidden = false
                )
                
                beforeEach {
                    coEvery { 
                        userDirectMessageChannelRepository.findByUserIdAndChannelId(requesterId, existingChannelId) 
                    } returns visibleUserChannel
                }
                
                it("아무 작업도 하지 않는다") {
                    // given
                    coEvery { userDirectMessageChannelRepository.save(any()) } returns mockk()
                    
                    // when & then
                    runTest {
                        val result = useCase(requesterId, targetUserIds)
                        result shouldBe existingChannel
                    }
                    
                    coVerify(exactly = 0) { 
                        directMessageChannelRepository.save(any())
                        userDirectMessageChannelRepository.save(any())
                    }
                }
            }
        }
        
        context("사용자 ID 조합") {
            it("요청자 ID가 targetUserIds에 포함되어도 중복 없이 처리된다") {
                // given
                val duplicateTargetUserIds = setOf(1L, 2L, 3L) // requesterId가 이미 포함됨
                val expectedAllUserIds = setOf(1L, 2L, 3L)
                
                coEvery { directMessageChannelDao.findByUserIds(expectedAllUserIds) } returns null
                every { idGenerator.generateId() } returns 300L
                coEvery { directMessageChannelRepository.save(any()) } answers { firstArg() }
                coEvery { userDirectMessageChannelRepository.findByUserIdAndChannelId(any(), any()) } returns null
                coEvery { userDirectMessageChannelRepository.save(any()) } returns mockk()
                
                // when & then
                runTest {
                    val result = useCase(requesterId, duplicateTargetUserIds)
                    result.userIds shouldBe expectedAllUserIds
                }
                
                coVerify(exactly = 1) { 
                    directMessageChannelDao.findByUserIds(expectedAllUserIds)
                }
            }
        }
    }
})