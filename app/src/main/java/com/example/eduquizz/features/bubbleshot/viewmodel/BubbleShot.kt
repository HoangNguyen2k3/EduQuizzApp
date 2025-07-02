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
        MathQuestion("25 - 7 = ?", "18"),
        MathQuestion("12 + 9 = ?", "21"),
        MathQuestion("8 × 4 = ?", "32"),
        MathQuestion("36 ÷ 4 = ?", "9"),
        MathQuestion("17 + 5 = ?", "22"),
        MathQuestion("24 - 11 = ?", "13"),
        MathQuestion("7 × 9 = ?", "63"),
        MathQuestion("81 ÷ 9 = ?", "9"),
        MathQuestion("13 + 16 = ?", "29"),
        MathQuestion("31 - 8 = ?", "23"),
        MathQuestion("6 × 8 = ?", "48"),
        MathQuestion("72 ÷ 8 = ?", "9"),
        MathQuestion("19 + 19 = ?", "38"),
        MathQuestion("45 - 25 = ?", "20"),
        MathQuestion("11 × 3 = ?", "33"),
        MathQuestion("64 ÷ 16 = ?", "4"),
        MathQuestion("27 + 14 = ?", "41"),
        MathQuestion("50 - 17 = ?", "33"),
        MathQuestion("9 × 7 = ?", "63"),
        MathQuestion("56 ÷ 7 = ?", "8"),
        MathQuestion("22 + 11 = ?", "33"),
        MathQuestion("35 - 9 = ?", "26"),
        MathQuestion("5 × 8 = ?", "40"),
        MathQuestion("49 ÷ 7 = ?", "7"),
        MathQuestion("16 + 23 = ?", "39"),
        MathQuestion("42 - 15 = ?", "27"),
        MathQuestion("12 × 4 = ?", "48"),
        MathQuestion("80 ÷ 10 = ?", "8"),
        MathQuestion("31 + 17 = ?", "48"),
        MathQuestion("60 - 23 = ?", "37"),
        MathQuestion("9 × 3 = ?", "27"),
        MathQuestion("45 ÷ 9 = ?", "5"),
        MathQuestion("24 + 18 = ?", "42"),
        MathQuestion("55 - 33 = ?", "22"),
        MathQuestion("7 × 7 = ?", "49"),
        MathQuestion("100 ÷ 20 = ?", "5"),
        MathQuestion("37 + 26 = ?", "63"),
        MathQuestion("72 - 36 = ?", "36"),
        MathQuestion("8 × 9 = ?", "72"),
        MathQuestion("88 ÷ 11 = ?", "8"),
        MathQuestion("43 + 28 = ?", "71"),
        MathQuestion("67 - 19 = ?", "48"),
        MathQuestion("6 × 9 = ?", "54"),
        MathQuestion("96 ÷ 12 = ?", "8"),
        MathQuestion("52 + 29 = ?", "81"),
        MathQuestion("75 - 28 = ?", "47")
    )
    var currentQuestion = mutableStateOf(allQuestions.random())
    var answers = mutableStateListOf<String?>()
    var timer = mutableStateOf(5)
    var score = mutableStateOf(0)
    var job: Job? = null
    var selectedAnswer = mutableStateOf<String?>(null)
    var isCorrectAnswer = mutableStateOf<Boolean?>(null)

    init {
        setupInitialAnswers()
        startTimer()
    }

    private fun setupInitialAnswers() {
        val answerPool = allQuestions.map { it.correctAnswer }.shuffled().take(20).toMutableList()
        // Đảm bảo đáp án đúng của câu hiện tại có trong lưới
        if (!answerPool.contains(currentQuestion.value.correctAnswer)) {
            answerPool[Random.nextInt(12)] = currentQuestion.value.correctAnswer
        }
        answers.clear()
        answers.addAll(answerPool.map { it as String? }) // cast để nullable
    }

    fun onAnswerSelected(index: Int) {
        job?.cancel()
        val answer = answers[index]
        val isCorrect = answer == currentQuestion.value.correctAnswer
        selectedAnswer.value = answer
        isCorrectAnswer.value = isCorrect
        if (isCorrect) {
            score.value += 1
        }
        viewModelScope.launch {
            delay(500)
            answers[index] = null
            delay(200)
            selectedAnswer.value = null
            isCorrectAnswer.value = null
            nextQuestion()
        }
    }
fun nextQuestion() {
    currentQuestion.value = allQuestions.random()
    val correctAnswer = currentQuestion.value.correctAnswer
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
    timer.value = 5
    startTimer()
}

    private fun startTimer(continueTimer: Boolean = false) {
        job?.cancel()
        job = viewModelScope.launch {
            if (continueTimer) {
                timer.value = 5
            }
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
