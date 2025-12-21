package com.example.eduquizz.features.auth.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.eduquizz.features.auth.data.AuthPreferencesManager
import com.example.eduquizz.features.auth.data.api.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

//sealed class AuthResult<out T> {
//    data class Success<T>(val data: T) : AuthResult<T>()
//    data class Error(val message: String) : AuthResult<Nothing>()
//    object Loading : AuthResult<Nothing>()
//}

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(
        val message: String,
        val remainingAttempts: Int? = null,
        val requiresCaptcha: Boolean = false
    ) : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(

    private val apiService: AuthApiService,
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
) {
    private val authPrefs = AuthPreferencesManager(context)

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val gson = Gson()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
    }

    // Save user data after successful login/register
    suspend fun saveUserData(user: UserResponse) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_DATA, gson.toJson(user))
            putString(KEY_USERNAME, user.username)
            putString(KEY_ROLE, user.role)
            // Lưu thêm userId và email để dùng làm unique identifier
            putLong("user_id_backend", user.id)
            putString("email", user.email)
            apply()
        }
        Log.d("AuthRepository", "✅ Saved user data: id=${user.id}, username=${user.username}, email=${user.email}")
    }

    // Get saved user data
    fun getSavedUser(): UserResponse? {
        val userJson = sharedPreferences.getString(KEY_USER_DATA, null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserResponse::class.java)
        } else {
            null
        }
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get saved username
    fun getSavedUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    // Get saved role
    fun getSavedRole(): String? {
        return sharedPreferences.getString(KEY_ROLE, null)
    }

    // Check if saved user is admin
    fun isUserAdmin(): Boolean {
        return getSavedRole() == "ADMIN"
    }

//    // Backend Auth Methods
//    suspend fun register(
//        username: String,
//        email: String,
//        password: String,
//        fullName: String
//    ): AuthResult<UserResponse> {
//        return try {
//            val request = RegisterRequest(username, email, password, fullName)
//            val response = apiService.register(request)
//
//            if (response.isSuccessful && response.body()?.success == true) {
//                response.body()?.user?.let { user ->
//                    // Save session
//                    authPrefs.saveUserSession(
//                        userId = user.id,
//                        username = user.username,
//                        email = user.email,
//                        fullName = user.fullName,
//                        profileImageUrl = user.profileImageUrl,
//                        authToken = null,
//                        loginMethod = "email"
//                    )
//                    AuthResult.Success(user)
//                } ?: AuthResult.Error("User data not found")
//            } else {
//                // Parse error message from backend
//                val errorMsg = response.body()?.message ?: when (response.code()) {
//                    400 -> "Invalid registration data"
//                    409 -> "Username or email already exists"
//                    else -> "Registration failed"
//                }
//                AuthResult.Error(errorMsg)
//            }
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Register exception: ${e.message}", e)
//            AuthResult.Error("Network error. Please check your connection.")
//        }
//    }
// Register function (updated to save user data)
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
                // Save user data
                saveUserData(registerResponse.user)
                AuthResult.Success(registerResponse.user)
            } else {
                AuthResult.Error(registerResponse?.message ?: "Registration failed")
            }
        } else {
            AuthResult.Error("Registration failed: ${response.message()}")
        }
    } catch (e: Exception) {
        AuthResult.Error(e.message ?: "Unknown error occurred")
    }
}

