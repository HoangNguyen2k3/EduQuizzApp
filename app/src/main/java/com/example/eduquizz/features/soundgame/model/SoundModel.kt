package com.example.eduquizz.features.soundgame.model

data class SoundClip(
    val id: Long = 0,
    val clipId: String,
    val name: String,
    val audioUrl: String,
    val answer: String,
    var userAnswer: String = "",
    var isCorrect: Boolean? = null
)

data class SoundLevel(
    val id: Long = 0,
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val clips: List<SoundClip> = emptyList()
)

data class SoundLevelData(
    val id: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val isCompleted: Boolean = false
)