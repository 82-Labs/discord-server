package com.jydev.discord.domain.user.relation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class UserRelationRequestTest : DescribeSpec({

    describe("UserRelationRequest 생성") {
        context("정상적인 관계 요청") {
            it("서로 다른 사용자 간 관계 요청을 생성할 수 있다") {
                // given
                val senderId = 1L
                val receiverId = 2L

                // when
                val relationRequest = UserRelationRequest.create(senderId, receiverId)

                // then
                relationRequest.senderId shouldBe senderId
                relationRequest.receiverId shouldBe receiverId
                relationRequest.status shouldBe UserRelationRequestStatus.PENDING
                relationRequest.isPending() shouldBe true
            }
        }

        context("잘못된 관계 요청") {
            it("자기 자신에게 관계 요청을 보낼 수 없다") {
                // given
                val userId = 1L

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    UserRelationRequest.create(userId, userId)
                }
                exception.message shouldBe "자기 자신에게 관계 요청을 보낼 수 없습니다."
            }
        }
    }

    describe("관계 요청 수락") {
        context("대기중인 요청") {
            it("대기중인 요청을 수락하면 양방향 UserRelation이 생성된다") {
                // given
                val senderId = 1L
                val receiverId = 2L
                val relationRequest = UserRelationRequest.create(senderId, receiverId)

                // when
                val (relation1, relation2) = relationRequest.accept()

                // then
                relationRequest.status shouldBe UserRelationRequestStatus.ACCEPTED
                relationRequest.isAccepted() shouldBe true
                relationRequest.isPending() shouldBe false
                
                // UserRelation 검증
                relation1.userId shouldBe senderId
                relation1.relatedUserId shouldBe receiverId
                relation1.relationType shouldBe UserRelationType.FRIEND
                
                relation2.userId shouldBe receiverId
                relation2.relatedUserId shouldBe senderId
                relation2.relationType shouldBe UserRelationType.FRIEND
            }
        }

        context("대기중이 아닌 요청") {
            it("이미 수락된 요청은 다시 수락할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.accept()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.accept()
                }
                exception.message shouldBe "대기중인 요청만 수락할 수 있습니다."
            }

            it("거절된 요청은 수락할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.reject()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.accept()
                }
                exception.message shouldBe "대기중인 요청만 수락할 수 있습니다."
            }

            it("취소된 요청은 수락할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.cancel()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.accept()
                }
                exception.message shouldBe "대기중인 요청만 수락할 수 있습니다."
            }
        }
    }

    describe("관계 요청 거절") {
        context("대기중인 요청") {
            it("대기중인 요청을 거절할 수 있다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)

                // when
                relationRequest.reject()

                // then
                relationRequest.status shouldBe UserRelationRequestStatus.REJECTED
                relationRequest.isPending() shouldBe false
                relationRequest.isAccepted() shouldBe false
            }
        }

        context("대기중이 아닌 요청") {
            it("이미 거절된 요청은 다시 거절할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.reject()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.reject()
                }
                exception.message shouldBe "대기중인 요청만 거절할 수 있습니다."
            }

            it("수락된 요청은 거절할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.accept()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.reject()
                }
                exception.message shouldBe "대기중인 요청만 거절할 수 있습니다."
            }
        }
    }

    describe("관계 요청 취소") {
        context("대기중인 요청") {
            it("대기중인 요청을 취소할 수 있다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)

                // when
                relationRequest.cancel()

                // then
                relationRequest.status shouldBe UserRelationRequestStatus.CANCELED
                relationRequest.isPending() shouldBe false
                relationRequest.isAccepted() shouldBe false
            }
        }

        context("대기중이 아닌 요청") {
            it("이미 취소된 요청은 다시 취소할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.cancel()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.cancel()
                }
                exception.message shouldBe "대기중인 요청만 취소할 수 있습니다."
            }

            it("수락된 요청은 취소할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.accept()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.cancel()
                }
                exception.message shouldBe "대기중인 요청만 취소할 수 있습니다."
            }

            it("거절된 요청은 취소할 수 없다") {
                // given
                val relationRequest = UserRelationRequest.create(1L, 2L)
                relationRequest.reject()

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    relationRequest.cancel()
                }
                exception.message shouldBe "대기중인 요청만 취소할 수 있습니다."
            }
        }
    }

    describe("상태 체크 메서드") {
        it("isPending()은 PENDING 상태일 때만 true를 반환한다") {
            // given
            val relationRequest = UserRelationRequest.create(1L, 2L)

            // then
            relationRequest.isPending() shouldBe true

            // when
            relationRequest.accept()

            // then
            relationRequest.isPending() shouldBe false
        }

        it("isAccepted()는 ACCEPTED 상태일 때만 true를 반환한다") {
            // given
            val relationRequest = UserRelationRequest.create(1L, 2L)

            // then
            relationRequest.isAccepted() shouldBe false

            // when
            relationRequest.accept()

            // then
            relationRequest.isAccepted() shouldBe true
        }
    }
})