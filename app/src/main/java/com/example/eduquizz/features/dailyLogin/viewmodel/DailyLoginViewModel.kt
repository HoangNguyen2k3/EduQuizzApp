package com.example.eduquizz.features.dailyLogin.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data_save.UserPreferencesManager
import com.example.eduquizz.data_save.SecureDataStoreManager
import com.example.eduquizz.features.dailyLogin.model.DailyLoginReward
import com.example.eduquizz.features.dailyLogin.model.DailyLoginUiState
import com.example.eduquizz.features.dailyLogin.model.UserDailyLoginData
import com.google.firebase.Timestamp
import com.example.eduquizz.features.dailyLogin.repository.DailyLoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyLoginViewModel @Inject constructor(
    private val repository: DailyLoginRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val userPreferencesManager = UserPreferencesManager(context)
    private val secureDataStore = SecureDataStoreManager(context)
    
    private val _uiState = MutableStateFlow(DailyLoginUiState())
    val uiState: StateFlow<DailyLoginUiState> = _uiState.asStateFlow()
    
    companion object {
        private const val TAG = "DailyLoginViewModel"
    }
    
    /**
     * Lấy userId duy nhất cho user hiện tại
     * Ưu tiên: Backend ID > Firebase UID > Email > Username
     */
    fun getCurrentUserId(): String {
        val prefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
        
        // 1. Thử lấy userId từ UserResponse (id từ backend) - unique nhất
        val userDataJson = prefs.getString("user_data", null)
        if (userDataJson != null) {
            try {
                val userData = Gson().fromJson(
                    userDataJson, 
                    com.example.eduquizz.features.auth.data.api.UserResponse::class.java
                )
                val backendUserId = userData.id.toString()
                if (backendUserId.isNotEmpty() && backendUserId != "0") {
                    Log.d(TAG, "✅ [USER_ID] Using backend userId: $backendUserId")
                    return backendUserId
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ [USER_ID] Failed to parse user_data: ${e.message}")
            }
        }
        
        // 2. Thử lấy Firebase Auth UID (nếu đăng nhập bằng Google/Firebase)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null && firebaseUser.uid.isNotEmpty()) {
            Log.d(TAG, "✅ [USER_ID] Using Firebase UID: ${firebaseUser.uid}")
            return firebaseUser.uid
        }
        
        // 3. Fallback: dùng email (unique hơn username)
        val email = prefs.getString("email", null)
        if (!email.isNullOrEmpty()) {
            Log.d(TAG, "✅ [USER_ID] Using email: $email")
            return email
        }
        
        // 4. Cuối cùng: dùng username (không an toàn nhưng để tương thích)
        val username = prefs.getString("username", null)
        if (!username.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ [USER_ID] Using username (not recommended): $username")
            return username
        }
        
        Log.e(TAG, "❌ [USER_ID] No user ID found, using default")
        return "default_user"
    }
    
    /**
     * Load daily login data cho user
     */
    fun loadDailyLoginData(userId: String) {
        Log.d(TAG, "🔄 [LOAD] Starting to load daily login data for userId: $userId")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                Log.d(TAG, "📥 [LOAD] Fetching user data from repository...")
                val userData = repository.getUserDailyLoginData(userId)
                
                if (userData == null) {
                    // User data null - có thể là user mới hoặc Firestore offline
                    Log.d(TAG, "🆕 [LOAD] User data is null")
                    Log.d(TAG, "⚠️ [LOAD] This could mean:")
                    Log.d(TAG, "   1. User is new (never claimed before)")
                    Log.d(TAG, "   2. Firestore is offline (cannot fetch existing data)")
                    Log.d(TAG, "   3. Firestore permission denied")
                    
                    // Tạo data mới với canClaim = true (user mới hoặc chưa claim)
                    // Nếu đã có data trên server nhưng offline, sẽ sync lại khi online
                    val defaultData = UserDailyLoginData(
                        userId = userId,
                        currentDay = 0,
                        lastClaimedDay = 0,
                        lastClaimedDate = null,
                        lastServerTimestamp = null,
                        cycleStartDate = null,
                        rewards = DailyLoginReward.createDefaultRewards(),
                        updatedAt = null
                    )
                    
                    Log.d(TAG, "🆕 [LOAD] Using default data - canClaim=true (user may be new)")
                    updateUiStateFromUserData(defaultData, canClaim = true)
                    
                    // Thử initialize trên server (background, không block UI)
                    viewModelScope.launch {
                        try {
                            Log.d(TAG, "🔄 [LOAD] Attempting to initialize on server (background)...")
                            repository.initializeUserDailyLogin(userId)
                            Log.d(TAG, "✅ [LOAD] Initialized on server successfully")
                        } catch (e: Exception) {
                            Log.w(TAG, "⚠️ [LOAD] Could not initialize on server (may be offline): ${e.message}")
                            // Không hiển thị error, chỉ log - user vẫn có thể thử claim
                        }
                    }
                } else {
                    // Kiểm tra xem có thể claim hôm nay không
                    Log.d(TAG, "📊 [LOAD] ========== USER DATA FOUND ==========")
                    Log.d(TAG, "📊 [LOAD] User data details:")
                    Log.d(TAG, "   - userId: ${userData.userId}")
                    Log.d(TAG, "   - currentDay: ${userData.currentDay}")
                    Log.d(TAG, "   - lastClaimedDay: ${userData.lastClaimedDay}")
                    Log.d(TAG, "   - lastClaimedDate: ${userData.lastClaimedDate}")
                    if (userData.lastClaimedDate != null) {
                        Log.d(TAG, "   - lastClaimedDate (formatted): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(userData.lastClaimedDate.toDate())}")
                    }
                    Log.d(TAG, "   - rewards count: ${userData.rewards.size}")
                    Log.d(TAG, "   - rewards claimed: ${userData.rewards.count { it.isClaimed }}")
                    userData.rewards.forEachIndexed { index, reward ->
                        Log.d(TAG, "     Day ${index + 1}: isClaimed=${reward.isClaimed}, claimedAt=${reward.claimedAt}")
                    }
                    
                    Log.d(TAG, "⏰ [LOAD] Getting server timestamp to check canClaimToday...")
                    val serverTimestamp = repository.getServerTimestamp()
                    Log.d(TAG, "⏰ [LOAD] Server timestamp: $serverTimestamp")
                    if (serverTimestamp != null) {
                        Log.d(TAG, "⏰ [LOAD] Server timestamp (formatted): ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(serverTimestamp.toDate())}")
                    }
                    
                    Log.d(TAG, "🔍 [LOAD] Calling canClaimToday()...")
                    val canClaim = userData.canClaimToday(serverTimestamp)
                    
                    Log.d(TAG, "🔍 [LOAD] ========== CAN CLAIM RESULT ==========")
                    Log.d(TAG, "🔍 [LOAD] canClaimToday: $canClaim")
                    Log.d(TAG, "🔍 [LOAD] ======================================")
                    
                    updateUiStateFromUserData(userData, canClaim)
                    Log.d(TAG, "✅ [LOAD] User data loaded and UI state updated")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [LOAD] Error loading daily login data: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load daily login data: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Claim reward cho ngày hôm nay
     */
    fun claimTodayReward(userId: String) {
        Log.d(TAG, "🎯 [CLAIM] ========== START CLAIM REWARD ==========")
        Log.d(TAG, "🎯 [CLAIM] User clicked Điểm danh button for userId: $userId")
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isClaiming = true,
                errorMessage = null,
                successMessage = null
            )
            Log.d(TAG, "🔄 [CLAIM] UI state updated: isClaiming=true")
            
            try {
                val clientTimestamp = System.currentTimeMillis()
                Log.d(TAG, "⏰ [CLAIM] Client timestamp: $clientTimestamp (${java.util.Date(clientTimestamp)})")
                
                Log.d(TAG, "📤 [CLAIM] Calling repository.claimDailyReward()...")
                val result = repository.claimDailyReward(userId, clientTimestamp)
                
                result.fold(
                    onSuccess = { (day, goldReward) ->
                        Log.d(TAG, "✅ [CLAIM] Repository returned success: day=$day, gold=$goldReward")
                        
                        // Cộng gold vào tài khoản (sử dụng SecureDataStore để sync với home)
                        try {
                            Log.d(TAG, "💰 [CLAIM] Adding gold to account: $goldReward")
                            val currentGold = secureDataStore.goldFlow.first()
                            Log.d(TAG, "💰 [CLAIM] Current gold before: $currentGold")
                            
                            val newGold = secureDataStore.addGold(goldReward)
                            
                            Log.d(TAG, "💰 [CLAIM] Gold after adding: $newGold (added $goldReward)")
                            Log.d(TAG, "✅ [CLAIM] Gold added successfully! Total: $newGold")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ [CLAIM] Error adding gold: ${e.message}", e)
                            Log.e(TAG, "❌ [CLAIM] Stack trace: ${e.stackTraceToString()}")
                        }
                        
                        // Reload data
                        Log.d(TAG, "🔄 [CLAIM] Reloading daily login data...")
                        loadDailyLoginData(userId)
                        
                        _uiState.value = _uiState.value.copy(
                            isClaiming = false,
                            successMessage = "Chúc mừng! Bạn đã nhận $goldReward vàng!",
                            claimedGold = goldReward
                        )
                        
                        Log.d(TAG, "🎉 [CLAIM] ========== CLAIM SUCCESS ==========")
                        Log.d(TAG, "🎉 [CLAIM] Day: $day, Gold: $goldReward")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "❌ [CLAIM] ========== CLAIM FAILED ==========")
                        Log.e(TAG, "❌ [CLAIM] Error: ${error.message}")
                        Log.e(TAG, "❌ [CLAIM] Error type: ${error.javaClass.simpleName}")
                        if (error.cause != null) {
                            Log.e(TAG, "❌ [CLAIM] Cause: ${error.cause?.message}")
                        }
                        _uiState.value = _uiState.value.copy(
                            isClaiming = false,
                            errorMessage = error.message ?: "Failed to claim reward"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ [CLAIM] ========== EXCEPTION ==========")
                Log.e(TAG, "❌ [CLAIM] Exception: ${e.message}", e)
                Log.e(TAG, "❌ [CLAIM] Stack trace: ${e.stackTraceToString()}")
                _uiState.value = _uiState.value.copy(
                    isClaiming = false,
                    errorMessage = "An error occurred: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Update UI state từ user data
     */
    private fun updateUiStateFromUserData(
        userData: UserDailyLoginData,
        canClaim: Boolean = false
    ) {
        Log.d(TAG, "🔄 [UPDATE] ========== UPDATING UI STATE ==========")
        Log.d(TAG, "🔄 [UPDATE] userData.currentDay: ${userData.currentDay}")
        Log.d(TAG, "🔄 [UPDATE] userData.lastClaimedDay: ${userData.lastClaimedDay}")
        Log.d(TAG, "🔄 [UPDATE] userData.lastClaimedDate: ${userData.lastClaimedDate}")
        Log.d(TAG, "🔄 [UPDATE] userData.rewards.size: ${userData.rewards.size}")
        Log.d(TAG, "🔄 [UPDATE] userData.rewards claimed: ${userData.rewards.count { it.isClaimed }}")
        Log.d(TAG, "🔄 [UPDATE] canClaim parameter: $canClaim")
        
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            currentDay = userData.currentDay,
            lastClaimedDay = userData.lastClaimedDay,
            rewards = userData.rewards,
            canClaimToday = canClaim
        )
        
        Log.d(TAG, "✅ [UPDATE] UI state updated:")
        Log.d(TAG, "   - currentDay: ${_uiState.value.currentDay}")
        Log.d(TAG, "   - lastClaimedDay: ${_uiState.value.lastClaimedDay}")
        Log.d(TAG, "   - canClaimToday: ${_uiState.value.canClaimToday}")
        Log.d(TAG, "   - rewards count: ${_uiState.value.rewards.size}")
        Log.d(TAG, "✅ [UPDATE] ========== UPDATE COMPLETE ==========")
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null, claimedGold = 0)
    }
}

