package com.example.eduquizz.features.bubbleshot.model

data class QuizQuestion(
    val question: String,
    val answers: List<String>,
    val correctAnswer: String
) 