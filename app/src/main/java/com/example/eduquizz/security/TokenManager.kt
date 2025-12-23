package com.example.eduquizz.security

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TokenManager - Quản lý JWT tokens an toàn
 * 
 * Sử dụng EncryptedSharedPreferences để lưu trữ mã hóa:
 * - Access Token: 15 phút
 * - Refresh Token: 7 ngày
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "jwt_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ACCESS_EXPIRY = "access_token_expiry"
        private const val KEY_REFRESH_EXPIRY = "refresh_token_expiry"
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    /**
     * Lưu cả 2 tokens sau khi login thành công
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accessExpiresInSeconds: Long = 15 * 60,      // 15 phút
        refreshExpiresInSeconds: Long = 7 * 24 * 60 * 60 // 7 ngày
    ) {
        val now = System.currentTimeMillis()
        securePrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_ACCESS_EXPIRY, now + accessExpiresInSeconds * 1000)
            .putLong(KEY_REFRESH_EXPIRY, now + refreshExpiresInSeconds * 1000)
            .apply()
        
        Log.d(TAG, "✅ Tokens saved successfully")
    }
    
    /**
     * Lấy Access Token
     */
    fun getAccessToken(): String? {
        return securePrefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Lấy Refresh Token
     */
    fun getRefreshToken(): String? {
        return securePrefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Kiểm tra Access Token còn hạn không
     */
    fun isAccessTokenExpired(): Boolean {
        val expiry = securePrefs.getLong(KEY_ACCESS_EXPIRY, 0)
        val isExpired = System.currentTimeMillis() >= expiry
        if (isExpired) {
            Log.d(TAG, "⏰ Access token expired")
        }
        return isExpired
    }
    
    /**
     * Kiểm tra Refresh Token còn hạn không
     */
    fun isRefreshTokenExpired(): Boolean {
        val expiry = securePrefs.getLong(KEY_REFRESH_EXPIRY, 0)
        val isExpired = System.currentTimeMillis() >= expiry
        if (isExpired) {
            Log.d(TAG, "⏰ Refresh token expired - user needs to login again")
        }
        return isExpired
    }
    
    /**
     * Kiểm tra có token hợp lệ không
     */
    fun hasValidTokens(): Boolean {
        val hasAccess = !getAccessToken().isNullOrBlank()
        val hasRefresh = !getRefreshToken().isNullOrBlank()
        return hasAccess && hasRefresh && !isRefreshTokenExpired()
    }
    
    /**
     * Cập nhật Access Token mới (sau khi refresh)
     */
    fun updateAccessToken(newAccessToken: String, expiresInSeconds: Long = 15 * 60) {
        val now = System.currentTimeMillis()
        securePrefs.edit()
            .putString(KEY_ACCESS_TOKEN, newAccessToken)
            .putLong(KEY_ACCESS_EXPIRY, now + expiresInSeconds * 1000)
            .apply()
        
        Log.d(TAG, "🔄 Access token refreshed")
    }
    
    /**
     * Xóa tất cả tokens (logout)
     */
    fun clearTokens() {
        securePrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_ACCESS_EXPIRY)
            .remove(KEY_REFRESH_EXPIRY)
            .apply()
        
        Log.d(TAG, "🗑️ All tokens cleared")
    }
    
    /**
     * Lấy thời gian còn lại của Access Token (milliseconds)
     */
    fun getAccessTokenRemainingTime(): Long {
        val expiry = securePrefs.getLong(KEY_ACCESS_EXPIRY, 0)
        return maxOf(0, expiry - System.currentTimeMillis())
    }
}
