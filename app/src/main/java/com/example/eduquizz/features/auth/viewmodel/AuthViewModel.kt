package com.example.eduquizz.features.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.auth.data.api.UserResponse
import com.example.eduquizz.features.auth.data.repository.SecureAuthRepository
import com.example.eduquizz.features.auth.data.repository.AuthResult
import com.example.eduquizz.security.InputValidator
import com.example.eduquizz.security.RateLimiter
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: UserResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAdmin: Boolean = false,  // NEW: Track admin status
    // Brute-force protection
    val remainingAttempts: Int? = null,
    val requiresCaptcha: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: SecureAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = repository.isUserLoggedIn()
            if (isLoggedIn) {
                // Load saved user data if logged in
                loadSavedUserData()
            }
            _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
        }
    }

    // NEW: Load saved user data from repository
    private suspend fun loadSavedUserData() {
        val savedUser = repository.getSavedUser()
        if (savedUser != null) {
            _uiState.value = _uiState.value.copy(
                currentUser = savedUser,
                isAdmin = savedUser.role == "ADMIN"
            )
        }
    }

    fun login(usernameOrEmail: String, password: String, captchaToken: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // === INPUT VALIDATION ===
            // 1. Validate input cho SQL Injection & XSS
            if (InputValidator.isMalicious(usernameOrEmail)) {
                Log.w("AuthViewModel", "🚨 Malicious input detected in usernameOrEmail")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Tên đăng nhập chứa ký tự không hợp lệ"
                )
                return@launch
            }
            
            // 2. Check rate limiting (client-side)
            val rateLimitKey = usernameOrEmail.lowercase().trim()
            if (!RateLimiter.loginLimiter.isAllowed(rateLimitKey)) {
                val blockTime = RateLimiter.loginLimiter.getBlockTimeRemainingFormatted(rateLimitKey)
                Log.w("AuthViewModel", "⏱️ Rate limited: $rateLimitKey, blocked for $blockTime")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Quá nhiều lần thử. Vui lòng đợi $blockTime",
                    remainingAttempts = 0,
                    requiresCaptcha = true
                )
                return@launch
            }
            
            // 3. Sanitize input
            val cleanUsername = usernameOrEmail.trim()
            
            when (val result = repository.login(cleanUsername, password, captchaToken)) {
                is AuthResult.Success -> {
                    // Đăng nhập thành công → Reset rate limiter
                    RateLimiter.loginLimiter.recordSuccess(rateLimitKey)
                    
                    val user = result.data
                    val isAdmin = user.role == "ADMIN"

                    Log.d("AuthViewModel", "Login successful - User: ${user.username}, Role: ${user.role}, IsAdmin: $isAdmin")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        isAdmin = isAdmin,
                        successMessage = "Login successful!",
                        remainingAttempts = null,
                        requiresCaptcha = false
                    )
                }
                is AuthResult.Error -> {
                    // Đăng nhập thất bại → Record attempt
                    RateLimiter.loginLimiter.recordAttempt(rateLimitKey)
                    val remaining = RateLimiter.loginLimiter.getRemainingAttempts(rateLimitKey)
                    val needsCaptcha = RateLimiter.loginLimiter.requiresCaptcha(rateLimitKey)
                    
                    Log.e("AuthViewModel", "Login failed: ${result.message}")
                    Log.d("AuthViewModel", "Client remaining attempts: $remaining")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message,
                        remainingAttempts = result.remainingAttempts ?: remaining,
                        requiresCaptcha = result.requiresCaptcha || needsCaptcha
                    )
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            // === INPUT VALIDATION ===
            // 1. Validate username
            val usernameResult = InputValidator.validateUsername(username)
            if (!usernameResult.isValid()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = usernameResult.getErrors().first()
                )
                return@launch
            }
            
            // 2. Validate email
            val emailResult = InputValidator.validateEmail(email)
            if (!emailResult.isValid()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = emailResult.getErrors().first()
                )
                return@launch
            }
            
            // 3. Validate password
            val passwordResult = InputValidator.validatePassword(password)
            if (!passwordResult.isValid()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = passwordResult.getErrors().first()
                )
                return@launch
            }
            
            // 4. Check fullName cho malicious content
            if (InputValidator.isMalicious(fullName)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Họ tên chứa ký tự không hợp lệ"
                )
                return@launch
            }
            
            // 5. Sanitize inputs
            val cleanUsername = username.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanFullName = InputValidator.sanitizeHtml(fullName.trim())

            when (val result = repository.register(cleanUsername, cleanEmail, password, cleanFullName)) {
                is AuthResult.Success -> {
                    val user = result.data
                    val isAdmin = user.role == "ADMIN"

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = user,
                        isAdmin = isAdmin,
                        successMessage = "Registration successful! Redirecting to login..."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "=== signInWithGoogle Called ===")
            Log.d("AuthViewModel", "Account: ${account.email}")

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.signInWithGoogle(account)) {
                is AuthResult.Success -> {
                    Log.d("AuthViewModel", "✅ Repository returned Success")
                    val firebaseUser = result.data
                    // Convert Firebase user to UserResponse
                    val userResponse = UserResponse(
                        id = 0,
                        username = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "user",
                        email = firebaseUser.email ?: "",
                        fullName = firebaseUser.displayName,
                        role = "USER",  // NEW: Default role for Google sign-in
                        phoneNumber = firebaseUser.phoneNumber,
                        profileImageUrl = firebaseUser.photoUrl?.toString(),
                        createdAt = null,
                        lastLogin = null
                    )

                    Log.d("AuthViewModel", "Setting state: isLoggedIn=true")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = userResponse,
                        isAdmin = false,  // NEW: Google users are not admin by default
                        successMessage = "Google sign-in successful!"
                    )

                    Log.d("AuthViewModel", "✅ State updated successfully")

                    // Force update login state immediately
                    kotlinx.coroutines.delay(100)
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "❌ Repository returned Error")
                    Log.e("AuthViewModel", "Message: ${result.message}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {
                    Log.w("AuthViewModel", "⚠️ Unexpected result type")
                }
            }
        }
    }

    fun updateFullName(newFullName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val username = _uiState.value.currentUser?.username ?: return@launch

            when (val result = repository.updateProfile(
                username = username,
                fullName = newFullName,
                phoneNumber = null,
                profileImageUrl = null
            )) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = result.data,
                        successMessage = "Cập nhật thành công!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updatePhoneNumber(newPhoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val username = _uiState.value.currentUser?.username ?: return@launch

            when (val result = repository.updateProfile(
                username = username,
                fullName = null,
                phoneNumber = newPhoneNumber,
                profileImageUrl = null
            )) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = result.data,
                        successMessage = "Cập nhật thành công!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun updateProfileImage(imageUrl: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val username = _uiState.value.currentUser?.username ?: return@launch

            when (val result = repository.updateProfile(
                username = username,
                fullName = null,
                phoneNumber = null,
                profileImageUrl = imageUrl
            )) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = result.data,
                        successMessage = "Cập nhật ảnh thành công!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun getGoogleSignInClient() = repository.getGoogleSignInClient()

    // NEW: Helper function to check if current user is admin
    fun isCurrentUserAdmin(): Boolean {
        return _uiState.value.isAdmin
    }

    // NEW: Get current username (useful for admin API calls)
    fun getCurrentUsername(): String {
        return _uiState.value.currentUser?.username ?: ""
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "=== Logout Started ===")

                // Sign out from Google if logged in via Google
                repository.getGoogleSignInClient().signOut().addOnCompleteListener {
                    Log.d("AuthViewModel", "Google sign out complete")
                }

                // Sign out from repository (clears saved session)
                repository.signOut()

                // Clear user session state
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    currentUser = null,
                    isAdmin = false,
                    errorMessage = null,
                    successMessage = null
                )

                Log.d("AuthViewModel", "=== Logout Complete ===")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error: ${e.message}", e)
            }
        }
    }

    // Security Features - Password Reset
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = repository.forgotPassword(email)) {
                is AuthResult.Success -> {
                    Log.d("AuthViewModel", "Forgot password success: ${result.data}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data
                    )
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "Forgot password error: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    fun verifyPinAndResetPassword(email: String, pin: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = repository.verifyPinAndResetPassword(email, pin, newPassword)) {
                is AuthResult.Success -> {
                    Log.d("AuthViewModel", "Password reset success: ${result.data}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = result.data
                    )
                }
                is AuthResult.Error -> {
                    Log.e("AuthViewModel", "Password reset error: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    // NEW: Check if user profile is completed
    fun isProfileCompleted(): Boolean {
        return repository.isProfileCompleted()
    }

    // NEW: Expose repository for accessing profile data
    fun getRepository(): SecureAuthRepository {
        return repository
    }
}