package com.example.eduquizz.features.dailyLogin.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Model cho phần thưởng đăng nhập hàng ngày
 * 
 * LƯU Ý: @PropertyName cần thiết vì Firestore Java SDK chuyển đổi
 * boolean property "isClaimed" thành "claimed" theo Java Bean convention.
 * Không có annotation này, data sẽ bị mất khi deserialize.
 */
data class DailyLoginReward(
    val day: Int = 0,
    val goldReward: Int = 0,
    
    // QUAN TRỌNG: Phải dùng @get:PropertyName và @set:PropertyName 
    // để Firestore serialize/deserialize đúng field name
    @get:PropertyName("isClaimed") 
    @set:PropertyName("isClaimed")
    var isClaimed: Boolean = false,
    
    val claimedAt: Timestamp? = null
) {
    // Constructor không tham số cần thiết cho Firestore deserialization
    constructor() : this(0, 0, false, null)
    
    companion object {
        /**
         * Tạo danh sách 7 ngày với phần thưởng tăng dần
         */
        fun createDefaultRewards(): List<DailyLoginReward> {
            return (1..7).map { day ->
                DailyLoginReward(
                    day = day,
                    goldReward = day * 100
                )
            }
        }
    }
}

/**
 * Data class lưu trữ thông tin daily login của user trên Firebase
 */
data class UserDailyLoginData(
    val userId: String = "",
    val currentDay: Int = 0,
    val lastClaimedDay: Int = 0,
    val lastClaimedDate: Timestamp? = null,
    val lastServerTimestamp: Timestamp? = null,
    val cycleStartDate: Timestamp? = null,
    val rewards: List<DailyLoginReward> = DailyLoginReward.createDefaultRewards(),
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    /**
     * Kiểm tra xem user có thể claim ngày hôm nay không
     */
    fun canClaimToday(serverTimestamp: Timestamp): Boolean {
        android.util.Log.d("UserDailyLoginData", "🔍 [CAN_CLAIM] Checking canClaimToday:")
        android.util.Log.d("UserDailyLoginData", "   - currentDay: $currentDay")
        android.util.Log.d("UserDailyLoginData", "   - lastClaimedDay: $lastClaimedDay")
        android.util.Log.d("UserDailyLoginData", "   - lastClaimedDate: $lastClaimedDate")
        android.util.Log.d("UserDailyLoginData", "   - serverTimestamp: $serverTimestamp")
        
        // Nếu chưa bắt đầu (currentDay = 0, lastClaimedDay = 0, lastClaimedDate = null)
        if (currentDay == 0 && lastClaimedDay == 0 && lastClaimedDate == null) {
            android.util.Log.d("UserDailyLoginData", "✅ [CAN_CLAIM] First time - can claim")
            return true // Chưa bắt đầu, có thể claim ngày 1
        }
        
        val lastClaimed = lastClaimedDate
        if (lastClaimed == null) {
            android.util.Log.d("UserDailyLoginData", "✅ [CAN_CLAIM] No lastClaimedDate - can claim")
            return true
        }
        
        val daysDiff = calculateDaysDifference(lastClaimed, serverTimestamp)
        android.util.Log.d("UserDailyLoginData", "   - daysDiff: $daysDiff")
        
        val result = when {
            daysDiff == 0 -> {
                android.util.Log.d("UserDailyLoginData", "❌ [CAN_CLAIM] Already claimed today (daysDiff=0)")
                false // Đã claim hôm nay
            }
            daysDiff == 1 -> {
                android.util.Log.d("UserDailyLoginData", "✅ [CAN_CLAIM] Next day - can claim")
                true // Ngày tiếp theo
            }
            daysDiff > 1 -> {
                android.util.Log.d("UserDailyLoginData", "✅ [CAN_CLAIM] Missed day - can claim (will reset)")
                true // Quá 1 ngày, có thể claim nhưng sẽ reset về ngày 1
            }
            else -> {
                android.util.Log.d("UserDailyLoginData", "❌ [CAN_CLAIM] Invalid daysDiff: $daysDiff")
                false
            }
        }
        
        android.util.Log.d("UserDailyLoginData", "🔍 [CAN_CLAIM] Final result: $result")
        return result
    }
    
    /**
     * Tính số ngày chênh lệch giữa 2 timestamp
     */
    private fun calculateDaysDifference(timestamp1: Timestamp, timestamp2: Timestamp): Int {
        val date1 = timestamp1.toDate()
        val date2 = timestamp2.toDate()
        
        android.util.Log.d("UserDailyLoginData", "📅 [CALC] Calculating days difference:")
        android.util.Log.d("UserDailyLoginData", "   - date1: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date1)}")
        android.util.Log.d("UserDailyLoginData", "   - date2: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(date2)}")
        
        // Reset về 00:00:00 để so sánh theo ngày
        val cal1 = java.util.Calendar.getInstance().apply {
            time = date1
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val cal2 = java.util.Calendar.getInstance().apply {
            time = date2
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val diffInMillis = cal2.timeInMillis - cal1.timeInMillis
        val daysDiff = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
        
        android.util.Log.d("UserDailyLoginData", "📅 [CALC] cal1 (normalized): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(cal1.time)}")
        android.util.Log.d("UserDailyLoginData", "📅 [CALC] cal2 (normalized): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(cal2.time)}")
        android.util.Log.d("UserDailyLoginData", "📅 [CALC] diffInMillis: $diffInMillis")
        android.util.Log.d("UserDailyLoginData", "📅 [CALC] daysDiff: $daysDiff")
        
        return daysDiff
    }
}

/**
 * UI State cho Daily Login Screen
 */
data class DailyLoginUiState(
    val isLoading: Boolean = false,
    val currentDay: Int = 0,
    val lastClaimedDay: Int = 0,
    val rewards: List<DailyLoginReward> = DailyLoginReward.createDefaultRewards(),
    val canClaimToday: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isClaiming: Boolean = false,
    val claimedGold: Int = 0
)

