package com.example.eduquizz.features.auth.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class AuthPreferencesManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = longPreferencesKey("user_id")
        private val USERNAME = stringPreferencesKey("username")
        private val EMAIL = stringPreferencesKey("email")
        private val FULL_NAME = stringPreferencesKey("full_name")
        private val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val LOGIN_METHOD = stringPreferencesKey("login_method") // "email", "google"
    }

    val isLoggedInFlow: Flow<Boolean> = context.authDataStore.data
        .map { it[IS_LOGGED_IN] ?: false }

    val userIdFlow: Flow<Long?> = context.authDataStore.data
        .map { it[USER_ID] }

    val usernameFlow: Flow<String?> = context.authDataStore.data
        .map { it[USERNAME] }

    val emailFlow: Flow<String?> = context.authDataStore.data
        .map { it[EMAIL] }

    val fullNameFlow: Flow<String?> = context.authDataStore.data
        .map { it[FULL_NAME] }

    val profileImageUrlFlow: Flow<String?> = context.authDataStore.data
        .map { it[PROFILE_IMAGE_URL] }

    val authTokenFlow: Flow<String?> = context.authDataStore.data
        .map { it[AUTH_TOKEN] }

    val loginMethodFlow: Flow<String?> = context.authDataStore.data
        .map { it[LOGIN_METHOD] }

    suspend fun saveUserSession(
        userId: Long,
        username: String,
        email: String,
        fullName: String?,
        profileImageUrl: String?,
        authToken: String?,
        loginMethod: String
    ) {
        context.authDataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId
            prefs[USERNAME] = username
            prefs[EMAIL] = email
            fullName?.let { prefs[FULL_NAME] = it }
            profileImageUrl?.let { prefs[PROFILE_IMAGE_URL] = it }
            authToken?.let { prefs[AUTH_TOKEN] = it }
            prefs[LOGIN_METHOD] = loginMethod
        }
    }

    suspend fun updateProfileImage(url: String) {
        context.authDataStore.edit { prefs ->
            prefs[PROFILE_IMAGE_URL] = url
        }
    }

    suspend fun updateFullName(name: String) {
        context.authDataStore.edit { prefs ->
            prefs[FULL_NAME] = name
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return context.authDataStore.data.map { it[IS_LOGGED_IN] ?: false }.first()
    }
}