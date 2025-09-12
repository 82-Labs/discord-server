package com.jydev.discord.auth.kakao.infra.external

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class KakaoAuthClient(
    private val kakaoAuthProperties: KakaoAuthProperties
) {

    companion object {
        private const val KAKAO_AUTH_BASE_URL = "https://kauth.kakao.com"
        private const val KAKAO_API_BASE_URL = "https://kapi.kakao.com"
        private const val TOKEN_REQUEST_PATH = "/oauth/token"
        private const val TOKEN_INFO_PATH = "/v1/user/access_token_info"
    }

    private val authWebClient: WebClient = WebClient.builder()
        .baseUrl(KAKAO_AUTH_BASE_URL)
        .build()
    
    private val apiWebClient: WebClient = WebClient.builder()
        .baseUrl(KAKAO_API_BASE_URL)
        .build()
    
    suspend fun authenticate(code: String): KakaoAuthResult {
        val tokenResponse = requestToken(code)
        val tokenInfo = getTokenInfo(tokenResponse.accessToken)
        
        return KakaoAuthResult(
            userId = tokenInfo.id,
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            expiresIn = tokenResponse.expiresIn
        )
    }
    
    private suspend fun requestToken(code: String): KakaoTokenResponse {
        val request = KakaoTokenRequest(
            clientId = kakaoAuthProperties.clientId,
            redirectUri = kakaoAuthProperties.redirectUri,
            code = code,
            clientSecret = kakaoAuthProperties.clientSecret
        )
        
        return try {
            authWebClient.post()
                .uri(TOKEN_REQUEST_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(request.toFormData())
                .retrieve()
                .bodyToMono(KakaoTokenResponse::class.java)
                .awaitSingle()
        } catch (e: WebClientResponseException) {
            throw RuntimeException("카카오 토큰 요청 실패: ${e.statusCode}", e)
        }
    }
    
    private suspend fun getTokenInfo(accessToken: String): KakaoTokenInfoResponse {
        return try {
            apiWebClient.get()
                .uri(TOKEN_INFO_PATH)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .bodyToMono(KakaoTokenInfoResponse::class.java)
                .awaitSingle()
        } catch (e: WebClientResponseException) {
            throw RuntimeException("카카오 토큰 정보 조회 실패: ${e.statusCode}", e)
        }
    }
    
    private data class KakaoTokenRequest(
        val grantType: String = "authorization_code",
        val clientId: String,
        val redirectUri: String,
        val code: String,
        val clientSecret: String? = null
    ) {
        fun toFormData(): MultiValueMap<String, String> {
            val formData = LinkedMultiValueMap<String, String>()
            formData.add("grant_type", grantType)
            formData.add("client_id", clientId)
            formData.add("redirect_uri", redirectUri)
            formData.add("code", code)
            clientSecret?.let { formData.add("client_secret", it) }
            return formData
        }
    }
    
    private data class KakaoTokenResponse(
        @JsonProperty("token_type")
        val tokenType: String,
        
        @JsonProperty("access_token")
        val accessToken: String,

        @JsonProperty("expires_in")
        val expiresIn: Int,
        
        @JsonProperty("refresh_token")
        val refreshToken: String,
        
        @JsonProperty("refresh_token_expires_in")
        val refreshTokenExpiresIn: Int,
        
        @JsonProperty("scope")
        val scope: String? = null
    )
    
    private data class KakaoTokenInfoResponse(
        val id: Long,
        
        @JsonProperty("expires_in")
        val expiresIn: Int,
        
        @JsonProperty("app_id")
        val appId: Int
    )
}