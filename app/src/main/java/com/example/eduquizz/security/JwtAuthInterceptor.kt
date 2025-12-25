package com.example.eduquizz.security

import android.util.Log
import com.example.eduquizz.dI.RefreshTokenClient
import com.example.eduquizz.features.auth.data.api.AuthApiService
import com.example.eduquizz.features.auth.data.api.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT Auth Interceptor
 * 
 * Tự động thêm Authorization header vào mọi request
 * và auto-refresh token khi hết hạn
 */
@Singleton
class JwtAuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authEventManager: AuthEventManager,
    @RefreshTokenClient private val refreshAuthApiService: AuthApiService  // Dùng client riêng cho refresh
) : Interceptor {
    
    companion object {
        private const val TAG = "JwtAuthInterceptor"
        
        // Các endpoints KHÔNG cần token
        private val publicEndpoints = listOf(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/verify-pin",
            "/api/auth/refresh-token"
        )
    }
    
    init {
        Log.d(TAG, "🚀🚀🚀 JwtAuthInterceptor CREATED! 🚀🚀🚀")
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        val fullUrl = originalRequest.url.toString()
        
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🌐 INTERCEPTOR CALLED: $requestPath")
        Log.d(TAG, "🌐 Full URL: $fullUrl")
        Log.d(TAG, "═══════════════════════════════════════")
        
        // Bỏ qua các public endpoints
        if (publicEndpoints.any { requestPath.contains(it) }) {
            Log.d(TAG, "📂 Public endpoint: $requestPath - skipping auth")
            return chain.proceed(originalRequest)
        }
        
        // Lấy access token
        var accessToken = tokenManager.getAccessToken()
        
        // Debug: Log token expiry info
        Log.d(TAG, "🔍 Token check - hasToken: ${accessToken != null}, isExpired: ${tokenManager.isAccessTokenExpired()}")
        
        // Kiểm tra token có hết hạn không
        if (accessToken != null && tokenManager.isAccessTokenExpired()) {
            Log.d(TAG, "⏰ Access token expired, attempting refresh...")
            
            // Thử refresh token
            accessToken = refreshToken()
            
            // Nếu refresh thất bại, emit event
            if (accessToken == null) {
                emitSessionExpiredEvent()
            }
        }
        
        // Nếu không có token cho protected endpoint, emit event và request sẽ fail
        if (accessToken.isNullOrBlank()) {
            Log.w(TAG, "⚠️ No access token available for protected endpoint: $requestPath")
            // Emit event để UI hiển thị dialog đăng nhập lại
            emitSessionExpiredEvent()
            return chain.proceed(originalRequest)
        }
        
        // Thêm Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        Log.d(TAG, "🔐 Added auth header to: $requestPath")
        
        // Thực hiện request
        val response = chain.proceed(authenticatedRequest)
        
        // Nếu bị 401 hoặc 403, thử refresh token và retry
        if ((response.code == 401 || response.code == 403) && !tokenManager.isRefreshTokenExpired()) {
            Log.d(TAG, "🔄 Got ${response.code}, attempting token refresh...")
            response.close()
            
            val newAccessToken = refreshToken()
            if (newAccessToken != null) {
                // Retry với token mới
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                Log.d(TAG, "🔄 Retrying with new token...")
                return chain.proceed(retryRequest)
            } else {
                // Refresh thất bại, emit event để thông báo user
                Log.e(TAG, "❌ Token refresh failed, session expired")
                emitSessionExpiredEvent()
            }
        }
        
        // Nếu vẫn bị 401/403 sau khi retry (không có refresh token hoặc refresh failed)
        if (response.code == 401 || response.code == 403) {
            Log.w(TAG, "🚫 Unauthorized/Forbidden - no valid refresh token")
            emitSessionExpiredEvent()
        }
        
        return response
    }
    
    /**
     * Emit session expired event để UI hiển thị dialog
     */
    private fun emitSessionExpiredEvent() {
        runBlocking {
            authEventManager.emitSessionExpired("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    private fun refreshToken(): String? {
        val refreshToken = tokenManager.getRefreshToken()
        
        Log.d(TAG, "🔄 === REFRESH TOKEN START ===")
        Log.d(TAG, "🔄 Refresh token available: ${!refreshToken.isNullOrBlank()}")
        Log.d(TAG, "🔄 Refresh token expired: ${tokenManager.isRefreshTokenExpired()}")
        
        if (refreshToken.isNullOrBlank()) {
            Log.w(TAG, "❌ No refresh token - user needs to login")
            return null
        }
        
        if (tokenManager.isRefreshTokenExpired()) {
            Log.w(TAG, "❌ Refresh token expired - user needs to login")
            tokenManager.clearTokens()
            return null
        }
        
        return try {
            // Gọi API refresh token đồng bộ (trong interceptor)
            runBlocking {
                Log.d(TAG, "🔄 Using RefreshTokenClient (no JWT interceptor)...")
                
                Log.d(TAG, "🔄 Calling refresh token API...")
                val response = refreshAuthApiService.refreshToken(RefreshTokenRequest(refreshToken))
                
                Log.d(TAG, "🔄 Response code: ${response.code()}")
                Log.d(TAG, "🔄 Response success: ${response.isSuccessful}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val newAccessToken = response.body()?.accessToken
                    val expiresIn = response.body()?.accessTokenExpiresIn ?: 900
                    
                    Log.d(TAG, "🔄 New access token received: ${!newAccessToken.isNullOrBlank()}")
                    Log.d(TAG, "🔄 Expires in: ${expiresIn}s")
                    
                    if (newAccessToken != null) {
                        tokenManager.updateAccessToken(newAccessToken, expiresIn)
                        Log.d(TAG, "✅ Token refreshed successfully!")
                        newAccessToken
                    } else {
                        Log.e(TAG, "❌ New access token is null in response")
                        null
                    }
                } else {
                    Log.e(TAG, "❌ Token refresh failed: ${response.code()} - ${response.message()}")
                    Log.e(TAG, "❌ Response body: ${response.body()}")
                    tokenManager.clearTokens()
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Token refresh error: ${e.message}", e)
            tokenManager.clearTokens()
            null
        }
    }
}

