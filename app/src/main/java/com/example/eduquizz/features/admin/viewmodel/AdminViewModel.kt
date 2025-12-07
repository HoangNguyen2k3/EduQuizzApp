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

    // Question Management States
    private val _questions = MutableStateFlow<List<QuestionItem>>(emptyList())
    val questions: StateFlow<List<QuestionItem>> = _questions.asStateFlow()

    private val _currentQuestion = MutableStateFlow<QuestionItem?>(null)
    val currentQuestion: StateFlow<QuestionItem?> = _currentQuestion.asStateFlow()

    // Contest Management States
    private val _contests = MutableStateFlow<List<Contest>>(emptyList())
    val contests: StateFlow<List<Contest>> = _contests.asStateFlow()

    private val _currentContest = MutableStateFlow<Contest?>(null)
    val currentContest: StateFlow<Contest?> = _currentContest.asStateFlow()

    private val _contestQuestions = MutableStateFlow<List<ContestQuestion>>(emptyList())
    val contestQuestions: StateFlow<List<ContestQuestion>> = _contestQuestions.asStateFlow()

    private val _contestStats = MutableStateFlow<ContestStats?>(null)
    val contestStats: StateFlow<ContestStats?> = _contestStats.asStateFlow()

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

    // Question Management Functions
    fun loadQuestions(username: String, filter: QuestionFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getQuestions(username, filter)
                .onSuccess { questions ->
                    _questions.value = questions
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load questions"
                }

            _isLoading.value = false
        }
    }

    fun loadQuestionById(username: String, questionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getQuestionById(username, questionId)
                .onSuccess { question ->
                    _currentQuestion.value = question
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load question"
                }

            _isLoading.value = false
        }
    }

    fun createQuestion(username: String, request: QuestionCreateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.createQuestion(username, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to create question"
                }

            _isLoading.value = false
        }
    }

    fun updateQuestion(username: String, request: QuestionUpdateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.updateQuestion(username, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update question"
                }

            _isLoading.value = false
        }
    }

    fun deleteQuestion(username: String, questionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.deleteQuestion(username, questionId)
                .onSuccess {
                    // Reload questions after deletion
                    val currentFilter = QuestionFilter() // You might want to preserve the current filter
                    loadQuestions(username, currentFilter)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to delete question"
                }

            _isLoading.value = false
        }
    }

    fun bulkImportQuestions(
        username: String,
        questions: List<QuestionCreateRequest>,
        onSuccess: (BulkImportResult) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.bulkImportQuestions(username, questions)
                .onSuccess { result ->
                    onSuccess(result)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to import questions"
                }

            _isLoading.value = false
        }
    }

    // Contest Management Functions
    fun loadContests(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getContests(username)
                .onSuccess { contests ->
                    _contests.value = contests
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load contests"
                }

            _isLoading.value = false
        }
    }

    fun loadContestById(username: String, contestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getContestById(username, contestId)
                .onSuccess { contest ->
                    _currentContest.value = contest
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load contest"
                }

            _isLoading.value = false
        }
    }

    fun loadContestQuestions(username: String, filter: QuestionFilter) {
        viewModelScope.launch {
            adminRepository.getQuestions(username, filter)
                .onSuccess { questions ->
                    _contestQuestions.value = questions.map { question ->
                        ContestQuestion(
                            id = question.id,
                            questionText = question.questionText,
                            category = question.category,
                            difficulty = question.difficulty,
                            isSelected = false
                        )
                    }
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load questions"
                }
        }
    }

    fun createContest(username: String, request: ContestCreateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.createContest(username, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to create contest"
                }

            _isLoading.value = false
        }
    }

    fun updateContest(username: String, request: ContestUpdateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.updateContest(username, request)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to update contest"
                }

            _isLoading.value = false
        }
    }

    fun deleteContest(username: String, contestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.deleteContest(username, contestId)
                .onSuccess {
                    // Reload contests after deletion
                    loadContests(username)
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to delete contest"
                }

            _isLoading.value = false
        }
    }

    fun loadContestStats(username: String, contestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            adminRepository.getContestStats(username, contestId)
                .onSuccess { stats ->
                    _contestStats.value = stats
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load contest stats"
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