package com.jydev.discord.test

import com.jydev.discord.domain.user.User
import com.jydev.discord.domain.user.UserRepository
import com.jydev.discord.domain.user.UserRole
import com.jydev.discord.domain.user.Username
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
    private val userRepository: UserRepository
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
}