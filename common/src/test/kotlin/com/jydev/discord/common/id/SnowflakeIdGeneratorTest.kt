package com.jydev.discord.common.id

import com.jydev.discord.common.time.CurrentTime
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

class SnowflakeIdGeneratorTest : DescribeSpec({
    
    describe("SnowflakeIdGenerator") {
        val mockCurrentTime = mockk<CurrentTime>()
        val currentTimeMillis = AtomicLong(1727740800000L + 1000L) // SERVICE_EPOCH + 1초
        
        beforeEach {
            currentTimeMillis.set(1727740800000L + 1000L)
            every { mockCurrentTime.millis() } answers { currentTimeMillis.get() }
            every { mockCurrentTime.now() } answers { Instant.ofEpochMilli(currentTimeMillis.get()) }
        }
        
        context("ID 생성") {
            it("유니크한 ID를 생성해야 한다") {
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = mockCurrentTime)
                
                val id1 = generator.generateId()
                val id2 = generator.generateId()
                
                id1 shouldNotBe id2
                id2 shouldBeGreaterThan id1
            }
            
            it("올바른 구조의 ID를 생성해야 한다") {
                val generator = SnowflakeIdGenerator(machineId = 512L, currentTime = mockCurrentTime)
                
                val id = generator.generateId()
                
                // ID 분해
                val timestamp = (id shr 22) // timestamp 부분 추출
                val machineId = (id shr 12) and 0x3FF // machine ID 부분 추출 (10 bits)
                val sequence = id and 0xFFF // sequence 부분 추출 (12 bits)
                
                timestamp shouldBe 1000L // SERVICE_EPOCH로부터 1000ms 후
                machineId shouldBe 512L
                sequence shouldBe 0L // 첫 번째 생성이므로
            }
        }
        
        context("동일 밀리초 내 생성") {
            it("동일한 밀리초 내에서 순차적인 시퀀스를 생성해야 한다") {
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = mockCurrentTime)
                
                // 시간을 고정
                val fixedTime = currentTimeMillis.get()
                every { mockCurrentTime.millis() } returns fixedTime
                
                val ids = (1..100).map { generator.generateId() }
                
                // 모든 ID가 유니크해야 함
                ids.toSet().size shouldBe 100
                
                // 시퀀스 부분이 증가해야 함
                for (i in 1 until ids.size) {
                    val seq1 = ids[i - 1] and 0xFFF
                    val seq2 = ids[i] and 0xFFF
                    seq2 shouldBe (seq1 + 1)
                }
            }
            
            it("시퀀스가 오버플로우되면 다음 밀리초를 대기해야 한다") {
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = mockCurrentTime)
                
                // 시퀀스 최대값(4095)까지 생성
                val fixedTime = currentTimeMillis.get()
                var callCount = 0
                every { mockCurrentTime.millis() } answers {
                    callCount++
                    // 4096번째 호출 이후부터는 다음 밀리초 반환
                    if (callCount > 4096) fixedTime + 1 else fixedTime
                }
                
                // 4097개 생성 (시퀀스 오버플로우 발생)
                val ids = (1..4097).map { generator.generateId() }
                
                // 마지막 ID는 다음 밀리초의 ID여야 함
                val lastTimestamp = ids.last() shr 22
                val firstTimestamp = ids.first() shr 22
                lastTimestamp shouldBe (firstTimestamp + 1)
            }
        }
        
        context("시계 후진 처리") {
            it("시계가 뒤로 가면 예외를 발생시켜야 한다") {
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = mockCurrentTime)
                
                // 첫 ID 생성
                generator.generateId()
                
                // 시간을 뒤로 설정
                currentTimeMillis.addAndGet(-1000)
                
                shouldThrow<IllegalStateException> {
                    generator.generateId()
                }
            }
        }
        
        context("Machine ID 검증") {
            it("유효하지 않은 machine ID는 거부해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    SnowflakeIdGenerator(machineId = -1L, currentTime = mockCurrentTime)
                }
                
                shouldThrow<IllegalArgumentException> {
                    SnowflakeIdGenerator(machineId = 1024L, currentTime = mockCurrentTime) // MAX = 1023
                }
            }
            
            it("유효한 machine ID는 허용해야 한다") {
                SnowflakeIdGenerator(machineId = 0L, currentTime = mockCurrentTime)
                SnowflakeIdGenerator(machineId = 512L, currentTime = mockCurrentTime)
                SnowflakeIdGenerator(machineId = 1023L, currentTime = mockCurrentTime)
            }
        }
        
        context("동시성 테스트") {
            it("멀티 스레드에서 유니크한 ID를 생성해야 한다") {
                val realCurrentTime = object : CurrentTime {
                    override fun now(): Instant = Instant.now()
                    override fun millis(): Long = System.currentTimeMillis()
                }
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = realCurrentTime)
                
                val threadCount = 10
                val idsPerThread = 1000
                val allIds = ConcurrentHashMap.newKeySet<Long>()
                val latch = CountDownLatch(threadCount)
                
                val threads = (1..threadCount).map {
                    thread {
                        try {
                            repeat(idsPerThread) {
                                allIds.add(generator.generateId())
                            }
                        } finally {
                            latch.countDown()
                        }
                    }
                }
                
                latch.await(5, TimeUnit.SECONDS)
                
                // 모든 ID가 유니크해야 함
                allIds shouldHaveSize (threadCount * idsPerThread)
            }
            
            it("코루틴에서 유니크한 ID를 생성해야 한다") {
                val realCurrentTime = object : CurrentTime {
                    override fun now(): Instant = Instant.now()
                    override fun millis(): Long = System.currentTimeMillis()
                }
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = realCurrentTime)
                
                val coroutineCount = 100
                val idsPerCoroutine = 100
                
                val allIds = runBlocking {
                    (1..coroutineCount).map {
                        async {
                            (1..idsPerCoroutine).map { generator.generateId() }
                        }
                    }.awaitAll().flatten()
                }
                
                // 모든 ID가 유니크해야 함
                allIds.toSet().size shouldBe (coroutineCount * idsPerCoroutine)
            }
            
            it("다른 machine ID를 가진 생성기들은 충돌하지 않아야 한다") {
                val realCurrentTime = object : CurrentTime {
                    override fun now(): Instant = Instant.now()
                    override fun millis(): Long = System.currentTimeMillis()
                }
                
                val generator1 = SnowflakeIdGenerator(machineId = 1L, currentTime = realCurrentTime)
                val generator2 = SnowflakeIdGenerator(machineId = 2L, currentTime = realCurrentTime)
                
                val ids1 = (1..1000).map { generator1.generateId() }.toSet()
                val ids2 = (1..1000).map { generator2.generateId() }.toSet()
                
                // 두 생성기의 ID가 겹치지 않아야 함
                ids1.intersect(ids2).size shouldBe 0
                
                // 각 생성기별로 모든 ID가 유니크해야 함
                ids1 shouldHaveSize 1000
                ids2 shouldHaveSize 1000
            }
        }
        
        context("성능 테스트") {
            it("초당 최소 100,000개 이상의 ID를 생성할 수 있어야 한다") {
                val realCurrentTime = object : CurrentTime {
                    override fun now(): Instant = Instant.now()
                    override fun millis(): Long = System.currentTimeMillis()
                }
                val generator = SnowflakeIdGenerator(machineId = 1L, currentTime = realCurrentTime)
                
                val startTime = System.currentTimeMillis()
                val count = 100_000
                
                repeat(count) {
                    generator.generateId()
                }
                
                val elapsedTime = System.currentTimeMillis() - startTime
                
                // 1초(1000ms) 이내에 완료되어야 함
                elapsedTime shouldBeLessThan 1000L
            }
        }
    }
})