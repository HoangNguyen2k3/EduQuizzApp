package com.example.eduquizz.security

import android.util.Log

/**
 * Rate Limiter để chống brute-force attack
 * 
 * Sử dụng sliding window algorithm để track số lần thử
 * trong một khoảng thời gian nhất định
 */
class RateLimiter(
    private val maxAttempts: Int = 5,
    private val windowMs: Long = 60_000 // 1 phút
) {
    companion object {
        private const val TAG = "RateLimiter"
        
        // Singleton instances cho các use-case khác nhau
        val loginLimiter = RateLimiter(maxAttempts = 5, windowMs = 60_000)  // 5 lần/phút
        val otpLimiter = RateLimiter(maxAttempts = 3, windowMs = 60_000)     // 3 lần/phút
        val chatLimiter = RateLimiter(maxAttempts = 30, windowMs = 60_000)   // 30 tin nhắn/phút
        val apiLimiter = RateLimiter(maxAttempts = 100, windowMs = 60_000)   // 100 requests/phút
    }

    // Map lưu trữ các lần thử cho mỗi key (username, IP, etc.)
    private val attempts = mutableMapOf<String, MutableList<Long>>()
    
    // Map lưu trữ thời gian block cho mỗi key
    private val blockedUntil = mutableMapOf<String, Long>()
    
    // Thời gian block khi vượt quá giới hạn (30 phút)
    private val blockDurationMs = 30 * 60 * 1000L

    /**
     * Kiểm tra xem key có được phép thực hiện action không
     * @param key Identifier (username, IP address, etc.)
     * @return true nếu được phép, false nếu bị block
     */
    @Synchronized
    fun isAllowed(key: String): Boolean {
        val now = System.currentTimeMillis()
        
        // Kiểm tra nếu đang bị block
        val blockTime = blockedUntil[key]
        if (blockTime != null && now < blockTime) {
            Log.w(TAG, "⛔ Key '$key' is blocked until ${java.util.Date(blockTime)}")
            return false
        }
        
        // Xóa block nếu đã hết thời gian
        if (blockTime != null && now >= blockTime) {
            blockedUntil.remove(key)
            attempts.remove(key) // Reset attempts khi hết block
        }
        
        // Lấy danh sách attempts và lọc những cái còn trong window
        val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
        keyAttempts.removeAll { now - it > windowMs }
        
        // Kiểm tra số lần thử
        if (keyAttempts.size >= maxAttempts) {
            // Block key này
            blockedUntil[key] = now + blockDurationMs
            Log.w(TAG, "🔒 Key '$key' has been BLOCKED for ${blockDurationMs / 60000} minutes")
            return false
        }
        
        return true
    }

    /**
     * Ghi nhận một lần thử
     * @param key Identifier
     */
    @Synchronized
    fun recordAttempt(key: String) {
        if (!isAllowed(key)) return // Không ghi nhận nếu đã bị block
        
        val keyAttempts = attempts.getOrPut(key) { mutableListOf() }
        keyAttempts.add(System.currentTimeMillis())
        
        Log.d(TAG, "📝 Recorded attempt for '$key'. Total: ${keyAttempts.size}/$maxAttempts")
    }

    /**
     * Ghi nhận thành công (reset attempts)
     * @param key Identifier
     */
    @Synchronized
    fun recordSuccess(key: String) {
        attempts.remove(key)
        blockedUntil.remove(key)
        Log.d(TAG, "✅ Success for '$key'. Attempts reset.")
    }

    /**
     * Lấy số lần thử còn lại
     * @param key Identifier
     * @return Số lần thử còn lại
     */
    @Synchronized
    fun getRemainingAttempts(key: String): Int {
        val now = System.currentTimeMillis()
        
        // Nếu đang bị block
        if (blockedUntil[key]?.let { now < it } == true) {
            return 0
        }
        
        val keyAttempts = attempts[key] ?: return maxAttempts
        
        // Lọc những attempts còn trong window
        val recentAttempts = keyAttempts.count { now - it <= windowMs }
        
        return maxOf(0, maxAttempts - recentAttempts)
    }

    /**
     * Lấy thời gian còn lại của block (milliseconds)
     * @param key Identifier
     * @return Thời gian block còn lại, 0 nếu không bị block
     */
    @Synchronized
    fun getBlockTimeRemaining(key: String): Long {
        val now = System.currentTimeMillis()
        val blockTime = blockedUntil[key] ?: return 0
        
        return if (now < blockTime) {
            blockTime - now
        } else {
            0
        }
    }

    /**
     * Lấy thời gian block còn lại dạng readable
     */
    fun getBlockTimeRemainingFormatted(key: String): String {
        val remainingMs = getBlockTimeRemaining(key)
        if (remainingMs <= 0) return ""
        
        val minutes = remainingMs / 60000
        val seconds = (remainingMs % 60000) / 1000
        
        return when {
            minutes > 0 -> "$minutes phút $seconds giây"
            else -> "$seconds giây"
        }
    }

    /**
     * Kiểm tra xem key có đang bị block không
     */
    @Synchronized
    fun isBlocked(key: String): Boolean {
        val now = System.currentTimeMillis()
        val blockTime = blockedUntil[key] ?: return false
        return now < blockTime
    }

    /**
     * Kiểm tra xem có cần hiển thị captcha không
     * (Khi còn <= 2 lần thử)
     */
    fun requiresCaptcha(key: String): Boolean {
        return getRemainingAttempts(key) <= 2
    }

    /**
     * Reset rate limiter cho key
     */
    @Synchronized
    fun reset(key: String) {
        attempts.remove(key)
        blockedUntil.remove(key)
        Log.d(TAG, "🔄 Rate limiter reset for '$key'")
    }

    /**
     * Clear tất cả data (cho testing)
     */
    @Synchronized
    fun clearAll() {
        attempts.clear()
        blockedUntil.clear()
        Log.d(TAG, "🧹 All rate limiter data cleared")
    }
}
