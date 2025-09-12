package com.jydev.discord.common.transaction

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * 트랜잭션 실행을 위한 Advice 클래스
 * Spring AOP가 프록시를 통해 트랜잭션을 처리하도록 함
 */
@Component
class TxAdvice {
    
    @Transactional(propagation = Propagation.REQUIRED)
    suspend fun <T> write(block: suspend () -> T): T {
        return block()
    }
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
    suspend fun <T> readOnly(block: suspend () -> T): T {
        return block()
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    suspend fun <T> writeNew(block: suspend () -> T): T {
        return block()
    }
}

/**
 * 카카오페이 스타일의 트랜잭션 유틸리티
 * Companion object를 통해 전역적으로 사용 가능
 */
@Component
class Tx(
    _txAdvice: TxAdvice
) {
    init {
        txAdvice = _txAdvice
    }
    
    companion object {
        private lateinit var txAdvice: TxAdvice
        
        /**
         * 쓰기 트랜잭션 실행
         */
        suspend fun <T> write(block: suspend () -> T): T {
            return txAdvice.write(block)
        }
        
        /**
         * 읽기 전용 실행 (트랜잭션 없음)
         */
        suspend fun <T> readOnly(block: suspend () -> T): T {
            return txAdvice.readOnly(block)
        }
        
        /**
         * 새로운 트랜잭션으로 실행
         */
        suspend fun <T> writeNew(block: suspend () -> T): T {
            return txAdvice.writeNew(block)
        }
    }
}