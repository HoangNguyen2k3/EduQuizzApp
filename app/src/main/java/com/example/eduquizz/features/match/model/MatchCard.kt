package com.example.eduquizz.Screens.NoiTu.model

data class MatchCard(
    val id: Int,
    val text: String,
    val pairId: Int, // id dùng để xác định cặp đúng
    val isMatched: Boolean = false
)