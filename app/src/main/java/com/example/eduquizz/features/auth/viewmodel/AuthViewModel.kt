package com.example.eduquizz.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.example.eduquizz.data.auth.UserSessionManager

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "http://10.0.2.2:8080/api/auth" // For emulator
    // For physical device, use: "http://YOUR_COMPUTER_IP:8080/api/auth"

    val isLoggedIn = sessionManager.isLoggedIn
    val userSession = sessionManager.userSession

    fun register(
        username: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val json = JSONObject().apply {
                        put("username", username)
                        put("email", email)
                        put("password", password)
                    }

                    val requestBody = json.toString()
                        .toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url("$baseUrl/register")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        Result.success(jsonResponse)
                    } else {
                        val errorJson = JSONObject(responseBody)
                        val errorMessage = errorJson.optString("error", "Registration failed")
                        Result.failure(Exception(errorMessage))
                    }
                }

                result.onSuccess {
                    onSuccess()
                }.onFailure { exception ->
                    onError(exception.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun login(
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val json = JSONObject().apply {
                        put("username", username)
                        put("password", password)
                    }

                    val requestBody = json.toString()
                        .toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url("$baseUrl/login")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        Result.success(jsonResponse)
                    } else {
                        val errorJson = JSONObject(responseBody)
                        val errorMessage = errorJson.optString("error", "Login failed")
                        Result.failure(Exception(errorMessage))
                    }
                }

                result.onSuccess { jsonResponse ->
                    // Save user data to local storage or state management
                    val username = jsonResponse.optString("username")
                    val email = jsonResponse.optString("email")
                    // TODO: Save to DataStore or SharedPreferences
                    onSuccess()
                }.onFailure { exception ->
                    onError(exception.message ?: "Login failed")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun loginWithFirebase(
        firebaseUid: String,
        email: String,
        displayName: String?,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val json = JSONObject().apply {
                        put("firebaseUid", firebaseUid)
                        put("email", email)
                        put("displayName", displayName ?: "")
                        put("photoUrl", photoUrl ?: "")
                    }

                    val requestBody = json.toString()
                        .toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url("$baseUrl/firebase-login")
                        .post(requestBody)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        Result.success(jsonResponse)
                    } else {
                        val errorJson = JSONObject(responseBody)
                        val errorMessage = errorJson.optString("error", "Firebase login failed")
                        Result.failure(Exception(errorMessage))
                    }
                }

                result.onSuccess { jsonResponse ->
                    // Save user data to session
                    viewModelScope.launch {
                        sessionManager.saveUserSession(
                            userId = jsonResponse.optLong("id"),
                            username = jsonResponse.optString("username"),
                            email = jsonResponse.optString("email"),
                            displayName = jsonResponse.optString("displayName"),
                            photoUrl = jsonResponse.optString("photoUrl"),
                            authProvider = "local"
                        )
                    }
                    onSuccess()
                }.onFailure { exception ->
                    onError(exception.message ?: "Firebase login failed")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    // Logout function
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearUserSession()
            onSuccess()
        }
    }
}