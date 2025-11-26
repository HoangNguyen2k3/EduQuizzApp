package com.example.eduquizz.features.bubbleshot.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.bubbleshot.model.MathQuestion
import com.example.eduquizz.features.bubbleshot.model.Bubble
import com.example.eduquizz.features.bubbleshot.model.BubblePool
import com.example.eduquizz.features.bubbleshot.repository.ShotQuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class BubbleShot : ViewModel() {
    private val repository = ShotQuestionRepository()
    private var allQuestions = listOf<MathQuestion>()

    // ===== THAY ĐỔI CHÍNH: Thêm BubblePool =====
    private val bubblePool = BubblePool(initialSize = 20)

    // Fallback questions
    private val fallbackQuestions = listOf(
        MathQuestion("5 + 7 = ?", "12"),
        MathQuestion("9 - 3 = ?", "6"),
        MathQuestion("6 × 7 = ?", "42")
    )

    var currentQuestion = mutableStateOf<MathQuestion?>(null)

    // ===== THAY ĐỔI: Từ List<String?> sang List<Bubble> =====
    var answers = mutableStateListOf<Bubble>()

    var timer = mutableStateOf(10)
    var score = mutableStateOf(0)
    var job: Job? = null

    var selectedAnswer = mutableStateOf<Bubble?>(null)

    var isCorrectAnswer = mutableStateOf<Boolean?>(null)
    var questionCount = mutableStateOf(0)
    var isGameOver = mutableStateOf(false)
    var totalQuestions = mutableStateOf(20)
    var isLoading = mutableStateOf(true)

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                val questions = repository.getQuestionsOnce()
                allQuestions = if (questions.isNotEmpty()) questions else fallbackQuestions

                currentQuestion.value = allQuestions.random()
                setupInitialAnswers()
                isLoading.value = false
                startTimer()
            } catch (e: Exception) {

                allQuestions = fallbackQuestions
                currentQuestion.value = allQuestions.random()
                setupInitialAnswers()
                isLoading.value = false
                startTimer()
            }
        }
    }

    private fun setupInitialAnswers() {
        bubblePool.releaseAll()
        answers.clear()
        val answerPool = allQuestions.map { it.correctAnswer }
            .shuffled()
            .take(20)
            .toMutableList()
        currentQuestion.value?.let { question ->
            if (!answerPool.contains(question.correctAnswer)) {
                answerPool[Random.nextInt(answerPool.size.coerceAtMost(12))] =
                    question.correctAnswer
            }
        }
        answerPool.forEachIndexed { index, answer ->
            val bubble = bubblePool.acquire(answer, index)
            answers.add(bubble)
        }
    }

    /**
     * Xử lý khi user chọn bubble
     */
    fun onAnswerSelected(index: Int) {
        if (isGameOver.value || currentQuestion.value == null) return
        if (index >= answers.size) return

        job?.cancel()

        val bubble = answers[index]
        val isCorrect = bubble.answer == currentQuestion.value?.correctAnswer
        selectedAnswer.value = bubble
        isCorrectAnswer.value = isCorrect

        if (isCorrect) {
            score.value += 1
        }

        questionCount.value += 1

        viewModelScope.launch {
            delay(500)

            // ===== THAY ĐỔI: Release bubble về pool thay vì set null =====
            bubblePool.release(bubble)
            answers.removeAt(index)

            delay(200)
            selectedAnswer.value = null
            isCorrectAnswer.value = null

            if (questionCount.value >= totalQuestions.value) {
                isGameOver.value = true
            } else {
                nextQuestion()
            }
        }
    }

    /**
     * Chuyển sang câu hỏi tiếp theo
     */
    fun nextQuestion() {
        if (isGameOver.value || allQuestions.isEmpty()) return

        currentQuestion.value = allQuestions.random()
        val correctAnswer = currentQuestion.value?.correctAnswer ?: return

        // Kiểm tra xem đáp án đúng có trong answers không
        val hasCorrectAnswer = answers.any { it.answer == correctAnswer }

        if (!hasCorrectAnswer) {
            // Thêm đáp án đúng vào vị trí random
            val position = (0..15).random()
            val bubble = bubblePool.acquire(correctAnswer, position)
            answers.add(bubble)


        } else {
            // Shuffle vị trí các bubble
            val shuffledPositions = answers.indices.shuffled()
            answers.forEachIndexed { index, bubble ->
                bubble.position = shuffledPositions[index]
                bubble.offsetY = Random.nextFloat() * 20f






            }
        }

        // Đảm bảo có ít nhất 8 bubbles
        val currentCount = answers.size
        if (currentCount < 8) {
            val pool = allQuestions.map { it.correctAnswer }
                .filter { answer -> answers.none { it.answer == answer } }
                .toMutableList()

            repeat((8 - currentCount).coerceAtMost(pool.size)) {
                val answer = pool.random()
                pool.remove(answer)
                val position = (0..15).random()
                val bubble = bubblePool.acquire(answer, position)
                answers.add(bubble)


            }
        }

        // Thêm random bubbles để đạt 12-15 bubbles
        val targetCount = (12..15).random()
        val allAnswers = answers.map { it.answer }.toSet()













        repeat((targetCount - answers.size).coerceAtLeast(0)) {
            var randomAnswer: String
            do {
                randomAnswer = (1..100).random().toString()
            } while (randomAnswer in allAnswers)

            val position = (0..15).random()
            val bubble = bubblePool.acquire(randomAnswer, position)
            answers.add(bubble)
        }

        timer.value = 10
        startTimer()
    }

    private fun startTimer() {
        job?.cancel()
        job = viewModelScope.launch {
            timer.value = 10
            while (timer.value > 0) {
                delay(1000)
                timer.value--
                if (timer.value == 0) {
                    if (questionCount.value >= totalQuestions.value) {
                        isGameOver.value = true
                    } else {
                        nextQuestion()
                    }
                }
            }
        }
    }

    fun resetGame() {
        job?.cancel()
        score.value = 0
        questionCount.value = 0
        isGameOver.value = false
        selectedAnswer.value = null
        isCorrectAnswer.value = null

        if (allQuestions.isNotEmpty()) {
            currentQuestion.value = allQuestions.random()
            setupInitialAnswers()
            startTimer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        // Cleanup pool
        bubblePool.releaseAll()
    }
}