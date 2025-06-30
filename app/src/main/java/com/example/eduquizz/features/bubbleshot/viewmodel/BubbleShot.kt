package com.example.eduquizz.features.bubbleshot.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.bubbleshot.model.QuizQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BubbleShot : ViewModel() {
    // Danh sách câu hỏi mẫu
    private val questions = listOf(
        QuizQuestion("What is the capital of France?", listOf("Paris", "London", "Berlin", "Rome"), "Paris"),
        QuizQuestion("2 + 2 = ?", listOf("3", "4", "5", "6"), "4"),
        QuizQuestion("Which is a fruit?", listOf("Carrot", "Potato", "Apple", "Onion"), "Apple"),
        QuizQuestion("Color of the sky?", listOf("Blue", "Green", "Red", "Yellow"), "Blue")
    )
    var currentIndex = mutableStateOf(0)
    var currentQuestion = mutableStateOf(questions[0])
    var answers = mutableStateOf(questions[0].answers.shuffled())
    var timer = mutableStateOf(5)
    var gold = mutableStateOf(100)
    var canUseHint = mutableStateOf(true)
    var job: Job? = null

    init {
        startTimer()
    }

    fun onAnswerSelected(answer: String) {
        if (answer == currentQuestion.value.correctAnswer) {
            // Đúng: loại đáp án khỏi lưới
            answers.value = answers.value.filter { it != answer }
        }
        nextQuestion()
    }

    fun nextQuestion() {
        job?.cancel()
        if (currentIndex.value < questions.size - 1) {
            currentIndex.value++
            currentQuestion.value = questions[currentIndex.value]
            answers.value = currentQuestion.value.answers.shuffled()
            timer.value = 5
            canUseHint.value = true
            startTimer()
        } else {
            // Kết thúc game
            timer.value = 0
        }
    }

    fun useHint() {
        if (gold.value >= 10 && canUseHint.value && answers.value.size > 2) {
            val wrongAnswers = answers.value.filter { it != currentQuestion.value.correctAnswer }
            if (wrongAnswers.isNotEmpty()) {
                val toRemove = wrongAnswers.random()
                answers.value = answers.value.filter { it != toRemove }
                gold.value -= 10
                if (answers.value.size <= 2) canUseHint.value = false
            }
        }
    }

    private fun startTimer() {
        job = viewModelScope.launch {
            while (timer.value > 0) {
                delay(1000)
                timer.value--
                if (timer.value == 0) {
                    nextQuestion()
                }
            }
        }
    }
} 