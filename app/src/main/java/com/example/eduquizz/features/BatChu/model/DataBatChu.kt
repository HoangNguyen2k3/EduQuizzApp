package com.example.eduquizz.features.BatChu.model

data class DataBatChu(
    val question:String = "What is this?",
    val imageUrl: String = "",
    val answer: String = "",
    val suggestion: String = "", // Optional
    val shuffledLetters: List<Char> = listOf() // 14 chữ cái (gồm cả chữ đúng + nhiễu)
)
