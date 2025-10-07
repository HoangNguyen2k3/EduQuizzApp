package com.example.eduquizz.features.ContestOnline.Model

data class QuestionItemContest(
    val question: String = "",
    val answer: String = "",
    val category: String = "",
    val image: String? = null,
    val choices: List<String> = emptyList()
)