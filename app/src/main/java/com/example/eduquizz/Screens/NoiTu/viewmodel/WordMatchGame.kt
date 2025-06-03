package com.example.quizapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.quizapp.model.WordPair
import com.example.quizapp.model.Connection

import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WordMatchGame: ViewModel() {
    private val _allWordPairs = listOf(
        WordPair("Apple", "A kind of fruit"),
        WordPair("Dog", "A domestic animal"),
        WordPair("Sun", "The star at the center of our solar system"),
        WordPair("Book", "A collection of pages"),
        WordPair("Computer", "An electronic device for processing data"),
        WordPair("Flower", "A plant's reproductive organ"),
        WordPair("Tiger", "A big cat"),
        WordPair("River", "A natural water flow"),
        WordPair("Mountain", "A high landform"),
        WordPair("Car", "A road vehicle"),
        WordPair("Banana", "A yellow fruit"),
        WordPair("Cat", "A domestic feline"),
        WordPair("Moon", "Earth's natural satellite"),
        WordPair("Notebook", "A collection of blank pages"),
        WordPair("Phone", "A device for calling"),
        WordPair("Rose", "A type of flower"),
        WordPair("Lion", "The king of the jungle"),
        WordPair("Lake", "A body of water surrounded by land"),
        WordPair("Hill", "A small elevation of land"),
        WordPair("Bus", "A large passenger vehicle"),
    ).shuffled()

    val gold = mutableStateOf(200)
    val currentLevel = mutableStateOf(0) // 0..3 (4 màn, mỗi màn 5 câu)
    val timerSeconds = mutableStateOf(40)
    val showResult = mutableStateOf(false)
    val showBuyGoldDialog = mutableStateOf(false)
    val showFinishDialog = mutableStateOf(false)
    val totalRight = mutableStateOf(0)
    val canPass = mutableStateOf(false)

    // Dữ liệu cho từng vòng
    var currentWordPairs = mutableStateListOf<WordPair>()
    var currentDefinitions = mutableStateListOf<String>()
    var connections = mutableStateListOf<Connection>()
    var selectedWordIndex by mutableStateOf<Int?>(null)

    // Timer
    private var timerJob: Job? = null

    init {
        startLevel(0)
    }

    fun startLevel(level: Int) {
        currentLevel.value = level
        val start = level * 5
        val end = (level + 1) * 5
        currentWordPairs.clear()
        currentWordPairs.addAll(_allWordPairs.subList(start, end))
        currentDefinitions.clear()
        currentDefinitions.addAll(currentWordPairs.map { it.definition }.shuffled())
        connections.clear()
        selectedWordIndex = null
        showResult.value = false
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerSeconds.value = 40
        timerJob = viewModelScope.launch {
            while (timerSeconds.value > 0 && !showResult.value) {
                delay(1000)
                timerSeconds.value--
            }
            if (timerSeconds.value == 0 && !showResult.value) {
                showResult.value = true
            }
        }
    }

    fun connectToDefinition(defIndex: Int) {
        selectedWordIndex?.let { wIdx ->
            if (connections.none { it.wordIndex == wIdx }) {
                connections.add(Connection(wIdx, defIndex))
                selectedWordIndex = null
            }
        }
    }

    fun selectWord(index: Int) {
        selectedWordIndex = index
    }

    fun checkResultAndReward() {
        showResult.value = true
        var roundRight = 0
        connections.forEach {
            val correct = currentWordPairs[it.wordIndex].definition == currentDefinitions[it.definitionIndex]
            if (correct) roundRight++
        }
        gold.value += roundRight * 5
        if (roundRight == 5) gold.value += 20 // Thưởng thêm nếu đúng cả 5
        totalRight.value += roundRight
    }

    fun nextLevel() {
        if (currentLevel.value < 3) {
            startLevel(currentLevel.value + 1)
        } else {
            finishGame()
        }
    }

    fun useHint() {
        if (gold.value >= 20) {
            gold.value -= 20
            // Tự động nối đúng 1 cặp còn lại chưa nối
            val available = (0..4).filter { wi ->
                connections.none { it.wordIndex == wi }
            }
            if (available.isNotEmpty()) {
                val idx = available.random()
                val defIdx = currentDefinitions.indexOf(currentWordPairs[idx].definition)
                connections.add(Connection(idx, defIdx))
            }
        } else showBuyGoldDialog.value = true
    }

    fun skipLevel() {
        if (gold.value >= 100) {
            gold.value -= 100
            // Nối toàn bộ còn lại đúng hết, showResult true
            (0..4).forEach { wi ->
                if (connections.none { it.wordIndex == wi }) {
                    val defIdx = currentDefinitions.indexOf(currentWordPairs[wi].definition)
                    connections.add(Connection(wi, defIdx))
                }
            }
            showResult.value = true
        } else showBuyGoldDialog.value = true
    }

    fun buyGold(amount: Int) {
        gold.value += amount
        showBuyGoldDialog.value = false
    }

    fun finishGame() {
        // Giả sử KPI qua màn là ≥ 16/20 đúng
        canPass.value = totalRight.value >= 16
        showFinishDialog.value = true
    }

    fun resetAll() {
        totalRight.value = 0
        gold.value = 200
        startLevel(0)
        showFinishDialog.value = false
    }
}

