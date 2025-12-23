package com.example.eduquizz.security

import android.util.Log
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
    private val tokenManager: TokenManager
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
    
    // AuthApiService sẽ được set sau để tránh circular dependency
    private var authApiService: AuthApiService? = null
    
    fun setAuthApiService(apiService: AuthApiService) {
        this.authApiService = apiService
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        
        // Bỏ qua các public endpoints
        if (publicEndpoints.any { requestPath.contains(it) }) {
            Log.d(TAG, "📂 Public endpoint: $requestPath - skipping auth")
            return chain.proceed(originalRequest)
        }
        
        // Lấy access token
        var accessToken = tokenManager.getAccessToken()
        
        // Kiểm tra token có hết hạn không
        if (accessToken != null && tokenManager.isAccessTokenExpired()) {
            Log.d(TAG, "⏰ Access token expired, attempting refresh...")
            
            // Thử refresh token
            accessToken = refreshToken()
        }
        
        // Nếu không có token, tiếp tục request (sẽ bị 401 từ server)
        if (accessToken.isNullOrBlank()) {
            Log.w(TAG, "⚠️ No access token available")
            return chain.proceed(originalRequest)
        }
        
        // Thêm Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        Log.d(TAG, "🔐 Added auth header to: $requestPath")
        
        // Thực hiện request
        val response = chain.proceed(authenticatedRequest)
        
        // Nếu bị 401, thử refresh token và retry
        if (response.code == 401 && !tokenManager.isRefreshTokenExpired()) {
            Log.d(TAG, "🔄 Got 401, attempting token refresh...")
            response.close()
            
            val newAccessToken = refreshToken()
            if (newAccessToken != null) {
                // Retry với token mới
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }
        
        return response
    }
    
    /**
     * Refresh access token using refresh token
     */
    private fun refreshToken(): String? {
        val refreshToken = tokenManager.getRefreshToken()
        
        if (refreshToken.isNullOrBlank() || tokenManager.isRefreshTokenExpired()) {
            Log.w(TAG, "❌ No valid refresh token - user needs to login")
            tokenManager.clearTokens()
            return null
        }
        
        return try {
            // Gọi API refresh token đồng bộ (trong interceptor)
            runBlocking {
                val apiService = authApiService
                if (apiService == null) {
                    Log.e(TAG, "❌ AuthApiService not available for refresh")
                    return@runBlocking null
                }
                
                val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val newAccessToken = response.body()?.accessToken
                    val expiresIn = response.body()?.accessTokenExpiresIn ?: 900
                    
                    if (newAccessToken != null) {
                        tokenManager.updateAccessToken(newAccessToken, expiresIn)
                        Log.d(TAG, "✅ Token refreshed successfully")
                        newAccessToken
                    } else {
                        null
                    }
                } else {
                    Log.e(TAG, "❌ Token refresh failed: ${response.message()}")
                    tokenManager.clearTokens()
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Token refresh error: ${e.message}")
            tokenManager.clearTokens()
            null
        }
    }
}
