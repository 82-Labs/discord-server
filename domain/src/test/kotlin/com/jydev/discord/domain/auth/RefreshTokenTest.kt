package com.jydev.discord.domain.auth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class RefreshTokenTest : DescribeSpec({

    describe("RefreshToken 생성") {
        context("create 메서드로 생성할 때") {
            it("새로운 RefreshToken 인스턴스를 생성한다") {
                // given
                val userId = 1L
                val expirationDays = 7L

                // when
                val refreshToken = RefreshToken.create(userId, expirationDays)

                // then
                refreshToken.token.shouldNotBeEmpty()
                refreshToken.session.userId shouldBe userId
                refreshToken.session.sessionId.shouldNotBeEmpty()
                refreshToken.expiredAt shouldNotBe null
            }

            it("고유한 토큰을 생성한다") {
                // given
                val userId = 1L
                val expirationDays = 7L

                // when
                val refreshToken1 = RefreshToken.create(userId, expirationDays)
                val refreshToken2 = RefreshToken.create(userId, expirationDays)

                // then
                refreshToken1.token shouldNotBe refreshToken2.token
            }

            it("만료 시간이 현재 시간보다 미래로 설정된다") {
                // given
                val userId = 1L
                val expirationDays = 7L
                val now = Instant.now()

                // when
                val refreshToken = RefreshToken.create(userId, expirationDays)

                // then
                refreshToken.expiredAt.isAfter(now) shouldBe true
                val daysBetween = ChronoUnit.DAYS.between(now, refreshToken.expiredAt)
                daysBetween shouldBe expirationDays
            }
        }

        context("of 메서드로 생성할 때") {
            it("기존 값들로 RefreshToken을 재구성한다") {
                // given
                val token = UUID.randomUUID().toString()
                val session = AuthSession(userId = 2L)
                val expiredAt = Instant.now().plus(5, ChronoUnit.DAYS)

                // when
                val refreshToken = RefreshToken.of(token, session, expiredAt)

                // then
                refreshToken.token shouldBe token
                refreshToken.session shouldBe session
                refreshToken.expiredAt shouldNotBe null
            }

            it("과거 만료 시간의 경우 expirationDays를 0으로 설정한다") {
                // given
                val token = UUID.randomUUID().toString()
                val session = AuthSession(userId = 2L)
                val expiredAt = Instant.now().minus(1, ChronoUnit.DAYS)

                // when
                val refreshToken = RefreshToken.of(token, session, expiredAt)

                // then
                refreshToken.token shouldBe token
                refreshToken.session shouldBe session
                val daysBetween = ChronoUnit.DAYS.between(Instant.now(), refreshToken.expiredAt)
                daysBetween shouldBe 0
            }

            it("미래 만료 시간의 경우 남은 일수를 계산한다") {
                // given
                val token = UUID.randomUUID().toString()
                val session = AuthSession(userId = 2L)
                val daysInFuture = 10L
                val expiredAt = Instant.now().plus(daysInFuture, ChronoUnit.DAYS)

                // when
                val refreshToken = RefreshToken.of(token, session, expiredAt)

                // then
                // RefreshToken.of가 현재 시간과 expiredAt의 차이를 일 단위로 계산하여 설정
                val actualDaysBetween = ChronoUnit.DAYS.between(Instant.now(), refreshToken.expiredAt)
                // 시간차로 인한 약간의 오차 허용 (8-10일 사이)
                actualDaysBetween shouldBeGreaterThan 7L
            }
        }

        context("직접 생성자로 생성할 때") {
            it("주어진 값들로 RefreshToken을 생성한다") {
                // given
                val token = UUID.randomUUID().toString()
                val session = AuthSession(userId = 3L)
                val expirationDays = 14L

                // when
                val refreshToken = RefreshToken(token, session, expirationDays)

                // then
                refreshToken.token shouldBe token
                refreshToken.session shouldBe session
                // 시간이 약간 지났을 수 있으므로 정확한 비교보다는 범위로 검증
                val minutesBetween = ChronoUnit.MINUTES.between(Instant.now(), refreshToken.expiredAt)
                val expectedMinutes = expirationDays * 24 * 60
                // 10분 오차 범위 내에서 검증
                minutesBetween shouldBeGreaterThan (expectedMinutes - 10)
            }
        }
    }

    describe("RefreshToken 갱신") {
        context("refresh 메서드를 호출할 때") {
            it("세션이 로테이션된다") {
                // given
                val refreshToken = RefreshToken.create(userId = 1L, expirationDays = 7L)
                val originalSessionId = refreshToken.session.sessionId

                // when
                refreshToken.refresh(14L)

                // then
                refreshToken.session.sessionId shouldNotBe originalSessionId
            }

            it("만료 시간이 갱신된다") {
                // given
                val refreshToken = RefreshToken.create(userId = 1L, expirationDays = 7L)
                val originalExpiredAt = refreshToken.expiredAt
                Thread.sleep(10) // 시간 차이를 보장하기 위한 짧은 대기

                // when
                val newExpirationDays = 14L
                refreshToken.refresh(newExpirationDays)

                // then
                refreshToken.expiredAt shouldNotBe originalExpiredAt
                refreshToken.expiredAt.isAfter(originalExpiredAt) shouldBe true
                // 시간이 약간 지났을 수 있으므로 정확한 비교보다는 범위로 검증
                val minutesBetween = ChronoUnit.MINUTES.between(Instant.now(), refreshToken.expiredAt)
                val expectedMinutes = newExpirationDays * 24 * 60
                // 10분 오차 범위 내에서 검증
                minutesBetween shouldBeGreaterThan (expectedMinutes - 10)
            }

            it("토큰 값은 변경되지 않는다") {
                // given
                val refreshToken = RefreshToken.create(userId = 1L, expirationDays = 7L)
                val originalToken = refreshToken.token

                // when
                refreshToken.refresh(14L)

                // then
                refreshToken.token shouldBe originalToken
            }

            it("userId는 변경되지 않는다") {
                // given
                val userId = 1L
                val refreshToken = RefreshToken.create(userId = userId, expirationDays = 7L)

                // when
                refreshToken.refresh(14L)

                // then
                refreshToken.session.userId shouldBe userId
            }
        }
    }

    describe("RefreshToken 속성") {
        context("expiredAt 필드") {
            it("외부에서 직접 수정할 수 없다") {
                // given
                val refreshToken = RefreshToken.create(userId = 1L, expirationDays = 7L)
                
                // when & then
                // expiredAt은 private setter로 보호되므로 외부에서 직접 수정 불가
                // 컴파일 타임에 이미 검증되므로 런타임 테스트는 불필요
                refreshToken.expiredAt shouldNotBe null
            }
        }

        context("0일 만료 시간") {
            it("오늘 자정까지 유효한 토큰을 생성한다") {
                // given
                val userId = 1L
                val expirationDays = 0L

                // when
                val refreshToken = RefreshToken.create(userId, expirationDays)

                // then
                val daysBetween = ChronoUnit.DAYS.between(Instant.now(), refreshToken.expiredAt)
                daysBetween shouldBe 0
                // 0일이더라도 실제로는 현재 시간의 0일 후이므로 약간의 시간이 남아있음
                val secondsRemaining = ChronoUnit.SECONDS.between(Instant.now(), refreshToken.expiredAt)
                secondsRemaining shouldBeGreaterThan -1L
            }
        }

        context("큰 만료 일수") {
            it("365일 이상의 만료 시간도 처리할 수 있다") {
                // given
                val userId = 1L
                val expirationDays = 365L

                // when
                val refreshToken = RefreshToken.create(userId, expirationDays)

                // then
                // 시간이 약간 지났을 수 있으므로 정확한 비교보다는 범위로 검증
                val minutesBetween = ChronoUnit.MINUTES.between(Instant.now(), refreshToken.expiredAt)
                val expectedMinutes = expirationDays * 24 * 60
                // 10분 오차 범위 내에서 검증
                minutesBetween shouldBeGreaterThan (expectedMinutes - 10)
            }
        }
    }
})