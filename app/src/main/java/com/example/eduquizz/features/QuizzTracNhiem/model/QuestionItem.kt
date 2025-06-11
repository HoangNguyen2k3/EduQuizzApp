package com.example.eduquizz.features.QuizzTracNhiem.model

data class QuestionItem(
    val answer: String ="",
    val category:String ="",
    val choices:List<String> = listOf(),
    val question:String=""
)
