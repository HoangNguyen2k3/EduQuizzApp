package com.example.eduquizz.model

data class QuestionItem(
    val answer: String ="",
    val category:String ="",
    val choices:List<String> = listOf(),
    val question:String="",
    val image: String? = null
)
