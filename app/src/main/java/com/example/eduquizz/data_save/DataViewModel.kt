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
    val birthDay = userPrefs.playerBirthdayFlow.asLiveData()
    // --- Cập nhật thông tin người chơi ---
    fun updateFirstTime(){
        viewModelScope.launch {
            userPrefs.firstTimeInPlayGame()
        }
    }

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
    fun addTotalQuestions(amount: Int = 1) {
        viewModelScope.launch {
            userPrefs.addNumTotalQuestions(amount)
        }
    }

    fun addCorrectAnsweredQuestions(amount: Int = 1) {
        viewModelScope.launch {
            userPrefs.addNumCorrectAnsweredQuestions(amount)
        }
    }

    fun addCorrectAllQuestions(amount: Int = 1) {
        viewModelScope.launch {
            userPrefs.addNumCorrectAllQuestions(amount)
        }
    }

    fun addCorrectAbove50Percent(amount: Int = 1) {
        viewModelScope.launch {
            userPrefs.addNumCorrectAbove50Percent(amount)
        }
    }

    fun addCorrectBelow50Percent(amount: Int = 1) {
        viewModelScope.launch {
            userPrefs.addNumCorrectBelow50Percent(amount)
        }
    }
    fun editBirthday(amount: String) {
        viewModelScope.launch {
            userPrefs.savePlayerBirthday(amount)
        }
    }
}
