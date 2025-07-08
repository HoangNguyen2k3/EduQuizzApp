package com.example.eduquizz.features.match.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.eduquizz.features.match.model.WordPair

import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data_save.DataViewModel
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

    var gold = mutableStateOf(-1)
        private set
    val currentLevel = mutableStateOf(0) // 0..3 (4 màn, mỗi màn 5 câu)
    val timerSeconds = mutableStateOf(40)
    val showResult = mutableStateOf(false)
    val showBuyGoldDialog = mutableStateOf(false)
    val showFinishDialog = mutableStateOf(false)
    val showTimeOutDialog = mutableStateOf(false)
    val totalRight = mutableStateOf(0)
    val totalQuestion = mutableStateOf(0)
    val canPass = mutableStateOf(false)

    // Dữ liệu cho từng vòng
    var cards = mutableStateListOf<MatchCard>()
    var selectedIndices = mutableStateListOf<Int>() // chỉ số trong cards
    var shakingIndices = mutableStateListOf<Int>() // chỉ số đang rung
    var correctIndices = mutableStateListOf<Int>() // chỉ số đúng (màu xanh)
    var wrongIndices = mutableStateListOf<Int>() // chỉ số sai (màu đỏ)

        // Timer
    private var timerJob: Job? = null

    init {
        startLevel(0)
    }
    private lateinit var dataViewModel: DataViewModel
    fun Init(data: DataViewModel) {
        this.dataViewModel = data
        viewModelScope.launch {
            delay(10) // Đợi LiveData emit ra giá trị
            gold.value = data.gold.value ?: 0
        }
        //gold = mutableStateOf(data.gold.value ?: 0)
        totalQuestion.value = _allWordPairs.size
    }
    fun spendCoins(amount: Int) {
        gold.value = (gold.value ?: 0) - amount
        dataViewModel.updateGold(gold.value ?: 0) // <-- chỉ update khi cần
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
        correctIndices.clear()
        wrongIndices.clear()
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
                onTimeOut()
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
                // Đúng, thêm vào danh sách đúng và đánh dấu đã match
                cards[selectedIndices[0]] = cards[selectedIndices[0]].copy(isMatched = true)
                cards[selectedIndices[1]] = cards[selectedIndices[1]].copy(isMatched = true)
                correctIndices.addAll(selectedIndices)
                gold.value += 5
                totalRight.value += 1
                selectedIndices.clear()
                
                // Kiểm tra nếu đã hoàn thành tất cả cặp (5 cặp = 10 card)
                if (cards.count { it.isMatched } == 10) {
                    // Tự động chuyển level sau 1 giây
                    viewModelScope.launch {
                        delay(1000)
                        if (currentLevel.value < 3) {
                            nextLevel()
                        } else {
                            finishGame()
                        }
                    }
                }
            } else {
                // Sai, rung 2 thẻ và thêm vào danh sách sai
                shakingIndices.addAll(selectedIndices)
                wrongIndices.addAll(selectedIndices)
                viewModelScope.launch {
                    delay(300)
                    shakingIndices.clear()
                    selectedIndices.clear()
                    // Xóa màu đỏ sau 1 giây
                    delay(1000)
                    wrongIndices.clear()
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
            spendCoins(20)
            // Tự động mở đúng 1 cặp chưa matched
            val unmatched = cards.withIndex().filter { !it.value.isMatched }
            val pairs = unmatched.groupBy { it.value.pairId }.values.filter { it.size == 2 }
            if (pairs.isNotEmpty()) {
                val pair = pairs.random()
                cards[pair[0].index] = cards[pair[0].index].copy(isMatched = true)
                cards[pair[1].index] = cards[pair[1].index].copy(isMatched = true)
                correctIndices.add(pair[0].index)
                correctIndices.add(pair[1].index)
                totalRight.value += 1
                
                // Kiểm tra nếu đã hoàn thành tất cả cặp
                if (cards.count { it.isMatched } == 10) {
                    viewModelScope.launch {
                        delay(1000)
                        if (currentLevel.value < 3) {
                            nextLevel()
                        } else {
                            finishGame()
                        }
                    }
                }
            }
        } else showBuyGoldDialog.value = true
    }
    fun skipLevel() {
        if (gold.value >= 100) {
            spendCoins(100)
            // Đếm số cặp chưa match để cộng vào totalRight
            val unmatchedCount = cards.count { !it.isMatched }
            val pairsToAdd = unmatchedCount / 2 // Mỗi cặp = 2 card
            
            // Mở toàn bộ cặp còn lại
            cards.forEachIndexed { i, c ->
                if (!c.isMatched) {
                    cards[i] = c.copy(isMatched = true)
                    correctIndices.add(i)
                }
            }
            
            // Cộng số cặp đúng vào totalRight (không cộng vàng)
            totalRight.value += pairsToAdd
            
            showResult.value = true
            
            // Tự động chuyển level sau 1 giây
            viewModelScope.launch {
                delay(1000)
                if (currentLevel.value < 3) {
                    nextLevel()
                } else {
                    finishGame()
                }
            }
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
        correctIndices.clear()
        wrongIndices.clear()
        startLevel(0)
        showFinishDialog.value = false
    }

    fun onTimeOut() {
        showResult.value = true
        timerJob?.cancel()
        showTimeOutDialog.value = true
        
        // Khi hết thời gian, hiển thị kết quả và reset về level đầu
        viewModelScope.launch {
            delay(5000) // Hiển thị dialog 3 giây
            showTimeOutDialog.value = false
            // Reset về level đầu thay vì kết thúc game
            totalRight.value = 0
            gold.value = 200
            correctIndices.clear()
            wrongIndices.clear()
            startLevel(0)
            showFinishDialog.value = false
        }
    }
}

