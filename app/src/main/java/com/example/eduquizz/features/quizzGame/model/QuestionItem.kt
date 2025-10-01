package com.example.eduquizz.features.quizzGame.model

import com.google.gson.annotations.SerializedName

data class QuestionItem(
    @SerializedName("question")
    val questionText: String = "",
    val answer: String = "",
    val category: String = "",
    val choices: List<String> = listOf(),
    val image: String? = null
) {
    // Constructor để convert từ backend response
    constructor(backendQuestion: BackendQuizQuestion) : this(
        questionText = backendQuestion.question,
        answer = backendQuestion.answer,
        category = backendQuestion.category,
        choices = backendQuestion.choices
    )
}

// Tạo data class để map với backend response
data class BackendQuizQuestion(
    val id: Long? = null,
    val question: String = "",
    val answer: String = "",
    val category: String = "",
    val choices: List<String> = listOf()
)

data class QuizGameLevelResponse(
    val levelId: String = "",
    val title: String = "",
    val difficulty: String = "",
    val questionCount: Int = 0,
    val questions: List<BackendQuizQuestion> = listOf() // Sử dụng BackendQuizQuestion
)