package com.example.eduquizz.features.match.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.eduquizz.features.match.model.WordPair

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.eduquizz.features.match.model.MatchCard
// Thẻ cho game matching pairs

class WordMatchGame : ViewModel() {
    private val _allWordPairs = listOf(
        WordPair("Apple", "a round fruit with firm, white flesh and a green, red, or yellow skin"),
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
    var cards = mutableStateListOf<MatchCard>()
    var selectedIndices = mutableStateListOf<Int>() // chỉ số trong cards
    var shakingIndices = mutableStateListOf<Int>() // chỉ số đang rung

    // Timer
    private var timerJob: Job? = null

    init {
        startLevel(0)
    }

    fun startLevel(level: Int) {
        currentLevel.value = level
        val start = level * 5
        val end = (level + 1) * 5
        val wordPairs = _allWordPairs.subList(start, end)
        // Tạo 10 thẻ (5 từ, 5 nghĩa), mỗi cặp có cùng pairId
        val newCards = mutableListOf<MatchCard>()
        wordPairs.forEachIndexed { idx, pair ->
            newCards.add(MatchCard(id = idx * 2, text = pair.word, pairId = idx))
            newCards.add(MatchCard(id = idx * 2 + 1, text = pair.definition, pairId = idx))
        }
        cards.clear()
        cards.addAll(newCards.shuffled())
        selectedIndices.clear()
        shakingIndices.clear()
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

    fun onCardClick(index: Int) {
        if (cards[index].isMatched || selectedIndices.contains(index) || selectedIndices.size == 2) return
        selectedIndices.add(index)
        if (selectedIndices.size == 2) {
            val first = cards[selectedIndices[0]]
            val second = cards[selectedIndices[1]]
            if (first.pairId == second.pairId && first.id != second.id) {
                // Đúng, ẩn 2 thẻ
                cards[selectedIndices[0]] = cards[selectedIndices[0]].copy(isMatched = true)
                cards[selectedIndices[1]] = cards[selectedIndices[1]].copy(isMatched = true)
                gold.value += 5
                totalRight.value += 1
                selectedIndices.clear()
            } else {
                // Sai, rung 2 thẻ
                shakingIndices.addAll(selectedIndices)
                viewModelScope.launch {
                    delay(300)
                    shakingIndices.clear()
                    selectedIndices.clear()
                }
            }
        }
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
            // Tự động mở đúng 1 cặp chưa matched
            val unmatched = cards.withIndex().filter { !it.value.isMatched }
            val pairs = unmatched.groupBy { it.value.pairId }.values.filter { it.size == 2 }
            if (pairs.isNotEmpty()) {
                val pair = pairs.random()
                cards[pair[0].index] = cards[pair[0].index].copy(isMatched = true)
                cards[pair[1].index] = cards[pair[1].index].copy(isMatched = true)
                totalRight.value += 1
            }
        } else showBuyGoldDialog.value = true
    }

    fun skipLevel() {
        if (gold.value >= 100) {
            gold.value -= 100
            // Mở toàn bộ cặp còn lại
            cards.forEachIndexed { i, c ->
                if (!c.isMatched) cards[i] = c.copy(isMatched = true)
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

