package com.example.eduquizz.features.match.model

data class MatchLevel(
    val id: Long = 0,
    val levelId: String = "",
    val title: String = "",
    val difficulty: String = "",
    val pairCount: Int = 0,
    val createdAt: String = "",
    val pairs: List<MatchWordPair> = emptyList()
)

data class MatchWordPair(
    val word: String = "",
    val definition: String = ""
)

data class MatchUserProgress(
    val id: Long = 0,
    val username: String = "",
    val levelId: String = "",
    val isCompleted: Boolean = false,
    val timeSpent: String = "",
    val completionDate: String = ""
)

data class CompletionRequest(
    val userName: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String
)

data class UserStatistics(
    val completedLevels: Int = 0,
    val totalLevels: Int = 0,
    val completionRate: Double = 0.0,
    val recentCompletions: List<MatchUserProgress> = emptyList()
)