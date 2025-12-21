package com.example.eduquizz.features.auth.data.repository

import android.content.Context
import android.util.Log
import com.example.eduquizz.security.SecurePreferencesManager
import com.example.eduquizz.features.auth.data.AuthPreferencesManager
import com.example.eduquizz.features.auth.data.api.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

@Singleton
class SecureAuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
) {
    private val authPrefs = AuthPreferencesManager(context)
    private val gson = Gson()

    companion object {
        // Keys cho encrypted storage
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_USER_ID = "user_id_backend"
        private const val KEY_EMAIL = "email"
        private const val KEY_AUTH_TOKEN = "auth_token" // Token nên được mã hóa!
    }

    /**
     * Lưu user data với mã hóa
     */
    suspend fun saveUserData(user: UserResponse) {
        try {
            // Lưu vào encrypted preferences
            SecurePreferencesManager.apply {
                saveBoolean(context, KEY_IS_LOGGED_IN, true)
                saveString(context, KEY_USER_DATA, gson.toJson(user))
                saveString(context, KEY_USERNAME, user.username)
                saveString(context, KEY_ROLE, user.role)
                saveLong(context, KEY_USER_ID, user.id)
                saveString(context, KEY_EMAIL, user.email)
            }

            Log.d("SecureAuthRepository", "✅ Saved encrypted user data: id=${user.id}, username=${user.username}")
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "❌ Failed to save user data: ${e.message}")
        }
    }

    /**
     * Lấy saved user data (đã giải mã)
     */
    fun getSavedUser(): UserResponse? {
        return try {
            val userJson = SecurePreferencesManager.getString(context, KEY_USER_DATA)
            if (userJson.isNotBlank()) {
                gson.fromJson(userJson, UserResponse::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Failed to get user data: ${e.message}")
            null
        }
    }

    /**
     * Kiểm tra user đã login chưa
     */
    fun isUserLoggedIn(): Boolean {
        return SecurePreferencesManager.getBoolean(context, KEY_IS_LOGGED_IN, false)
    }

    /**
     * Lấy username đã lưu
     */
    fun getSavedUsername(): String? {
        return SecurePreferencesManager.getString(context, KEY_USERNAME).takeIf { it.isNotBlank() }
    }

    /**
     * Lấy role đã lưu
     */
    fun getSavedRole(): String? {
        return SecurePreferencesManager.getString(context, KEY_ROLE).takeIf { it.isNotBlank() }
    }

    /**
     * Kiểm tra user có phải admin không
     */
    fun isUserAdmin(): Boolean {
        return getSavedRole() == "ADMIN"
    }

    /**
     * Lưu auth token (được mã hóa)
     */
    fun saveAuthToken(token: String) {
        SecurePreferencesManager.saveString(context, KEY_AUTH_TOKEN, token)
    }

    /**
     * Lấy auth token
     */
    fun getAuthToken(): String? {
        return SecurePreferencesManager.getString(context, KEY_AUTH_TOKEN).takeIf { it.isNotBlank() }
    }

    /**
     * Register với backend
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): AuthResult<UserResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(username, email, password, fullName)
            )

            if (response.isSuccessful) {
                val registerResponse = response.body()
                if (registerResponse?.success == true && registerResponse.user != null) {
                    saveUserData(registerResponse.user)
                    AuthResult.Success(registerResponse.user)
                } else {
                    AuthResult.Error(registerResponse?.message ?: "Registration failed")
                }
            } else {
                AuthResult.Error("Registration failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Register exception: ${e.message}")
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Login với backend
     */
    suspend fun login(usernameOrEmail: String, password: String): AuthResult<UserResponse> {
        return try {
            val response = apiService.login(
                LoginRequest(usernameOrEmail, password)
            )

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse?.success == true && loginResponse.user != null) {
                    saveUserData(loginResponse.user)
                    AuthResult.Success(loginResponse.user)
                } else {
                    AuthResult.Error(loginResponse?.message ?: "Login failed")
                }
            } else {
                AuthResult.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Login exception: ${e.message}")
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Lấy user profile từ backend
     */
    suspend fun getUserProfile(username: String): AuthResult<UserResponse> {
        return try {
            val response = apiService.getUserProfile(username)
            if (response.isSuccessful) {
                response.body()?.let {
                    AuthResult.Success(it)
                } ?: AuthResult.Error("User not found")
            } else {
                AuthResult.Error("Failed to fetch user profile")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Google Sign-In
     */
    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient

    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult<FirebaseUser> {
        return try {
            Log.d("SecureAuthRepository", "=== signInWithGoogle ===")
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()

            authResult.user?.let { firebaseUser ->
                // Lưu session với mã hóa
                authPrefs.saveUserSession(
                    userId = 0,
                    username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "user",
                    email = firebaseUser.email ?: "",
                    fullName = firebaseUser.displayName,
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    authToken = account.idToken,
                    loginMethod = "google"
                )

                // Lưu token vào encrypted storage
                account.idToken?.let { saveAuthToken(it) }

                AuthResult.Success(firebaseUser)
            } ?: AuthResult.Error("Google sign-in failed")
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Google sign-in error: ${e.message}")
            AuthResult.Error(e.message ?: "Google sign-in error")
        }
    }

    /**
     * Đăng xuất - xóa session data nhưng GIỮ LẠI profile data
     */
    fun signOut() {
        // Chỉ xóa session data, KHÔNG xóa profile data
        SecurePreferencesManager.apply {
            saveBoolean(context, KEY_IS_LOGGED_IN, false)
            saveString(context, KEY_USER_DATA, "")
            saveString(context, KEY_USERNAME, "")
            saveString(context, KEY_ROLE, "")
            saveLong(context, KEY_USER_ID, 0)
            saveString(context, KEY_EMAIL, "")
            saveString(context, KEY_AUTH_TOKEN, "")
        }
        // Profile data (profile_full_name, profile_dob, etc.) và profile_completed_<userId> được giữ lại
        firebaseAuth.signOut()
        googleSignInClient.signOut()
    }

    /**
     * Cập nhật profile
     */
    suspend fun updateProfile(
        username: String,
        fullName: String? = null,
        phoneNumber: String? = null,
        profileImageUrl: String? = null
    ): AuthResult<UserResponse> {
        return try {
            val request = UpdateProfileRequest(
                fullName = fullName,
                phoneNumber = phoneNumber,
                profileImageUrl = profileImageUrl
            )

            val response = apiService.updateProfile(username, request)

            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse?.success == true && updateResponse.user != null) {
                    saveUserData(updateResponse.user)
                    AuthResult.Success(updateResponse.user)
                } else {
                    AuthResult.Error(updateResponse?.message ?: "Update failed")
                }
            } else {
                AuthResult.Error("Update failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Update profile exception: ${e.message}")
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Cập nhật thông tin profile của user
     * - Lưu TẤT CẢ thông tin (cả nhạy cảm và không nhạy cảm) vào EncryptedSharedPreferences
     * - Chỉ gửi thông tin KHÔNG nhạy cảm lên server
     */
    suspend fun updateUserProfile(
        userId: Long,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        hometown: String,
        // Thông tin nhạy cảm - chỉ lưu local
        phoneNumber: String = "",
        cccd: String = "",
        cccdIssueDate: String = "",
        cccdIssuePlace: String = ""
    ): AuthResult<UserProfileResponse> {
        return try {
            // 1. Lưu TẤT CẢ thông tin vào EncryptedSharedPreferences
            SecurePreferencesManager.apply {
                // Thông tin không nhạy cảm
                saveString(context, "profile_full_name", fullName)
                saveString(context, "profile_dob", dateOfBirth)
                saveString(context, "profile_gender", gender)
                saveString(context, "profile_hometown", hometown)
                
                // Thông tin nhạy cảm - CHỈ lưu local
                saveString(context, "profile_phone", phoneNumber)
                saveString(context, "profile_cccd", cccd)
                saveString(context, "profile_cccd_issue_date", cccdIssueDate)
                saveString(context, "profile_cccd_issue_place", cccdIssuePlace)
                
                // Flag để đánh dấu đã hoàn thành profile - LƯU THEO userId
                saveBoolean(context, "profile_completed_$userId", true)
            }
            
            Log.d("SecureAuthRepository", "✅ Saved all profile data to encrypted storage")
            
            // 2. Gửi CHỈ thông tin không nhạy cảm lên server qua HTTPS
            val request = UserProfileRequest(
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                gender = gender,
                hometown = hometown
            )
            
            val response = apiService.updateUserProfile(userId, request)
            
            if (response.isSuccessful) {
                val profileResponse = response.body()
                if (profileResponse?.success == true) {
                    Log.d("SecureAuthRepository", "✅ Profile updated on server successfully")
                    AuthResult.Success(profileResponse)
                } else {
                    AuthResult.Error(profileResponse?.message ?: "Profile update failed")
                }
            } else {
                AuthResult.Error("Profile update failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Update profile exception: ${e.message}")
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    /**
     * Kiểm tra xem user đã hoàn thành profile chưa - DỰA TRÊN userId
     */
    fun isProfileCompleted(): Boolean {
        val userId = SecurePreferencesManager.getLong(context, KEY_USER_ID, 0)
        if (userId == 0L) {
            Log.d("SecureAuthRepository", "isProfileCompleted: userId = 0, returning false")
            return false
        }
        val isCompleted = SecurePreferencesManager.getBoolean(context, "profile_completed_$userId", false)
        Log.d("SecureAuthRepository", "isProfileCompleted: userId=$userId, isCompleted=$isCompleted")
        return isCompleted
    }

    /**
     * Lấy thông tin profile đã lưu từ EncryptedSharedPreferences
     */
    fun getSavedProfileData(): com.example.eduquizz.features.auth.model.UserProfileData {
        return com.example.eduquizz.features.auth.model.UserProfileData(
            fullName = SecurePreferencesManager.getString(context, "profile_full_name", ""),
            dateOfBirth = SecurePreferencesManager.getString(context, "profile_dob", ""),
            gender = SecurePreferencesManager.getString(context, "profile_gender", ""),
            hometown = SecurePreferencesManager.getString(context, "profile_hometown", ""),
            phoneNumber = SecurePreferencesManager.getString(context, "profile_phone", ""),
            cccd = SecurePreferencesManager.getString(context, "profile_cccd", ""),
            cccdIssueDate = SecurePreferencesManager.getString(context, "profile_cccd_issue_date", ""),
            cccdIssuePlace = SecurePreferencesManager.getString(context, "profile_cccd_issue_place", "")
        )
    }

    /**
     * Kiểm tra profile completion status từ server
     */
    suspend fun checkProfileCompletionFromServer(userId: Long): AuthResult<UserProfileResponse> {
        return try {
            val response = apiService.checkProfileCompletion(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    AuthResult.Success(it)
                } ?: AuthResult.Error("Failed to check profile status")
            } else {
                AuthResult.Error("Failed to check profile status")
            }
        } catch (e: Exception) {
            Log.e("SecureAuthRepository", "Check profile exception: ${e.message}")
            AuthResult.Error(e.message ?: "Network error")
        }
    }
}