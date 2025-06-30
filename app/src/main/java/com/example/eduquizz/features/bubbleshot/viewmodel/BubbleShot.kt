package com.example.eduquizz.features.bubbleshot.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.bubbleshot.model.MathQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class BubbleShot : ViewModel() {
    private val allQuestions = listOf(
        MathQuestion("5 + 7 = ?", "12"),
        MathQuestion("9 - 3 = ?", "6"),
        MathQuestion("6 × 7 = ?", "42"),
        MathQuestion("15 ÷ 3 = ?", "5"),
        MathQuestion("8 + 4 = ?", "12"),
        MathQuestion("10 - 2 = ?", "8"),
        MathQuestion("3 × 5 = ?", "15"),
        MathQuestion("18 ÷ 2 = ?", "9"),
        MathQuestion("7 + 6 = ?", "13"),
        MathQuestion("14 - 7 = ?", "7"),
        MathQuestion("4 × 4 = ?", "16"),
        MathQuestion("20 ÷ 4 = ?", "5"),
        MathQuestion("2 + 9 = ?", "11"),
        MathQuestion("11 - 1 = ?", "10"),
        MathQuestion("5 × 3 = ?", "15"),
        MathQuestion("21 ÷ 7 = ?", "3"),
        MathQuestion("6 + 8 = ?", "14"),
        MathQuestion("12 - 5 = ?", "7"),
        MathQuestion("9 × 2 = ?", "18"),
        MathQuestion("16 ÷ 4 = ?", "4")
    )
    var currentQuestion = mutableStateOf(allQuestions.random())
    var answers = mutableStateListOf<String>()
    var timer = mutableStateOf(5)
    var score = mutableStateOf(0)
    var job: Job? = null

    init {
        setupInitialAnswers()
        startTimer()
    }

    private fun setupInitialAnswers() {
        // Lấy 12 đáp án đúng khác nhau
        val answerPool = allQuestions.map { it.correctAnswer }.shuffled().take(12).toMutableList()
        // Đảm bảo đáp án đúng của câu hiện tại có trong lưới
        if (!answerPool.contains(currentQuestion.value.correctAnswer)) {
            answerPool[Random.nextInt(12)] = currentQuestion.value.correctAnswer
        }
        answers.clear()
        answers.addAll(answerPool)
    }

    fun onAnswerSelected(answer: String) {
        job?.cancel()
        val isCorrect = answer == currentQuestion.value.correctAnswer
        if (isCorrect) {
            score.value += 1
        }
        answers.remove(answer)
        // KHÔNG chuyển câu hỏi ở đây, chỉ chuyển khi không còn đáp án đúng trong lưới
        // Nếu sau khi xóa, không còn đáp án đúng của câu hỏi hiện tại trong lưới thì chuyển câu mới
        if (!answers.contains(currentQuestion.value.correctAnswer)) {
            nextQuestion()
        } else {
            // Nếu vẫn còn đáp án đúng, chỉ reset timer
            timer.value = 5
            startTimer()
        }
    }

    fun nextQuestion() {
        // Chọn câu hỏi mới
        currentQuestion.value = allQuestions.random()
        // Nếu đáp án đúng của câu hỏi mới không còn trong lưới, thêm vào
        if (!answers.contains(currentQuestion.value.correctAnswer)) {
            if (answers.size < 12) {
                answers.add(currentQuestion.value.correctAnswer)
            } else {
                // Nếu đã đủ 12 bóng, thay thế ngẫu nhiên 1 bóng sai bằng đáp án đúng
                val wrongs = answers.filter { it != currentQuestion.value.correctAnswer }
                if (wrongs.isNotEmpty()) {
                    val toReplace = wrongs.random()
                    val idx = answers.indexOf(toReplace)
                    answers[idx] = currentQuestion.value.correctAnswer
                }
            }
        }
        // Đảm bảo luôn có tối đa 12 bóng
        while (answers.size < 12) {
            val pool = allQuestions.map { it.correctAnswer }.filter { it !in answers }
            if (pool.isNotEmpty()) {
                answers.add(pool.random())
            } else break
        }
        timer.value = 5
        startTimer()
    }

    private fun startTimer() {
        job?.cancel()
        job = viewModelScope.launch {
            timer.value = 5
            while (timer.value > 0) {
                delay(1000)
                timer.value--
                if (timer.value == 0) {
                    // Khi hết giờ, chuyển câu mới và reset timer
                    nextQuestion()
                }
            }
        }
    }
} 