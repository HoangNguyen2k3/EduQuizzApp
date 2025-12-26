package com.example.eduquizz.features.dailyLogin.repository

import android.util.Log
import com.example.eduquizz.features.dailyLogin.model.DailyLoginReward
import com.example.eduquizz.features.dailyLogin.model.UserDailyLoginData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý daily login rewards với Firebase
 * Sử dụng server timestamp để chống gian lận
 */
@Singleton
class DailyLoginRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collectionName = "daily_login_rewards"
    
    companion object {
        private const val TAG = "DailyLoginRepository"
        private const val MAX_TIME_DIFF_MS = 5 * 60 * 1000 // 5 phút tolerance cho time sync
    }
    
    /**
     * Lấy server timestamp từ Firebase
     * Đây là cách duy nhất để có timestamp đáng tin cậy
     */
    suspend fun getServerTimestamp(): Timestamp {
        return try {
            // Tạo document tạm với server timestamp
            val tempDocId = "temp_${System.currentTimeMillis()}"
            val docRef = firestore.collection("_server_timestamps").document(tempDocId)
            
            // Set document với server timestamp
            docRef.set(mapOf(
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "created" to System.currentTimeMillis()
            )).await()
            
            // Đọc lại để lấy timestamp thực tế từ server
            val snapshot = docRef.get().await()
            val timestamp = snapshot.getTimestamp("timestamp")
            
            // Xóa document tạm (không cần await)
            docRef.delete()
            
            if (timestamp != null) {
                Log.d(TAG, "Got server timestamp: $timestamp")
                timestamp
            } else {
                Log.w(TAG, "Server timestamp is null, using local")
                Timestamp.now()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting server timestamp, using local: ${e.message}", e)
            // Fallback: sử dụng local timestamp nhưng sẽ validate sau
            Timestamp.now()
        }
    }
    
    /**
     * Validate client timestamp với server timestamp
     * Chống gian lận bằng cách đổi thời gian local
     */
    suspend fun validateTimestamp(clientTimestamp: Long): Boolean {
        return try {
            val serverTimestamp = getServerTimestamp()
            val serverTime = serverTimestamp.toDate().time
            val timeDiff = kotlin.math.abs(serverTime - clientTimestamp)
            
            // Cho phép chênh lệch tối đa 5 phút (do network delay, time sync)
            val isValid = timeDiff <= MAX_TIME_DIFF_MS
            
            if (!isValid) {
                Log.w(TAG, "Timestamp validation failed: diff=${timeDiff}ms, client=$clientTimestamp, server=$serverTime")
            }
            
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Error validating timestamp: ${e.message}")
            false
        }
    }
    
    /**
     * Lấy thông tin daily login của user
     */
    suspend fun getUserDailyLoginData(userId: String): UserDailyLoginData? {
        Log.d(TAG, "📥 [GET] Getting user daily login data for userId: $userId")
        return try {
            val docRef = firestore.collection(collectionName).document(userId)
            Log.d(TAG, "📥 [GET] Fetching from Firestore...")
            val snapshot = docRef.get().await()
            
            if (snapshot.exists()) {
                val userData = snapshot.toObject(UserDailyLoginData::class.java)
                Log.d(TAG, "✅ [GET] User data found: currentDay=${userData?.currentDay}, lastClaimedDay=${userData?.lastClaimedDay}")
                userData
            } else {
                Log.d(TAG, "📭 [GET] User data does not exist in Firestore")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GET] Error getting user daily login data: ${e.message}", e)
            Log.e(TAG, "❌ [GET] Error type: ${e.javaClass.simpleName}")
            if (e.message?.contains("offline") == true || e.message?.contains("PERMISSION_DENIED") == true) {
                Log.w(TAG, "⚠️ [GET] Firestore is offline or permission denied - cannot fetch data")
            }
            null
        }
    }
    
    /**
     * Khởi tạo daily login data cho user mới
     */
    suspend fun initializeUserDailyLogin(userId: String): UserDailyLoginData {
        Log.d(TAG, "🆕 [INIT] Initializing daily login for new user: $userId")
        return try {
            val serverTimestamp = getServerTimestamp()
            Log.d(TAG, "⏰ [INIT] Server timestamp: $serverTimestamp")
            
            val newData = UserDailyLoginData(
                userId = userId,
                currentDay = 0,
                lastClaimedDay = 0,
                lastClaimedDate = null,
                lastServerTimestamp = serverTimestamp,
                cycleStartDate = null,
                rewards = DailyLoginReward.createDefaultRewards(),
                updatedAt = serverTimestamp
            )
            
            Log.d(TAG, "💾 [INIT] Saving new user data to Firestore...")
            val docRef = firestore.collection(collectionName).document(userId)
            docRef.set(newData).await()
            
            Log.d(TAG, "✅ [INIT] User initialized successfully")
            newData
        } catch (e: Exception) {
            Log.e(TAG, "❌ [INIT] Error initializing user daily login: ${e.message}", e)
            if (e.message?.contains("offline") == true || e.message?.contains("PERMISSION_DENIED") == true) {
                Log.w(TAG, "⚠️ [INIT] Firestore offline - returning local data only")
                // Trả về data local nếu offline
                val serverTimestamp = getServerTimestamp()
                return UserDailyLoginData(
                    userId = userId,
                    currentDay = 0,
                    lastClaimedDay = 0,
                    lastClaimedDate = null,
                    lastServerTimestamp = serverTimestamp,
                    cycleStartDate = null,
                    rewards = DailyLoginReward.createDefaultRewards(),
                    updatedAt = serverTimestamp
                )
            }
            throw e
        }
    }
    
    /**
     * Claim reward cho ngày hôm nay
     * Sử dụng server timestamp để đảm bảo tính chính xác
     */
    suspend fun claimDailyReward(
        userId: String,
        clientTimestamp: Long
    ): Result<Pair<Int, Int>> {
        Log.d(TAG, "🎯 [REPO] ========== CLAIM DAILY REWARD ==========")
        Log.d(TAG, "🎯 [REPO] userId: $userId")
        Log.d(TAG, "🎯 [REPO] clientTimestamp: $clientTimestamp (${java.util.Date(clientTimestamp)})")

        return try {
            // 1) Lấy server timestamp duy nhất
            Log.d(TAG, "⏰ [REPO] Getting server timestamp...")
            val serverTimestamp = getServerTimestamp()
            Log.d(TAG, "⏰ [REPO] Server timestamp: $serverTimestamp (${serverTimestamp.toDate()})")

            // 2) Validate timestamp (chống đổi giờ)
            Log.d(TAG, "🔍 [REPO] Validating timestamp...")
            val isValid = validateTimestamp(clientTimestamp, serverTimestamp)
            Log.d(TAG, "🔍 [REPO] Timestamp validation: $isValid")
            
            if (!isValid) {
                Log.w(TAG, "⚠️ [REPO] Timestamp validation FAILED!")
                return Result.failure(Exception("Your device time is incorrect"))
            }

            // 3) Lấy dữ liệu user
            Log.d(TAG, "📥 [REPO] Getting user data...")
            val userData = getUserDailyLoginData(userId) ?: run {
                Log.d(TAG, "🆕 [REPO] User data not found, initializing...")
                initializeUserDailyLogin(userId)
            }
            Log.d(TAG, "📊 [REPO] ========== USER DATA IN CLAIM ==========")
            Log.d(TAG, "📊 [REPO] currentDay: ${userData.currentDay}")
            Log.d(TAG, "📊 [REPO] lastClaimedDay: ${userData.lastClaimedDay}")
            Log.d(TAG, "📊 [REPO] lastClaimedDate: ${userData.lastClaimedDate}")
            if (userData.lastClaimedDate != null) {
                Log.d(TAG, "📊 [REPO] lastClaimedDate (formatted): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(userData.lastClaimedDate.toDate())}")
            }
            Log.d(TAG, "📊 [REPO] =========================================")

            val lastClaimed = userData.lastClaimedDate
            val daysDiff = if (lastClaimed == null) {
                Log.d(TAG, "🆕 [REPO] First time claim (lastClaimed is null)")
                1
            } else {
                val diff = calculateDaysDifference(lastClaimed, serverTimestamp)
                Log.d(TAG, "📅 [REPO] Days difference: $diff (lastClaimed: ${lastClaimed.toDate()}, server: ${serverTimestamp.toDate()})")
                diff
            }

            when (daysDiff) {
                0 -> {
                    Log.w(TAG, "⚠️ [REPO] Already claimed today (daysDiff=0)")
                    return Result.failure(Exception("Already claimed today"))
                }

                1 -> {
                    val nextDay = (userData.lastClaimedDay + 1)
                    val finalDay = if (nextDay > 7) 1 else nextDay
                    val gold = finalDay * 100

                    Log.d(TAG, "💰 [REPO] Claiming day: $finalDay, gold: $gold (nextDay was $nextDay)")

                    val updated = updateReward(userData, finalDay, gold, serverTimestamp)
                    saveUserDailyLoginData(userId, updated)

                    Log.d(TAG, "✅ [REPO] Claim SUCCESS: day=$finalDay, gold=$gold")
                    return Result.success(finalDay to gold)
                }

                else -> {
                    // Reset streak
                    Log.d(TAG, "🔄 [REPO] Missed day (daysDiff=$daysDiff), resetting to day 1")
                    val gold = 100
                    val updated = updateReward(userData, 1, gold, serverTimestamp, reset = true)
                    saveUserDailyLoginData(userId, updated)

                    Log.d(TAG, "✅ [REPO] Reset claim SUCCESS: day=1, gold=$gold")
                    return Result.success(1 to gold)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [REPO] ========== EXCEPTION ==========")
            Log.e(TAG, "❌ [REPO] Error: ${e.message}", e)
            Log.e(TAG, "❌ [REPO] Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
    private fun updateReward(
        oldData: UserDailyLoginData,
        newDay: Int,
        gold: Int,
        serverTS: Timestamp,
        reset: Boolean = false
    ): UserDailyLoginData {
        Log.d(TAG, "🔄 [UPDATE] Updating reward: newDay=$newDay, gold=$gold, reset=$reset")
        
        val newRewards = if (reset) {
            Log.d(TAG, "🔄 [UPDATE] Resetting rewards list")
            DailyLoginReward.createDefaultRewards()
        } else {
            oldData.rewards
        }

        val updatedRewards = newRewards.mapIndexed { index, reward ->
            if (index == newDay - 1) {
                Log.d(TAG, "✅ [UPDATE] Marking day ${newDay} as claimed")
                // Tạo reward mới với isClaimed = true
                DailyLoginReward(
                    day = reward.day,
                    goldReward = reward.goldReward,
                    isClaimed = true,
                    claimedAt = serverTS
                )
            } else {
                if (reset && index != newDay - 1) {
                    // Reset các ngày khác về unclaimed khi reset cycle
                    DailyLoginReward(
                        day = reward.day,
                        goldReward = reward.goldReward,
                        isClaimed = false,
                        claimedAt = null
                    )
                } else {
                    reward
                }
            }
        }

        val updatedData = oldData.copy(
            currentDay = newDay,
            lastClaimedDay = newDay,
            lastClaimedDate = serverTS,
            lastServerTimestamp = serverTS,
            cycleStartDate = if (reset || oldData.cycleStartDate == null) serverTS else oldData.cycleStartDate,
            rewards = updatedRewards,
            updatedAt = serverTS
        )
        
        // Log chi tiết rewards sau khi update
        Log.d(TAG, "✅ [UPDATE] Rewards after update:")
        updatedRewards.forEachIndexed { index, r ->
            Log.d(TAG, "     Day ${index + 1}: isClaimed=${r.isClaimed}, claimedAt=${r.claimedAt}")
        }
        
        Log.d(TAG, "✅ [UPDATE] Reward updated: currentDay=${updatedData.currentDay}, lastClaimedDay=${updatedData.lastClaimedDay}")
        return updatedData
    }

    fun validateTimestamp(clientTime: Long, serverTimestamp: Timestamp): Boolean {
        val serverTime = serverTimestamp.toDate().time
        val diff = kotlin.math.abs(serverTime - clientTime)
        return diff <= MAX_TIME_DIFF_MS
    }
    /**
     * Lưu user daily login data
     */
    private suspend fun saveUserDailyLoginData(
        userId: String,
        data: UserDailyLoginData
    ) {
        try {
            Log.d(TAG, "💾 [SAVE] Saving user data to Firebase...")
            Log.d(TAG, "💾 [SAVE] userId: $userId")
            Log.d(TAG, "💾 [SAVE] currentDay: ${data.currentDay}, lastClaimedDay: ${data.lastClaimedDay}")
            Log.d(TAG, "💾 [SAVE] lastClaimedDate: ${data.lastClaimedDate}")
            
            val docRef = firestore.collection(collectionName).document(userId)
            docRef.set(data, SetOptions.merge()).await()
            
            Log.d(TAG, "✅ [SAVE] User data saved successfully to Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [SAVE] Error saving user daily login data: ${e.message}", e)
            Log.e(TAG, "❌ [SAVE] Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    /**
     * Tính số ngày chênh lệch giữa 2 timestamp
     */
    private fun calculateDaysDifference(timestamp1: Timestamp, timestamp2: Timestamp): Int {
        val date1 = timestamp1.toDate()
        val date2 = timestamp2.toDate()
        
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
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

