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

    // --- Thông tin người chơi ---
    val playerName = userPrefs.playerNameFlow.asLiveData()
    val playerAge = userPrefs.playerAgeFlow.asLiveData()
    val playerHobbiesSubject = userPrefs.playerHobbiesSubjectFlow.asLiveData()
    val gold = userPrefs.goldFlow.asLiveData()
    val currentLevel = userPrefs.currentLevelFlow.asLiveData()
    val firstTime = userPrefs.firstTimeInGame.asLiveData()

    // --- Thống kê ---
    val numTotalQuestions = userPrefs.numTotalQuestionsFlow.asLiveData()
    val numCorrectAnsweredQuestions = userPrefs.numCorrectAnsweredQuestionsFlow.asLiveData()
    val numCorrectAllQuestions = userPrefs.numCorrectAllQuestionsFlow.asLiveData()
    val numCorrectAbove50Percent = userPrefs.numCorrectAbove50PercentFlow.asLiveData()
    val numCorrectBelow50Percent = userPrefs.numCorrectBelow50PercentFlow.asLiveData()

    // --- Cập nhật thông tin người chơi ---
    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            userPrefs.savePlayerName(name)
        }
    }

    fun updatePlayerAge(age: Int) {
        viewModelScope.launch {
            userPrefs.savePlayerAge(age)
        }
    }

    fun updatePlayerHobbiesSubject(subject: String) {
        viewModelScope.launch {
            userPrefs.savePlayerHobbiesSubject(subject)
        }
    }

    fun updateGold(amount: Int) {
        viewModelScope.launch {
            userPrefs.saveGold(amount)
        }
    }

    fun addGold(amount: Int) {
        viewModelScope.launch {
            userPrefs.addGold(amount)
        }
    }

    fun updateLevel(level: Int) {
        viewModelScope.launch {
            userPrefs.saveCurrentLevel(level)
        }
    }

    fun setFirstTime(flag: Boolean) {
        viewModelScope.launch {
            userPrefs.saveFirstTime(flag)
        }
    }

    // --- Cập nhật thống kê ---
    fun updateTotalQuestions(count: Int) {
        viewModelScope.launch {
            userPrefs.saveNumTotalQuestions(count)
        }
    }

    fun updateCorrectAnsweredQuestions(count: Int) {
        viewModelScope.launch {
            userPrefs.saveNumCorrectAnsweredQuestions(count)
        }
    }

    fun updateCorrectAllQuestions(count: Int) {
        viewModelScope.launch {
            userPrefs.saveNumCorrectAllQuestions(count)
        }
    }

    fun updateCorrectAbove50Percent(count: Int) {
        viewModelScope.launch {
            userPrefs.saveNumCorrectAbove50Percent(count)
        }
    }

    fun updateCorrectBelow50Percent(count: Int) {
        viewModelScope.launch {
            userPrefs.saveNumCorrectBelow50Percent(count)
        }
    }
}
