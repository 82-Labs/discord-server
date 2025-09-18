package com.jydev.discord.test

import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationRequestRepository
import com.jydev.discord.user.application.DeleteUserRelationUseCase
import com.jydev.discord.user.application.HandleUserRelationRequestUseCase
import com.jydev.discord.user.application.RequestUserRelationUseCase
import com.jydev.discord.user.application.dto.DeleteUserRelationCommand
import com.jydev.discord.user.application.dto.HandleUserRelationRequestCommand
import com.jydev.discord.user.application.dto.RequestUserRelationCommand
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/test")
@Profile("local")
@Tag(name = "Test", description = "로컬 환경 테스트용 API")
class TestController(
    private val userRepository: UserRepository,
    private val requestUserRelationUseCase: RequestUserRelationUseCase,
    private val handleUserRelationRequestUseCase: HandleUserRelationRequestUseCase,
    private val userRelationRequestRepository: UserRelationRequestRepository
) {

    @PostMapping("/users")
    @Operation(
        summary = "테스트 사용자 생성",
        description = """
            로컬 환경에서만 사용 가능한 테스트용 사용자 생성 API입니다.
            인증 없이 username만으로 사용자를 생성할 수 있습니다.
            
            **주의사항:**
            - 프로덕션 환경에서는 사용할 수 없습니다
            - 인증 절차 없이 바로 사용자가 생성됩니다
            - username이 중복될 경우 에러가 발생합니다
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "사용자 생성 성공",
                content = [Content(schema = Schema(implementation = TestUserResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (username 중복 등)"
            )
        ]
    )
    suspend fun createTestUser(@RequestBody request: CreateTestUserRequest): TestUserResponse {
        val user = User.create(
            username = Username(request.username),
            roles = listOf(UserRole.USER),
            checkDuplicate = userRepository::existsByUsername
        ).let {
            userRepository.save(it)
        }

        return TestUserResponse(
            id = user.id!!,
            username = user.username.value
        )
    }

    @PostMapping("/relations/request")
    @Operation(
        summary = "테스트 친구 요청 전송",
        description = """
            테스트용 친구 요청 전송 API입니다.
            sender와 receiver의 username을 직접 지정할 수 있습니다.
            
            **주의사항:**
            - 두 사용자가 모두 존재해야 합니다
            - 이미 친구 관계인 경우 에러가 발생합니다
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "친구 요청 전송 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    suspend fun sendTestRelationRequest(@RequestBody request: TestRelationRequest) {
        val sender = userRepository.findByUsername(Username(request.senderUsername))
            ?: throw IllegalArgumentException("발신자를 찾을 수 없습니다: ${request.senderUsername}")
        
        val command = RequestUserRelationCommand(
            senderId = sender.id!!,
            receiverUsername = request.receiverUsername
        )
        requestUserRelationUseCase(command)
    }

    @PostMapping("/relations/requests/{action}")
    @Operation(
        summary = "테스트 친구 요청 처리",
        description = """
            테스트용 친구 요청 처리 API입니다.
            username으로 사용자를 지정하여 요청을 처리할 수 있습니다.
            
            **액션 타입:**
            - ACCEPT: 요청 수락 (receiver가 처리)
            - REJECT: 요청 거절 (receiver가 처리)
            - CANCEL: 요청 취소 (sender가 처리)
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "요청 처리 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청")
        ]
    )
    suspend fun handleTestRelationRequest(
        @PathVariable action: UserRelationRequestAction,
        @RequestBody request: TestHandleRelationRequest
    ) {
        val user = userRepository.findByUsername(Username(request.username))
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: ${request.username}")
        
        userRelationRequestRepository.findById(request.requestId)
            ?: throw IllegalArgumentException("요청을 찾을 수 없습니다: ${request.requestId}")
        
        val command = HandleUserRelationRequestCommand(
            requestId = request.requestId,
            userId = user.id!!,
            action = action
        )
        handleUserRelationRequestUseCase(command)
    }

    @Schema(description = "테스트 사용자 생성 요청")
    data class CreateTestUserRequest(
        @field:Schema(description = "사용자명", example = "testuser123", required = true)
        val username: String
    )

    @Schema(description = "테스트 사용자 생성 응답")
    data class TestUserResponse(
        @field:Schema(description = "사용자 ID", example = "1")
        val id: Long,
        @field:Schema(description = "사용자명", example = "testuser123")
        val username: String
    )

    @Schema(description = "테스트 친구 요청")
    data class TestRelationRequest(
        @field:Schema(description = "발신자 username", example = "sender123", required = true)
        val senderUsername: String,
        @field:Schema(description = "수신자 username", example = "receiver456", required = true)
        val receiverUsername: String
    )


    @Schema(description = "테스트 친구 요청 처리")
    data class TestHandleRelationRequest(
        @field:Schema(description = "요청 ID", example = "1", required = true)
        val requestId: Long,
        @field:Schema(description = "처리하는 사용자의 username", example = "user123", required = true)
        val username: String
    )
}