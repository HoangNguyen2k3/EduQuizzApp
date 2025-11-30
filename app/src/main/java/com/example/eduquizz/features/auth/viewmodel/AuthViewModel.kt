package com.example.eduquizz.features.auth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.auth.data.api.UserResponse
import com.example.eduquizz.features.auth.data.repository.AuthRepository
import com.example.eduquizz.features.auth.data.repository.AuthResult
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
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = repository.isUserLoggedIn()
            _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
        }
    }

    fun login(usernameOrEmail: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.login(usernameOrEmail, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.data,
                        successMessage = "Login successful!"
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun register(username: String, email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            when (val result = repository.register(username, email, password, fullName)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        currentUser = result.data,
                        successMessage = "Registration successful! Redirecting to login..."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                else -> {}
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
}