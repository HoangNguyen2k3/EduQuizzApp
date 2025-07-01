package com.example.eduquizz.data.models

import androidx.compose.ui.graphics.Color

data class Game(
    val id: String,
    val name: String,
    val iconRes: Int,
    val progress: Int,
    val totalQuestions: Int,
    val completedQuestions: Int,
    val totalLessons: Int,
    val gradientColors: List<Color>
)