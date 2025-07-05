package com.example.eduquizz.data_save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val userPrefs: UserPreferencesManager
) : ViewModel() {
    val playerName = userPrefs.playerNameFlow.asLiveData()
    val gold = userPrefs.goldFlow.asLiveData()
    val currentLevel = userPrefs.currentLevelFlow.asLiveData()

    public fun updatePlayerName(name: String) {
        viewModelScope.launch {
            userPrefs.savePlayerName(name)
        }
    }

    public fun updateGold(amount: Int) {
        viewModelScope.launch {
            userPrefs.saveGold(amount)
        }
    }

    public fun updateLevel(level: Int) {
        viewModelScope.launch {
            userPrefs.saveCurrentLevel(level)
        }
    }
}