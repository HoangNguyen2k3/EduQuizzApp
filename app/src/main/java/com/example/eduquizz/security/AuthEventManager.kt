package com.example.eduquizz.security

import android.util.Log
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
    
    companion object {
        private const val TAG = "AuthEventManager"
        private const val DEBOUNCE_MS = 5000L // 5 giây debounce
    }
    
    private val _authEvents = MutableSharedFlow<AuthEvent>(
        replay = 1,  // Replay 1 để đảm bảo event không bị miss
        extraBufferCapacity = 1
    )
    val authEvents: SharedFlow<AuthEvent> = _authEvents.asSharedFlow()
    
    // Track last emit time để debounce
    private var lastEventTime: Long = 0
    private var lastEventType: String = ""
    
    /**
     * Phát sự kiện session expired
     */
    suspend fun emitSessionExpired(reason: String = "Phiên đăng nhập đã hết hạn") {
        if (shouldEmit("SessionExpired")) {
            Log.d(TAG, "📢 Emitting SessionExpired: $reason")
            _authEvents.emit(AuthEvent.SessionExpired(reason))
        }
    }
    
    /**
     * Phát sự kiện token refresh failed
     */
    suspend fun emitTokenRefreshFailed() {
        if (shouldEmit("TokenRefreshFailed")) {
            Log.d(TAG, "📢 Emitting TokenRefreshFailed")
            _authEvents.emit(AuthEvent.TokenRefreshFailed)
        }
    }
    
    /**
     * Phát sự kiện unauthorized (401/403)
     */
    suspend fun emitUnauthorized() {
        if (shouldEmit("Unauthorized")) {
            Log.d(TAG, "📢 Emitting Unauthorized")
            _authEvents.emit(AuthEvent.Unauthorized)
        }
    }
    
    /**
     * Phát sự kiện force logout
     */
    suspend fun emitForceLogout(reason: String = "Bạn đã bị đăng xuất") {
        if (shouldEmit("ForceLogout")) {
            Log.d(TAG, "📢 Emitting ForceLogout: $reason")
            _authEvents.emit(AuthEvent.ForceLogout(reason))
        }
    }
    
    /**
     * Check if should emit event (debounce logic)
     */
    @Synchronized
    private fun shouldEmit(eventType: String): Boolean {
        val now = System.currentTimeMillis()
        
        // Nếu cùng loại event và trong thời gian debounce, bỏ qua
        if (eventType == lastEventType && (now - lastEventTime) < DEBOUNCE_MS) {
            Log.d(TAG, "⏳ Debounced $eventType (${now - lastEventTime}ms since last)")
            return false
        }
        
        lastEventTime = now
        lastEventType = eventType
        return true
    }
    
    /**
     * Reset debounce (gọi sau khi user đã xử lý event)
     */
    fun resetDebounce() {
        lastEventTime = 0
        lastEventType = ""
        Log.d(TAG, "🔄 Debounce reset")
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

