package com.example.eduquizz.data_save

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val userPrefs: UserPreferencesManager,
    private val secureDataStore: SecureDataStoreManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Sử dụng encrypted DataStore
    val playerName = secureDataStore.playerNameFlow.asLiveData()
    val playerAge = secureDataStore.playerAgeFlow.asLiveData()
    val avatarUri = secureDataStore.avatarUriFlow.asLiveData()
    val birthDay = secureDataStore.birthdayFlow.asLiveData()

    // Non-encrypted data
    val gold = secureDataStore.goldFlow.asLiveData()
    val currentLevel = secureDataStore.currentLevelFlow.asLiveData()

    val playerHobbiesSubject = userPrefs.playerHobbiesSubjectFlow.asLiveData()
    val firstTime = userPrefs.firstTimeInGame.asLiveData()
val music = userPrefs.boolMusicFlow.asLiveData()
    val sfx = userPrefs.boolSfxFlow.asLiveData()
    val lastSeenTs = userPrefs.lastSeenTsFlow.asLiveData()
    // --- Thống kê ---
    val numTotalQuestions = userPrefs.numTotalQuestionsFlow.asLiveData()
    val numCorrectAnsweredQuestions = userPrefs.numCorrectAnsweredQuestionsFlow.asLiveData()
    val numCorrectAllQuestions = userPrefs.numCorrectAllQuestionsFlow.asLiveData()
    val numCorrectAbove50Percent = userPrefs.numCorrectAbove50PercentFlow.asLiveData()
    val numCorrectBelow50Percent = userPrefs.numCorrectBelow50PercentFlow.asLiveData()

    fun UpdateMusic(flag: Boolean){
        viewModelScope.launch {
            userPrefs.editmusic(flag)
        }
    }
    fun UpdateSfx(flag: Boolean){
        viewModelScope.launch {
            userPrefs.sfxmusic(flag)
        }
    }

    fun updateLastSeenNow() {
        viewModelScope.launch {
            userPrefs.saveLastSeenTs(System.currentTimeMillis())
        }
    }
    fun updateFirstTime(){
        viewModelScope.launch {
            userPrefs.firstTimeInPlayGame()
        }
    }

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            secureDataStore.savePlayerName(name)
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
            secureDataStore.saveGold(amount)
        }
    }

    fun addGold(amount: Int) {
        viewModelScope.launch {
            secureDataStore.addGold(amount)
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

    fun updateAvatar(uri: String) {
        viewModelScope.launch {
            secureDataStore.saveAvatarUri(uri)
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
