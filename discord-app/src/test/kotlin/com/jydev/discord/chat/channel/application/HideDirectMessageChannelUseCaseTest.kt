package com.jydev.discord.chat.application

import com.jydev.discord.domain.chat.UserDirectMessageChannel
import com.jydev.discord.domain.chat.UserDirectMessageChannelRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.test.runTest

class HideDirectMessageChannelUseCaseTest : DescribeSpec({
    
    lateinit var userDirectMessageChannelRepository: UserDirectMessageChannelRepository
    lateinit var useCase: HideDirectMessageChannelUseCase
    
    beforeEach {
        userDirectMessageChannelRepository = mockk()
        useCase = HideDirectMessageChannelUseCase(userDirectMessageChannelRepository)
    }
    
    afterEach {
        clearAllMocks()
    }
    
    describe("invoke") {
        val userId = 1L
        val channelId = 100L
        
        context("채널 설정이 없는 경우") {
            beforeEach {
                coEvery { 
                    userDirectMessageChannelRepository.findByUserIdAndChannelId(userId, channelId) 
                } returns null
            }
            
            it("hide가 true면 숨김 상태로 새로 생성한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = true)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.apply {
                    this.userId shouldBe userId
                    this.channelId shouldBe channelId
                    this.isHidden shouldBe true
                }
            }
            
            it("hide가 false면 표시 상태로 새로 생성한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = false)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.apply {
                    this.userId shouldBe userId
                    this.channelId shouldBe channelId
                    this.isHidden shouldBe false
                }
            }
        }
        
        context("기존에 표시 상태인 채널이 있는 경우") {
            val existingChannel = UserDirectMessageChannel(
                userId = userId,
                channelId = channelId,
                isHidden = false
            )
            
            beforeEach {
                coEvery { 
                    userDirectMessageChannelRepository.findByUserIdAndChannelId(userId, channelId) 
                } returns existingChannel
            }
            
            it("hide가 true면 숨김 상태로 변경한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = true)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.isHidden shouldBe true
            }
            
            it("hide가 false면 표시 상태를 유지한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = false)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.isHidden shouldBe false
            }
        }
        
        context("기존에 숨김 상태인 채널이 있는 경우") {
            val existingChannel = UserDirectMessageChannel(
                userId = userId,
                channelId = channelId,
                isHidden = true
            )
            
            beforeEach {
                coEvery { 
                    userDirectMessageChannelRepository.findByUserIdAndChannelId(userId, channelId) 
                } returns existingChannel
            }
            
            it("hide가 true면 숨김 상태를 유지한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = true)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.isHidden shouldBe true
            }
            
            it("hide가 false면 표시 상태로 변경한다") {
                // given
                val capturedChannel = slot<UserDirectMessageChannel>()
                coEvery { 
                    userDirectMessageChannelRepository.save(capture(capturedChannel)) 
                } returns mockk()
                
                // when
                runTest {
                    useCase(userId, channelId, hide = false)
                }
                
                // then
                coVerify(exactly = 1) { 
                    userDirectMessageChannelRepository.save(any())
                }
                capturedChannel.captured.isHidden shouldBe false
            }
        }
    }
})