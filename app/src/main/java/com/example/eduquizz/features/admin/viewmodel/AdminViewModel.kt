package com.example.eduquizz.features.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.admin.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminUiState>(AdminUiState.Loading)
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val _dashboardStats = MutableStateFlow(AdminDashboardStats())
    val dashboardStats: StateFlow<AdminDashboardStats> = _dashboardStats.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _selectedGameLevels = MutableStateFlow<List<GameLevel>>(emptyList())
    val selectedGameLevels: StateFlow<List<GameLevel>> = _selectedGameLevels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun checkAdminStatus(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            adminRepository.checkAdminStatus(username)
                .onSuccess { isAdmin ->
                    _isAdmin.value = isAdmin
                    if (isAdmin) {
                        loadDashboardStats(username)
                    } else {
                        _uiState.value = AdminUiState.AccessDenied
                    }
                }
                .onFailure { error ->
                    _uiState.value = AdminUiState.Error(
                        error.message ?: "Failed to check admin status"
                    )
                }
            _isLoading.value = false
        }
    }

    fun loadDashboardStats(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _uiState.value = AdminUiState.Loading

            adminRepository.getDashboardStats(username)
                .onSuccess { stats ->
                    _dashboardStats.value = stats
                    _uiState.value = AdminUiState.Success(stats)
                }
                .onFailure { error ->
                    _uiState.value = AdminUiState.Error(
                        error.message ?: "Failed to load dashboard"
                    )
                }
            _isLoading.value = false
        }
    }

    fun loadGameLevels(username: String, gameType: GameType) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = when (gameType) {
                GameType.WORD_SEARCH -> adminRepository.getWordSearchLevels(username)
                GameType.BAT_CHU -> adminRepository.getBatChuLevels(username)
                GameType.MATCH_GAME -> adminRepository.getMatchGameLevels(username)
                GameType.QUIZ -> adminRepository.getQuizLevels(username)
                GameType.SCENE -> adminRepository.getSceneLevels(username)
                GameType.SOUND -> adminRepository.getSoundLevels(username)
            }

            result
                .onSuccess { levels ->
                    _selectedGameLevels.value = levels
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load levels"
                }

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

enum class GameType {
    WORD_SEARCH,
    BAT_CHU,
    MATCH_GAME,
    QUIZ,
    SCENE,
    SOUND
}