//    suspend fun login(usernameOrEmail: String, password: String): AuthResult<UserResponse> {
//        return try {
//            val request = LoginRequest(usernameOrEmail, password)
//            val response = apiService.login(request)
//
//            Log.d("AuthRepository", "Login response code: ${response.code()}")
//            Log.d("AuthRepository", "Login response success: ${response.body()?.success}")
//
//            if (response.isSuccessful && response.body()?.success == true) {
//                response.body()?.user?.let { user ->
//                    // Save session
//                    authPrefs.saveUserSession(
//                        userId = user.id,
//                        username = user.username,
//                        email = user.email,
//                        fullName = user.fullName,
//                        profileImageUrl = user.profileImageUrl,
//                        authToken = null,
//                        loginMethod = "email"
//                    )
//                    Log.d("AuthRepository", "Login successful: ${user.username}")
//                    AuthResult.Success(user)
//                } ?: AuthResult.Error("User data not found")
//            } else {
//                // Parse detailed error message from backend
//                val errorMsg = response.body()?.message ?: when (response.code()) {
//                    400 -> "Invalid login credentials"
//                    401 -> "Incorrect password. Please try again."
//                    404 -> "Account not found. Please check your username/email."
//                    else -> "Login failed. Please try again."
//                }
//                Log.e("AuthRepository", "Login failed: $errorMsg")
//                AuthResult.Error(errorMsg)
//            }
//        } catch (e: Exception) {
//            Log.e("AuthRepository", "Login exception: ${e.message}", e)
//            AuthResult.Error("Network error. Please check your connection.")
//        }
//    }

    // Login function (updated to save user data)
    suspend fun login(usernameOrEmail: String, password: String, captchaToken: String? = null): AuthResult<UserResponse> {
        return try {
            val response = apiService.login(
                LoginRequest(usernameOrEmail, password, captchaToken)
            )

            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse?.success == true && loginResponse.user != null) {
                    // Save user data
                    saveUserData(loginResponse.user)
                    AuthResult.Success(loginResponse.user)
                } else {
                    // Login failed - parse brute-force info
                    AuthResult.Error(
                        message = loginResponse?.message ?: "Login failed",
                        remainingAttempts = loginResponse?.remainingAttempts,
                        requiresCaptcha = loginResponse?.requiresCaptcha ?: false
                    )
                }
            } else {
                // HTTP error - try to parse error body
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                
                Log.e("AuthRepository", "Login failed: ${response.code()} - ${response.message()}")
                Log.e("AuthRepository", "Error body: $errorBody")
                
                // Try to parse remainingAttempts from error body
                var remainingAttempts: Int? = null
                var requiresCaptcha = false
                var errorMessage = ""
                
                if (errorBody != null) {
                    try {
                        Log.d("AuthRepository", "════════════════════════════")
                        Log.d("AuthRepository", "RAW ERROR BODY: $errorBody")
                        Log.d("AuthRepository", "════════════════════════════")
                        
                        val errorJson = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        remainingAttempts = if (errorJson.has("remainingAttempts")) {
                            errorJson.get("remainingAttempts").asInt
                        } else null
                        
                        requiresCaptcha = if (errorJson.has("requiresCaptcha")) {
                            errorJson.get("requiresCaptcha").asBoolean
                        } else false
                        
                        errorMessage = if (errorJson.has("message")) {
                            errorJson.get("message").asString
                        } else ""
                        
                        Log.d("AuthRepository", "════════════════════════════")
                        Log.d("AuthRepository", "PARSED VALUES:")
                        Log.d("AuthRepository", "  remainingAttempts: $remainingAttempts")
                        Log.d("AuthRepository", "  requiresCaptcha: $requiresCaptcha")
                        Log.d("AuthRepository", "  errorMessage: $errorMessage")
                        Log.d("AuthRepository", "════════════════════════════")
                    } catch (e: Exception) {
                        Log.e("AuthRepository", "Failed to parse error body: ${e.message}")
                    }
                }
                
                // Set remainingAttempts to 0 if status is 429 (blocked)
                if (response.code() == 429) {
                    Log.d("AuthRepository", "⚠️ HTTP 429 detected - BEFORE: remainingAttempts = $remainingAttempts")
                    remainingAttempts = 0
                    requiresCaptcha = true
                    Log.d("AuthRepository", "⚠️ HTTP 429 detected - AFTER: remainingAttempts = $remainingAttempts")
                }
                
                Log.d("AuthRepository", "════════════════════════════")
                Log.d("AuthRepository", "FINAL RESULT TO RETURN:")
                Log.d("AuthRepository", "  HTTP Code: ${response.code()}")
                Log.d("AuthRepository", "  remainingAttempts: $remainingAttempts")
                Log.d("AuthRepository", "  requiresCaptcha: $requiresCaptcha")
                Log.d("AuthRepository", "════════════════════════════")
                
                AuthResult.Error(
                    message = errorMessage.ifEmpty {
                        when (response.code()) {
                            401 -> "Incorrect username or password"
                            429 -> "Too many failed attempts. Please try again later."
                            else -> "Login failed: ${response.message()}"
                        }
                    },
                    remainingAttempts = remainingAttempts,
                    requiresCaptcha = requiresCaptcha
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login exception: ${e.message}", e)
            AuthResult.Error(
                message = e.message ?: "Network error. Please check your connection.",
                remainingAttempts = null,
                requiresCaptcha = false
            )
        }
    }

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

    // Firebase Google Sign-In Methods
    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient

    suspend fun signInWithGoogle(account: GoogleSignInAccount): AuthResult<FirebaseUser> {
        return try {
            Log.d("AuthRepository", "=== signInWithGoogle ===")
            Log.d("AuthRepository", "Account: ${account.email}")
            Log.d("AuthRepository", "ID Token: ${account.idToken?.take(20)}...")

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("AuthRepository", "✅ Credential created")

            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Log.d("AuthRepository", "✅ Firebase auth completed")

            authResult.user?.let { firebaseUser ->
                Log.d("AuthRepository", "✅ Firebase user: ${firebaseUser.email}")

                // Save session
                authPrefs.saveUserSession(
                    userId = 0,
                    username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "user",
                    email = firebaseUser.email ?: "",
                    fullName = firebaseUser.displayName,
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    authToken = account.idToken,
                    loginMethod = "google"
                )
                Log.d("AuthRepository", "✅ Session saved")

                // Verify
                val isLoggedIn = authPrefs.isUserLoggedIn()
                Log.d("AuthRepository", "✅ Verify isLoggedIn: $isLoggedIn")

                AuthResult.Success(firebaseUser)
            } ?: run {
                Log.e("AuthRepository", "❌ Firebase user is null")
                AuthResult.Error("Google sign-in failed. Please try again.")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception occurred", e)
            AuthResult.Error(e.message ?: "Google sign-in error")
        }
    }

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign-in failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Authentication error")
        }
    }

    suspend fun registerWithEmailPassword(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Registration failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration error")
        }
    }

