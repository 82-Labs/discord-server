package com.jydev.discord.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * 컨트롤러 메소드 파라미터에 현재 인증된 사용자 주입
 * 
 * 사용 예시:
 * ```
 * @GetMapping("/me")
 * suspend fun getMyInfo(@CurrentUser authUser: AuthUser): UserInfo {
 *     // authUser 사용
 * }
 * ```
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal
annotation class CurrentUser