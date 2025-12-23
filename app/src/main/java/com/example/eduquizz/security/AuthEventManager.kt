package com.example.eduquizz.security

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthEventManager - Quản lý các sự kiện authentication
 * 
 * Sử dụng SharedFlow để phát sự kiện đến UI khi:
 * - Session hết hạn
 * - Cần login lại
 * - Token refresh thất bại
 */
@Singleton
class AuthEventManager @Inject constructor() {
    
    private val _authEvents = MutableSharedFlow<AuthEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()
    
    /**
     * Phát sự kiện session expired
     */
    suspend fun emitSessionExpired(reason: String = "Phiên đăng nhập đã hết hạn") {
        _authEvents.emit(AuthEvent.SessionExpired(reason))
    }
    
    /**
     * Phát sự kiện token refresh failed
     */
    suspend fun emitTokenRefreshFailed() {
        _authEvents.emit(AuthEvent.TokenRefreshFailed)
    }
    
    /**
     * Phát sự kiện unauthorized (401/403)
     */
    suspend fun emitUnauthorized() {
        _authEvents.emit(AuthEvent.Unauthorized)
    }
    
    /**
     * Phát sự kiện force logout
     */
    suspend fun emitForceLogout(reason: String = "Bạn đã bị đăng xuất") {
        _authEvents.emit(AuthEvent.ForceLogout(reason))
    }
}

/**
 * Sealed class cho các sự kiện authentication
 */
sealed class AuthEvent {
    data class SessionExpired(val message: String) : AuthEvent()
    object TokenRefreshFailed : AuthEvent()
    object Unauthorized : AuthEvent()
    data class ForceLogout(val reason: String) : AuthEvent()
}
