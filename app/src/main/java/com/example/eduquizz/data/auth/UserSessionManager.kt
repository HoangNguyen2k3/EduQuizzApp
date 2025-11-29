package com.example.eduquizz.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

@Singleton
class UserSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val AUTH_PROVIDER = stringPreferencesKey("auth_provider")
    }

    // Save user session
    suspend fun saveUserSession(
        userId: Long,
        username: String,
        email: String,
        displayName: String? = null,
        photoUrl: String? = null,
        authProvider: String = "local"
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USERNAME] = username
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.DISPLAY_NAME] = displayName ?: username
            preferences[PreferencesKeys.PHOTO_URL] = photoUrl ?: ""
            preferences[PreferencesKeys.AUTH_PROVIDER] = authProvider
        }
    }

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }

    // Get username
    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USERNAME]
        }

    // Get email
    val email: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EMAIL]
        }

    // Get display name
    val displayName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DISPLAY_NAME]
        }

    // Get photo URL
    val photoUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PHOTO_URL]
        }

    // Get user ID
    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }

    // Clear user session (logout)
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // Get all user data
    data class UserSession(
        val isLoggedIn: Boolean = false,
        val userId: Long? = null,
        val username: String? = null,
        val email: String? = null,
        val displayName: String? = null,
        val photoUrl: String? = null,
        val authProvider: String? = null
    )

    val userSession: Flow<UserSession> = context.dataStore.data
        .map { preferences ->
            UserSession(
                isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
                userId = preferences[PreferencesKeys.USER_ID],
                username = preferences[PreferencesKeys.USERNAME],
                email = preferences[PreferencesKeys.EMAIL],
                displayName = preferences[PreferencesKeys.DISPLAY_NAME],
                photoUrl = preferences[PreferencesKeys.PHOTO_URL],
                authProvider = preferences[PreferencesKeys.AUTH_PROVIDER]
            )
        }
}