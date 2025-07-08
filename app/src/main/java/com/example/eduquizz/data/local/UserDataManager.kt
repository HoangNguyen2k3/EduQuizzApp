package com.example.eduquizz.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@HiltViewModel
class UserViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val userNameKey = stringPreferencesKey("user_name")
    private val completedLessonsKey = stringSetPreferencesKey("completed_lessons")

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            context.dataStore.data.map { preferences ->
                preferences[userNameKey] ?: ""
            }.collect { name ->
                _userName.value = name
            }
        }
    }

    fun setUserName(name: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[userNameKey] = name
            }
            _userName.value = name
        }
    }

    fun clearUserName() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences.remove(userNameKey)
            }
            _userName.value = ""
        }
    }

    fun getUserNameFlow(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[userNameKey] ?: ""
        }
    }

    val completedLessons: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[completedLessonsKey] ?: emptySet()
    }

    fun addCompletedLesson(lessonId: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val currentSet = preferences[completedLessonsKey] ?: emptySet()
                preferences[completedLessonsKey] = currentSet + lessonId
            }
        }
    }

    fun removeCompletedLesson(lessonId: String) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                val currentSet = preferences[completedLessonsKey] ?: emptySet()
                preferences[completedLessonsKey] = currentSet - lessonId
            }
        }
    }

    fun clearCompletedLessons() {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences.remove(completedLessonsKey)
            }
        }
    }
}