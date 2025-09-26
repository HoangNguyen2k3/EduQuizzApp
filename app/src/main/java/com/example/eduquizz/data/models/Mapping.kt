package com.example.eduquizz.data.models

import androidx.compose.ui.graphics.Color

data class Mapping(
    val id: String,
    val name: String,
    val description: String = "",
    val iconRes: Int = 0,
    val gradientColors: List<Color> = emptyList(),
    val difficulty: String = "Easy",
    val isLocked: Boolean = false,
    val completedLevels: Int = 0,
    val totalLevels: Int = 1
)