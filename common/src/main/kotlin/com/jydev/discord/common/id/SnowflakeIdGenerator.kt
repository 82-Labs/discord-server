package com.jydev.discord.common.id

import com.jydev.discord.common.time.CurrentTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

/**
 * Snowflake 알고리즘 기반 ID 생성기
 * 64bit ID 구조:
 * - 1bit: 부호 비트 (0)
 * - 41bit: timestamp (epoch 이후 밀리초)
 * - 10bit: machine ID (workerId + datacenterId)
 * - 12bit: sequence number
 */
@Component
class SnowflakeIdGenerator(
    @Value("\${snowflake.machine-id:1}") private val machineId: Long = 1L,
    private val currentTime: CurrentTime
) {
    companion object {
        // Service epoch: 2025-10-01 00:00:00 UTC
        private const val SERVICE_EPOCH = 1727740800000L
        
        // 비트 할당
        private const val MACHINE_ID_BITS = 10L
        private const val SEQUENCE_BITS = 12L
        
        private const val MAX_MACHINE_ID = (1L shl MACHINE_ID_BITS.toInt()) - 1L
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS.toInt()) - 1L
        
        private const val MACHINE_ID_SHIFT = SEQUENCE_BITS
        private const val TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS
    }
    
    init {
        require(machineId in 0..MAX_MACHINE_ID) {
            "Machine ID must be between 0 and $MAX_MACHINE_ID"
        }
    }
    
    private var lastTimestamp = -1L
    private val sequence = AtomicLong(0L)
    
    @Synchronized
    fun nextId(): Long {
        var timestamp = currentTime.millis()
        
        // 시계가 뒤로 갔을 경우 에러
        if (timestamp < lastTimestamp) {
            throw IllegalStateException(
                "Clock moved backwards. Refusing to generate id"
            )
        }
        
        // 같은 밀리초 내에서 호출된 경우
        if (timestamp == lastTimestamp) {
            val seq = sequence.incrementAndGet() and MAX_SEQUENCE
            sequence.set(seq)
            
            // 시퀀스가 overflow된 경우 다음 밀리초까지 대기
            if (seq == 0L) {
                timestamp = waitNextMillis(lastTimestamp)
            }
        } else {
            // 새로운 밀리초이므로 시퀀스 초기화
            sequence.set(0L)
        }
        
        lastTimestamp = timestamp
        
        // ID 생성
        return ((timestamp - SERVICE_EPOCH) shl TIMESTAMP_SHIFT.toInt()) or
                (machineId shl MACHINE_ID_SHIFT.toInt()) or
                sequence.get()
    }
    
    private fun waitNextMillis(lastTimestamp: Long): Long {
        var timestamp = currentTime.millis()
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime.millis()
        }
        return timestamp
    }
}