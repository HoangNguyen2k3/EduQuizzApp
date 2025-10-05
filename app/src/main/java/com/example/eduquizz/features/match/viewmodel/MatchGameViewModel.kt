package com.example.eduquizz.features.match.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.match.model.*
import com.example.eduquizz.features.match.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchGameUiState(
    val isLoading: Boolean = false,
    val currentLevel: MatchLevel? = null,
    val allLevels: List<MatchLevel> = emptyList(),
    val completedLevels: Map<String, Boolean> = emptyMap(),
    val userStats: UserStatistics? = null,
    val error: String? = null,
    val selectedPairs: List<Pair<String, String>> = emptyList(),
    val matchedPairs: Set<String> = emptySet(),
    val attempts: Int = 0,
    val correctMatches: Int = 0,
    val totalPairs: Int = 0,
    val isGameComplete: Boolean = false,
    val startTime: Long = 0L,
    val timeRemaining: Int = 60 // 60 seconds timer
)

@HiltViewModel
class MatchGameViewModel @Inject constructor(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchGameUiState())
    val uiState: StateFlow<MatchGameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun loadAllLevels() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllLevels().fold(
                onSuccess = { levels ->
                    _uiState.value = _uiState.value.copy(
                        allLevels = levels,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to load levels",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun loadLevel(levelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                startTime = System.currentTimeMillis(),
                timeRemaining = 60
            )
            repository.getLevel(levelId).fold(
                onSuccess = { level ->
                    _uiState.value = _uiState.value.copy(
                        currentLevel = level,
                        isLoading = false,
                        matchedPairs = emptySet(),
                        selectedPairs = emptyList(),
                        attempts = 0,
                        correctMatches = 0,
                        totalPairs = level.pairCount,
                        isGameComplete = false
                    )
                    startTimer()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to load level",
                        isLoading = false
                    )
                }
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && !_uiState.value.isGameComplete) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeRemaining = _uiState.value.timeRemaining - 1
                )
            }
            if (_uiState.value.timeRemaining == 0 && !_uiState.value.isGameComplete) {
                // Time's up - game over
                _uiState.value = _uiState.value.copy(isGameComplete = true)
            }
        }
    }

    fun loadUserCompletions(userName: String) {
        viewModelScope.launch {
            repository.getAllCompletions(userName).fold(
                onSuccess = { completions ->
                    _uiState.value = _uiState.value.copy(completedLevels = completions)
                },
                onFailure = { }
            )
        }
    }

    fun loadUserStatistics(userName: String) {
        viewModelScope.launch {
            repository.getUserStatistics(userName).fold(
                onSuccess = { stats ->
                    _uiState.value = _uiState.value.copy(userStats = stats)
                },
                onFailure = { }
            )
        }
    }

    fun selectCard(type: String, value: String) {
        val currentSelected = _uiState.value.selectedPairs

        if (currentSelected.any { it.first == type && it.second == value }) {
            _uiState.value = _uiState.value.copy(
                selectedPairs = currentSelected.filter {
                    !(it.first == type && it.second == value)
                }
            )
            return
        }

        val newSelected = currentSelected + (type to value)

        if (newSelected.size == 2) {
            val word = newSelected.find { it.first == "word" }?.second
            val definition = newSelected.find { it.first == "definition" }?.second

            if (word != null && definition != null) {
                checkMatch(word, definition)
            }
        } else {
            _uiState.value = _uiState.value.copy(selectedPairs = newSelected)
        }
    }

    private fun checkMatch(word: String, definition: String) {
        val currentLevel = _uiState.value.currentLevel ?: return
        val isMatch = currentLevel.pairs.any {
            it.word == word && it.definition == definition
        }

        _uiState.value = _uiState.value.copy(attempts = _uiState.value.attempts + 1)

        if (isMatch) {
            val newMatched = _uiState.value.matchedPairs + setOf(word, definition)
            val allMatched = currentLevel.pairs.all {
                newMatched.contains(it.word) && newMatched.contains(it.definition)
            }

            _uiState.value = _uiState.value.copy(
                matchedPairs = newMatched,
                selectedPairs = emptyList(),
                isGameComplete = allMatched
            )
        } else {
            viewModelScope.launch {
                delay(500)
                _uiState.value = _uiState.value.copy(selectedPairs = emptyList())
            }
        }
    }

    fun saveProgress(userName: String, levelId: String, onComplete: (Int, Int) -> Unit) {
        viewModelScope.launch {
            val timeSpent = calculateTimeSpent()
            val request = CompletionRequest(
                userName = userName,
                levelId = levelId,
                completed = true,
                timeSpent = timeSpent
            )

            repository.saveProgress(request).fold(
                onSuccess = {
                    loadUserCompletions(userName)
                    loadUserStatistics(userName)
                    // Navigate to result screen
                    val totalPairs = _uiState.value.currentLevel?.pairCount ?: 0
                    onComplete(totalPairs, totalPairs) // All pairs matched = all correct
                },
                onFailure = { }
            )
        }
    }

    private fun calculateTimeSpent(): String {
        val duration = System.currentTimeMillis() - _uiState.value.startTime
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun resetGame() {
        _uiState.value = _uiState.value.copy(
            selectedPairs = emptyList(),
            matchedPairs = emptySet(),
            attempts = 0,
            isGameComplete = false,
            startTime = System.currentTimeMillis()
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}