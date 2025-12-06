package com.example.eduquizz.features.admin.data

data class AdminDashboardStats(
    val wordSearchTopics: Int = 0,
    val batChuLevels: Int = 0,
    val matchLevels: Int = 0,
    val quizLevels: Int = 0,
    val sceneLevels: Int = 0,
    val soundLevels: Int = 0
)

data class AdminCheckResponse(
    val isAdmin: Boolean = false
)

data class AdminResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T? = null
)

// Game management data classes
data class GameLevel(
    val id: String,
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val createdAt: String? = null
)

sealed class AdminUiState {
    object Loading : AdminUiState()
    object AccessDenied : AdminUiState()
    data class Success(val stats: AdminDashboardStats) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}