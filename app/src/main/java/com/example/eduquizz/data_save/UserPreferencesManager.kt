package com.example.eduquizz.data_save

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

// Extension property để tạo DataStore 1 lần duy nhất
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesManager(private val context: Context) {

    // --- Thông tin người chơi ---
    val playerNameFlow: Flow<String> = context.dataStore.data
        .map { it[UserPreferencesKeys.PLAYER_NAME] ?: "" }

    val playerAgeFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.PLAYER_AGE] ?: 0 }

    val playerHobbiesSubjectFlow: Flow<String> = context.dataStore.data
        .map { it[UserPreferencesKeys.PLAYER_HOBBIES_SUBJECT] ?: "" }

    val goldFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.GOLD] ?: 0 }
    val playerBirthdayFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[UserPreferencesKeys.BIRTHDAY] ?: "01/01/2000"
        }
    val currentLevelFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.CURRENT_LEVEL] ?: 1 }

    val firstTimeInGame: Flow<Boolean> = context.dataStore.data
        .map { it[UserPreferencesKeys.FIRST_TIME] == true }

    // --- Thống kê ---
    val numTotalQuestionsFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.NUM_TOTAL_QUESTION] ?: 0 }

    val numCorrectAnsweredQuestionsFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.NUM_CORRECT_ANS_QUESTION] ?: 0 }

    val numCorrectAllQuestionsFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.NUM_CORRECT_ALL_QUESTION] ?: 0 }

    val numCorrectAbove50PercentFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.NUM_CORRECT_ABOVE_50_PERCENT_QUES] ?: 0 }

    val numCorrectBelow50PercentFlow: Flow<Int> = context.dataStore.data
        .map { it[UserPreferencesKeys.NUM_CORRECT_BELOW_50_PERCENT_QUES] ?: 0 }
    val boolMusicFlow: Flow<Boolean> =context.dataStore.data
        .map { it[UserPreferencesKeys.music] != false }
    val boolSfxFlow: Flow<Boolean> =context.dataStore.data
        .map { it[UserPreferencesKeys.sfx] != false }

    val lastSeenTsFlow: Flow<Long> = context.dataStore.data
        .map { it[UserPreferencesKeys.LAST_SEEN_TS] ?: 0L }
    // --- Lưu thông tin người chơi ---
    suspend fun firstTimeInPlayGame(){
        context.dataStore.edit {
            it[UserPreferencesKeys.FIRST_TIME] = true
        }
    }
    suspend fun savePlayerName(name: String) {
        context.dataStore.edit {
            it[UserPreferencesKeys.PLAYER_NAME] = name
        }
    }

    suspend fun saveLastSeenTs(epochMillis: Long) {
        context.dataStore.edit {
            it[UserPreferencesKeys.LAST_SEEN_TS] = epochMillis
        }
    }

    suspend fun savePlayerAge(age: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.PLAYER_AGE] = age
        }
    }

    suspend fun savePlayerHobbiesSubject(subject: String) {
        context.dataStore.edit {
            it[UserPreferencesKeys.PLAYER_HOBBIES_SUBJECT] = subject
        }
    }

    suspend fun saveGold(gold: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.GOLD] = gold
        }
    }

    suspend fun saveCurrentLevel(level: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.CURRENT_LEVEL] = level
        }
    }

    suspend fun saveFirstTime(flag: Boolean) {
        context.dataStore.edit {
            it[UserPreferencesKeys.FIRST_TIME] = flag
        }
    }
    suspend fun editmusic(flag: Boolean){
        context.dataStore.edit {
            it[UserPreferencesKeys.music] = flag
        }
    }
    suspend fun sfxmusic(flag: Boolean){
        context.dataStore.edit {
            it[UserPreferencesKeys.sfx] = flag
        }
    }
    suspend fun addGold(amount: Int) {
        val currentGold = goldFlow.first()
        saveGold(currentGold + amount)
    }

    // --- Lưu thống kê ---
    suspend fun saveNumTotalQuestions(count: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.NUM_TOTAL_QUESTION] = count
        }
    }

    suspend fun saveNumCorrectAnsweredQuestions(count: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.NUM_CORRECT_ANS_QUESTION] = count
        }
    }

    suspend fun saveNumCorrectAllQuestions(count: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.NUM_CORRECT_ALL_QUESTION] = count
        }
    }

    suspend fun saveNumCorrectAbove50Percent(count: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.NUM_CORRECT_ABOVE_50_PERCENT_QUES] = count
        }
    }

    suspend fun saveNumCorrectBelow50Percent(count: Int) {
        context.dataStore.edit {
            it[UserPreferencesKeys.NUM_CORRECT_BELOW_50_PERCENT_QUES] = count
        }
    }
    suspend fun addNumTotalQuestions(amount: Int = 1) {
        val current = numTotalQuestionsFlow.first()
        saveNumTotalQuestions(current + amount)
    }

    suspend fun addNumCorrectAnsweredQuestions(amount: Int = 1) {
        val current = numCorrectAnsweredQuestionsFlow.first()
        saveNumCorrectAnsweredQuestions(current + amount)
    }

    suspend fun addNumCorrectAllQuestions(amount: Int = 1) {
        val current = numCorrectAllQuestionsFlow.first()
        saveNumCorrectAllQuestions(current + amount)
    }

    suspend fun addNumCorrectAbove50Percent(amount: Int = 1) {
        val current = numCorrectAbove50PercentFlow.first()
        saveNumCorrectAbove50Percent(current + amount)
    }

    suspend fun addNumCorrectBelow50Percent(amount: Int = 1) {
        val current = numCorrectBelow50PercentFlow.first()
        saveNumCorrectBelow50Percent(current + amount)
    }
    suspend fun savePlayerBirthday(birthday: String) {
        context.dataStore.edit {
            it[UserPreferencesKeys.BIRTHDAY] = birthday
        }
    }


}
