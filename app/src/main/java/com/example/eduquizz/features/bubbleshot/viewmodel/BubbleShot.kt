package com.example.eduquizz.features.bubbleshot.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.bubbleshot.model.MathQuestion
import com.example.eduquizz.features.bubbleshot.repository.ShotQuestionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class BubbleShot : ViewModel() {
    private val repository = ShotQuestionRepository()
    private var allQuestions = listOf<MathQuestion>()

    // Fallback questions trong trường hợp không lấy được từ Firebase
    private val fallbackQuestions = listOf(
        MathQuestion("5 + 7 = ?", "12"),
        MathQuestion("9 - 3 = ?", "6"),
        MathQuestion("6 × 7 = ?", "42")
    )

    var currentQuestion = mutableStateOf<MathQuestion?>(null)
    var answers = mutableStateListOf<String?>()
    var timer = mutableStateOf(10)
    var score = mutableStateOf(0)
    var job: Job? = null
    var selectedAnswer = mutableStateOf<String?>(null)
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
        val answerPool = allQuestions.map { it.correctAnswer }.shuffled().take(20).toMutableList()
        currentQuestion.value?.let { question ->
            if (!answerPool.contains(question.correctAnswer)) {
                answerPool[Random.nextInt(answerPool.size.coerceAtMost(12))] = question.correctAnswer
            }
        }
        answers.clear()
        answers.addAll(answerPool.map { it as String? })
    }

    fun onAnswerSelected(index: Int) {
        if (isGameOver.value || currentQuestion.value == null) return
        job?.cancel()

        val answer = answers[index]
        val isCorrect = answer == currentQuestion.value?.correctAnswer
        selectedAnswer.value = answer
        isCorrectAnswer.value = isCorrect

        if (isCorrect) {
            score.value += 1
        }

        questionCount.value += 1

        viewModelScope.launch {
            delay(500)
            answers[index] = null
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

    fun nextQuestion() {
        if (isGameOver.value || allQuestions.isEmpty()) return

        currentQuestion.value = allQuestions.random()
        val correctAnswer = currentQuestion.value?.correctAnswer ?: return

        val nonNullIndices = answers.mapIndexedNotNull { idx, v -> if (v != null) idx else null }

        if (!answers.filterNotNull().contains(correctAnswer)) {
            if (nonNullIndices.isNotEmpty()) {
                val replaceIdx = nonNullIndices.random()
                answers[replaceIdx] = correctAnswer
            } else {
                answers.add(0, correctAnswer)
            }
        } else {
            val nonNulls = nonNullIndices.map { answers[it]!! }.toMutableList()
            val swapCount = Random.nextInt(2, 4)
            repeat(swapCount) {
                val i = Random.nextInt(nonNulls.size)
                val j = Random.nextInt(nonNulls.size)
                val tmp = nonNulls[i]
                nonNulls[i] = nonNulls[j]
                nonNulls[j] = tmp
            }
            nonNullIndices.forEachIndexed { order, idx ->
                answers[idx] = nonNulls[order]
            }
        }

        val existing = answers.filterNotNull().toMutableList()
        if (existing.size < 8) {
            val pool = allQuestions.map { it.correctAnswer }
                .filter { it !in existing }
                .toMutableList()

            for (i in answers.indices) {
                if (existing.size >= 8) break
                if (answers[i] == null && pool.isNotEmpty()) {
                    val pick = pool.random()
                    pool.remove(pick)
                    answers[i] = pick
                    existing.add(pick)
                }
            }
        }

        val targetCount = (12..15).random()
        val allExisting = answers.filterNotNull().toMutableSet()
        for (i in answers.indices) {
            if (answers.count { it != null } >= targetCount) break
            if (answers[i] == null) {
                var rand: String
                do {
                    rand = (1..100).random().toString()
                } while (rand in allExisting)

                answers[i] = rand
                allExisting.add(rand)
            }
        }

        while (answers.count { it != null } < targetCount) {
            var rand: String
            do {
                rand = (1..100).random().toString()
            } while (rand in allExisting)

            answers.add(rand)
            allExisting.add(rand)
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
    }
}