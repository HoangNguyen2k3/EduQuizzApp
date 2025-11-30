package com.example.eduquizz.features.auth.data.repository

import android.content.Context
import android.util.Log
import com.example.eduquizz.features.auth.data.AuthPreferencesManager
import com.example.eduquizz.features.auth.data.api.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AuthApiService,
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient,
    @ApplicationContext private val context: Context
) {
    private val authPrefs = AuthPreferencesManager(context)

    // Backend Auth Methods
    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String
    ): AuthResult<UserResponse> {
        return try {
            val request = RegisterRequest(username, email, password, fullName)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    // Save session
                    authPrefs.saveUserSession(
                        userId = user.id,
                        username = user.username,
                        email = user.email,
                        fullName = user.fullName,
                        profileImageUrl = user.profileImageUrl,
                        authToken = null,
                        loginMethod = "email"
                    )
                    AuthResult.Success(user)
                } ?: AuthResult.Error("User data not found")
            } else {
                AuthResult.Error(response.body()?.message ?: "Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun login(usernameOrEmail: String, password: String): AuthResult<UserResponse> {
        return try {
            val request = LoginRequest(usernameOrEmail, password)
            val response = apiService.login(request)

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    // Save session
                    authPrefs.saveUserSession(
                        userId = user.id,
                        username = user.username,
                        email = user.email,
                        fullName = user.fullName,
                        profileImageUrl = user.profileImageUrl,
                        authToken = null,
                        loginMethod = "email"
                    )
                    AuthResult.Success(user)
                } ?: AuthResult.Error("User data not found")
            } else {
                AuthResult.Error(response.body()?.message ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error")
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

    suspend fun checkUsernameAvailability(username: String): Boolean {
        return try {
            val response = apiService.checkUsername(username)
            response.body()?.get("exists") == false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkEmailAvailability(email: String): Boolean {
        return try {
            val response = apiService.checkEmail(email)
            response.body()?.get("exists") == false
        } catch (e: Exception) {
            false
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
                AuthResult.Error("Firebase user is null")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Exception occurred", e)
            AuthResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult<com.google.firebase.auth.FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign-in failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Authentication error")
        }
    }

    suspend fun registerWithEmailPassword(email: String, password: String): AuthResult<com.google.firebase.auth.FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

            authResult.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Registration failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration error")
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()
        // Clear session asynchronously
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            authPrefs.clearSession()
        }
    }

    suspend fun isUserLoggedIn() = authPrefs.isUserLoggedIn()
}