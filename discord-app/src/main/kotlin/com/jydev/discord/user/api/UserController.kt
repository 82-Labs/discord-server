package com.jydev.discord.user.api

import com.jydev.discord.domain.auth.AuthUser
import com.jydev.discord.domain.user.relation.UserRelationRequestAction
import com.jydev.discord.domain.user.relation.UserRelationType
import com.jydev.discord.security.CurrentUser
import com.jydev.discord.user.api.dto.*
import com.jydev.discord.user.application.*
import com.jydev.discord.user.application.dto.*
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*


private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val requestUserRelationUseCase: RequestUserRelationUseCase,
    private val handleUserRelationRequestUseCase: HandleUserRelationRequestUseCase,
    private val deleteUserRelationUseCase: DeleteUserRelationUseCase,
    private val userRelationRequestDao: UserRelationRequestDao,
    private val userRelationDao: UserRelationDao,
    private val userDao: UserDao
) : UserControllerDocs {

    @PostMapping("/register")
    override suspend fun registerUser(
        @CurrentUser authUser: AuthUser,
        @Valid @RequestBody request: RegisterUserApiRequest
    ): RegisterUserApiResponse {

        return when (authUser) {
            is AuthUser.User -> {
                logger.warn { "이미 가입된 유저 : $authUser" }
                throw IllegalArgumentException("이미 가입된 유저입니다. ${authUser.id}")
            }

            is AuthUser.TemporalUser -> {
                val command = RegisterUserCommand(
                    authCredentialId = authUser.authCredentialId,
                    username = request.username
                )

                val result = registerUserUseCase(command)

                RegisterUserApiResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken
                )
            }
        }
    }

    // ===== User Relation APIs =====

    @PostMapping("/relations/request")
    override suspend fun requestUserRelation(
        @CurrentUser authUser: AuthUser.User,
        @Valid @RequestBody request: RequestUserRelationApiRequest
    ) {
        val command = RequestUserRelationCommand(
            senderId = authUser.userId,
            receiverUsername = request.receiverUsername
        )
        requestUserRelationUseCase(command)
    }

    @PostMapping("/relations/requests/{action}")
    override suspend fun handleUserRelationRequest(
        @CurrentUser authUser: AuthUser.User,
        @PathVariable action: UserRelationRequestAction,
        @Valid @RequestBody request: HandleUserRelationRequestApiRequest
    ) {
        val command = HandleUserRelationRequestCommand(
            requestId = request.requestId,
            userId = authUser.userId,
            action = action
        )
        handleUserRelationRequestUseCase(command)
    }

    @GetMapping("/relations/requests/received")
    override suspend fun getReceivedRequests(
        @CurrentUser authUser: AuthUser.User
    ): ReceivedRequestsApiResponse {
        val requests = userRelationRequestDao.findPendingReceivedRequests(authUser.userId)
        val items = requests.map { readModel ->
            ReceivedRequestItem(
                id = readModel.id,
                senderId = readModel.userId,
                senderUsername = readModel.username.value,
                senderNickname = readModel.nickname.value
            )
        }
        return ReceivedRequestsApiResponse(content = items)
    }

    @GetMapping("/relations/requests/sent")
    override suspend fun getSentRequests(
        @CurrentUser authUser: AuthUser.User
    ): SentRequestsApiResponse {
        val requests = userRelationRequestDao.findPendingSentRequests(authUser.userId)
        val items = requests.map { readModel ->
            SentRequestItem(
                id = readModel.id,
                receiverId = readModel.userId,
                receiverUsername = readModel.username.value,
                receiverNickname = readModel.nickname.value
            )
        }
        return SentRequestsApiResponse(content = items)
    }

    @GetMapping("/relations/{type}")
    override suspend fun getUserRelations(
        @CurrentUser authUser: AuthUser.User,
        @PathVariable type: UserRelationType
    ): UserRelationsApiResponse {
        val relations = userRelationDao.findUserRelationsWithUserInfoByType(
            userId = authUser.userId,
            type = type
        )
        
        val items = relations.map { readModel ->
            UserRelationItem(
                id = readModel.id,
                userId = readModel.relatedUserId,
                username = readModel.relatedUsername.value,
                nickname = readModel.relatedNickname.value,
                type = readModel.relationType,
                status = readModel.relatedUserStatus
            )
        }
        return UserRelationsApiResponse(content = items)
    }
    
    @DeleteMapping("/relations/{targetUsername}")
    override suspend fun deleteUserRelation(
        @CurrentUser authUser: AuthUser.User,
        @PathVariable targetUsername: String
    ) {
        val command = DeleteUserRelationCommand(
            userId = authUser.userId,
            targetUsername = targetUsername
        )
        deleteUserRelationUseCase(command)
    }
    
    @GetMapping("/me")
    override suspend fun getMe(
        @CurrentUser authUser: AuthUser.User
    ): UserApiResponse {
        val user = userDao.findById(authUser.userId) 
            ?: throw IllegalStateException("사용자를 찾을 수 없습니다: ${authUser.userId}")
        
        return UserApiResponse(
            id = user.id,
            username = user.username.value,
            nickname = user.nickname.value,
            status = user.status
        )
    }
}