//    fun signOut() {
//        firebaseAuth.signOut()
//        googleSignInClient.signOut()
//        // Clear session asynchronously
//        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
//            authPrefs.clearSession()
//        }
//    }

    fun signOut() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
        // Also sign out from Firebase if needed
        firebaseAuth.signOut()
    }

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
                    // Update saved user data
                    saveUserData(updateResponse.user)
                    AuthResult.Success(updateResponse.user)
                } else {
                    AuthResult.Error(updateResponse?.message ?: "Update failed")
                }
            } else {
                AuthResult.Error("Update failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Update profile exception: ${e.message}", e)
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    // Security Features - Password Reset
    suspend fun forgotPassword(email: String): AuthResult<String> {
        return try {
            Log.d("AuthRepository", "Sending forgot password request for: $email")
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d("AuthRepository", "Forgot password success: ${body.message}")
                    AuthResult.Success(body.message)
                } else {
                    val errorMsg = body?.message ?: "Failed to send reset PIN"
                    Log.e("AuthRepository", "Forgot password failed: $errorMsg")
                    AuthResult.Error(errorMsg)
                }
            } else {
                // Parse error message from backend response body
                val errorBody = try {
                    response.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                
                val errorMsg = when (response.code()) {
                    404 -> "Email không tồn tại trong hệ thống"
                    400 -> "Yêu cầu không hợp lệ"
                    500 -> "Lỗi server. Vui lòng thử lại sau"
                    else -> "Không thể gửi mã PIN (${response.code()})"
                }
                
                Log.e("AuthRepository", "API error: ${response.code()} - ${response.message()}")
                Log.e("AuthRepository", "Error body: $errorBody")
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Forgot password exception: ${e.message}", e)
            AuthResult.Error("Lỗi kết nối. Vui lòng kiểm tra internet.")
        }
    }

    suspend fun verifyPinAndResetPassword(
        email: String,
        pin: String,
        newPassword: String
    ): AuthResult<String> {
        return try {
            Log.d("AuthRepository", "Verifying PIN for: $email")
            val response = apiService.verifyPinAndResetPassword(
                VerifyPinRequest(email, pin, newPassword)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Log.d("AuthRepository", "PIN verified and password reset: ${body.message}")
                    AuthResult.Success(body.message)
                } else {
                    val errorMsg = body?.message ?: "Failed to reset password"
                    Log.e("AuthRepository", "Verify PIN failed: $errorMsg")
                    AuthResult.Error(errorMsg)
                }
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Mã PIN không đúng hoặc đã hết hạn"
                    404 -> "Email không tồn tại"
                    else -> "Đặt lại mật khẩu thất bại"
                }
                Log.e("AuthRepository", "API error: ${response.code()} - ${response.message()}")
                AuthResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Verify PIN exception: ${e.message}", e)
            AuthResult.Error("Lỗi kết nối. Vui lòng kiểm tra internet.")
        }
    }
}
