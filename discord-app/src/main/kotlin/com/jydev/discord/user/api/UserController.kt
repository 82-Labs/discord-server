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
    private val userRelationRequestDao: UserRelationRequestDao,
    private val userRelationDao: UserRelationDao
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
    ): List<ReceivedRequestApiResponse> {
        val requests = userRelationRequestDao.findPendingReceivedRequests(authUser.userId)
        return requests.map { readModel ->
            ReceivedRequestApiResponse(
                id = readModel.id,
                senderId = readModel.senderId,
                senderUsername = readModel.senderUsername.value,
                senderNickname = readModel.senderNickname.value
            )
        }
    }

    @GetMapping("/relations/{type}")
    override suspend fun getUserRelations(
        @CurrentUser authUser: AuthUser.User,
        @PathVariable type: UserRelationType
    ): List<UserRelationApiResponse> {
        val relations = userRelationDao.findUserRelationsWithUserInfoByType(
            userId = authUser.userId,
            type = type
        )
        
        return relations.map { readModel ->
            UserRelationApiResponse(
                id = readModel.id,
                userId = readModel.relatedUserId,
                username = readModel.relatedUsername.value,
                nickname = readModel.relatedNickname.value,
                type = readModel.relationType
            )
        }
    }
}