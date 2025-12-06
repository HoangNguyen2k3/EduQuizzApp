package com.example.eduquizz.features.admin.data

data class AdminDashboardStats(
    val wordSearchTopics: Int = 0,
    val batChuLevels: Int = 0,
    val matchLevels: Int = 0,
    val quizLevels: Int = 0,
    val sceneLevels: Int = 0,
    val soundLevels: Int = 0,
    val totalQuestions: Int = 0,
    val totalContests: Int = 0,
    val activeContests: Int = 0,
    val totalUsers: Int = 0
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

// Question Management Models
data class QuestionChoice(
    val choiceLabel: String,
    val choiceText: String,
    val isCorrect: Boolean
)

data class QuestionItem(
    val id: String = "",
    val questionText: String = "",
    val choices: List<QuestionChoice> = emptyList(),
    val difficulty: String = "Easy",
    val category: String = "",
    val points: Int = 10,
    val timeLimit: Int = 30,
    val gameType: String = "",
    val levelId: String = "",
    val createdAt: Long = 0L
)

data class QuestionCreateRequest(
    val questionText: String,
    val choices: List<QuestionChoice>,
    val difficulty: String,
    val category: String,
    val points: Int,
    val timeLimit: Int,
    val gameType: String = "",
    val levelId: String = ""
)

data class QuestionUpdateRequest(
    val id: String,
    val questionText: String,
    val choices: List<QuestionChoice>,
    val difficulty: String,
    val category: String,
    val points: Int,
    val timeLimit: Int
)

data class QuestionFilter(
    val gameType: String? = null,
    val levelId: String? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val searchQuery: String? = null
)

data class BulkImportResult(
    val totalImported: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val errors: List<String> = emptyList()
)

// Contest Management Models
data class Contest(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0, // Timestamp
    val endTime: Long = 0, // Timestamp
    val duration: Int = 0, // Minutes
    val totalQuestions: Int = 0,
    val questionIds: List<String> = emptyList(), // Question IDs
    var status: String = "scheduled", // scheduled, live, ended
    val participantCount: Int = 0,
    val maxParticipants: Int = 100,
    val createdBy: String = "",
    val createdAt: Long = 0L
)

data class ContestCreateRequest(
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int, // Minutes
    val questionIds: List<String>,
    val maxParticipants: Int = 100,
    val createdBy: String
)

data class ContestUpdateRequest(
    val id: String,
    val title: String,
    val description: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int,
    val questionIds: List<String>,
    val maxParticipants: Int = 100
)

data class ContestQuestion(
    val id: String,
    val questionText: String,
    val category: String,
    val difficulty: String,
    val isSelected: Boolean = false
)

data class ContestStats(
    val contestId: String = "",
    val totalParticipants: Int = 0,
    val averageScore: Double = 0.0,
    val highestScore: Int = 0,
    val lowestScore: Int = 0,
    val completionRate: Double = 0.0,
    val leaderboard: List<LeaderboardEntry> = emptyList()
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val score: Int,
    val completionTime: Int // seconds
)

sealed class AdminUiState {
    object Loading : AdminUiState()
    object AccessDenied : AdminUiState()
    data class Success(val stats: AdminDashboardStats) : AdminUiState()
    data class Error(val message: String) : AdminUiState()
}