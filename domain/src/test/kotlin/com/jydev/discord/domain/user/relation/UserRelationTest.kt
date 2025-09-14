package com.jydev.discord.domain.user.relation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class UserRelationTest : DescribeSpec({

    describe("UserRelation 생성") {
        context("정상적인 관계") {
            it("서로 다른 사용자 간 친구 관계를 생성할 수 있다") {
                // given
                val userId = 1L
                val friendId = 2L

                // when
                val relation = UserRelation.createFriend(userId, friendId)

                // then
                relation.userId shouldBe userId
                relation.relatedUserId shouldBe friendId
                relation.relationType shouldBe UserRelationType.FRIEND
                relation.id shouldBe null
            }

            it("차단 관계를 생성할 수 있다") {
                // given
                val userId = 1L
                val blockedUserId = 2L

                // when
                val relation = UserRelation.createBlock(userId, blockedUserId)

                // then
                relation.userId shouldBe userId
                relation.relatedUserId shouldBe blockedUserId
                relation.relationType shouldBe UserRelationType.BLOCKED
            }

            it("생성자로 직접 관계를 생성할 수 있다") {
                // given
                val id = 100L
                val userId = 1L
                val relatedUserId = 2L

                // when
                val relation = UserRelation(id, RelationTarget.forRelation(userId, relatedUserId), UserRelationType.FRIEND)

                // then
                relation.id shouldBe id
                relation.userId shouldBe userId
                relation.relatedUserId shouldBe relatedUserId
                relation.relationType shouldBe UserRelationType.FRIEND
            }
        }

        context("잘못된 관계") {
            it("자기 자신과 관계를 맺을 수 없다") {
                // given
                val userId = 1L

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    UserRelation.createFriend(userId, userId)
                }
                exception.message shouldBe "자기 자신과 관계를 맺을 수 없습니다."
            }

            it("생성자로도 자기 자신과 관계를 맺을 수 없다") {
                // given
                val userId = 1L

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    UserRelation(null, RelationTarget.forRelation(userId, userId))
                }
                exception.message shouldBe "자기 자신과 관계를 맺을 수 없습니다."
            }

            it("차단 관계에서도 자기 자신을 차단할 수 없다") {
                // given
                val userId = 1L

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    UserRelation.createBlock(userId, userId)
                }
                exception.message shouldBe "자기 자신과 관계를 맺을 수 없습니다."
            }
        }
    }

    describe("양방향 친구 관계 생성") {
        context("createBidirectionalFriend 메서드") {
            it("양방향 친구 관계를 한 번에 생성할 수 있다") {
                // given
                val userId = 1L
                val friendId = 2L

                // when
                val (relation1, relation2) = UserRelation.createBidirectionalFriend(userId, friendId)

                // then
                relation1.userId shouldBe userId
                relation1.relatedUserId shouldBe friendId
                relation1.relationType shouldBe UserRelationType.FRIEND
                
                relation2.userId shouldBe friendId
                relation2.relatedUserId shouldBe userId
                relation2.relationType shouldBe UserRelationType.FRIEND
            }

            it("생성된 두 관계는 서로 다른 객체이다") {
                // given
                val userId = 1L
                val friendId = 2L

                // when
                val (relation1, relation2) = UserRelation.createBidirectionalFriend(userId, friendId)

                // then
                relation1 shouldNotBe relation2
            }

            it("양방향 생성 시에도 자기 자신과는 친구가 될 수 없다") {
                // given
                val userId = 1L

                // when & then
                val exception = shouldThrow<IllegalArgumentException> {
                    UserRelation.createBidirectionalFriend(userId, userId)
                }
                exception.message shouldBe "자기 자신과 관계를 맺을 수 없습니다."
            }
        }
    }

    describe("관계 타입") {
        it("친구 관계는 FRIEND 타입이다") {
            // given & when
            val relation = UserRelation.createFriend(1L, 2L)

            // then
            relation.relationType shouldBe UserRelationType.FRIEND
        }

        it("차단 관계는 BLOCKED 타입이다") {
            // given & when
            val relation = UserRelation.createBlock(1L, 2L)

            // then
            relation.relationType shouldBe UserRelationType.BLOCKED
        }
    }

    describe("관계의 대칭성") {
        it("A와 B의 친구 관계와 B와 A의 친구 관계는 다른 객체이다") {
            // given
            val userId = 1L
            val friendId = 2L

            // when
            val relationAB = UserRelation.createFriend(userId, friendId)
            val relationBA = UserRelation.createFriend(friendId, userId)

            // then
            relationAB.userId shouldBe relationBA.relatedUserId
            relationAB.relatedUserId shouldBe relationBA.userId
            relationAB shouldNotBe relationBA
        }

        it("차단 관계는 단방향이다") {
            // given
            val userId = 1L
            val blockedUserId = 2L

            // when
            val blockRelation = UserRelation.createBlock(userId, blockedUserId)

            // then
            blockRelation.userId shouldBe userId
            blockRelation.relatedUserId shouldBe blockedUserId
            blockRelation.relationType shouldBe UserRelationType.BLOCKED
            // 차단은 단방향이므로 역방향 관계는 생성하지 않음
        }
    }
})