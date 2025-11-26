package com.example.eduquizz.features.bubbleshot.model

import kotlin.random.Random


data class Bubble(
    val id: Int,
    var answer: String = "",
    var isActive: Boolean = false,
    var position: Int = 0,
    var offsetY: Float = Random.nextFloat() * 20f
) {
    fun reset() {
        answer = ""
        isActive = false
        position = 0
        offsetY = Random.nextFloat() * 20f
    }
}