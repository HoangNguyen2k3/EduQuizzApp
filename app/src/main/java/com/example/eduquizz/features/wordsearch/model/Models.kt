package com.example.eduquizz.features.wordsearch.model

data class Cell(
    val row: Int,
    val col: Int,
    val char: Char,
    var isSelected: Boolean = false,
    var belongsToFoundWord: Boolean = false
)

data class Word(
    val word: String,
    var isFound: Boolean = false
)

enum class Direction {
    HORIZONTAL,
    VERTICAL,
    DIAGONAL_DOWN,
    DIAGONAL_UP
}