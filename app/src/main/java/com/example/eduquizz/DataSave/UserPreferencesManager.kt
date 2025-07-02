package com.example.eduquizz.DataSave

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property để tạo DataStore 1 lần duy nhất
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesManager(private val context: Context) {

    val playerNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.PLAYER_NAME] ?: ""
        }

    val goldFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.GOLD] ?: 0
        }

    val currentLevelFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.CURRENT_LEVEL] ?: 1
        }
    val firstTimeInGame: Flow<Boolean> =context.dataStore.data
        .map {
            preferences ->
            preferences[UserPreferencesKeys.FIRST_TIME] ?: false
        }
    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.PLAYER_NAME] = name
        }
    }

    suspend fun saveGold(gold: Int) {
        context.dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.GOLD] = gold
        }
    }

    suspend fun saveCurrentLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.CURRENT_LEVEL] = level
        }
    }
}