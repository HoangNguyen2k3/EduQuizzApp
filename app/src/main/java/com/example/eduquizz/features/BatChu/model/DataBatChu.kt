package com.example.eduquizz.features.BatChu.model

data class DataBatChu(
    val question:String = "What is this?",
    val imageUrl: String = "",
    val answer: String = "",
    val suggestion: String = "",
    val shuffledLetters: List<Char> = listOf()
)
