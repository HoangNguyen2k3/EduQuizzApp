package com.example.quizapp.data

import androidx.compose.ui.graphics.Color

data class Subject(
    val id: String,
    val name: String,
    val iconRes: Int,
    val progress: Int,
    val totalQuestions: Int,
    val completedQuestions: Int,
    val totalLessons: Int,
    val gradientColors: List<Color>,
    val isRecommended: Boolean = false
)