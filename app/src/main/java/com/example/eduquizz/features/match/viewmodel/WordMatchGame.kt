package com.example.eduquizz.features.match.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.eduquizz.features.match.model.WordPair
import com.example.eduquizz.features.match.repository.WordPairRepository
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data_save.DataViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.eduquizz.features.match.model.MatchCard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WordMatchGame @Inject constructor(
    val wordPairRepository: WordPairRepository
) : ViewModel() {

    private var _allWordPairs = mutableStateListOf<WordPair>()
    private val _isDataLoaded = mutableStateOf(false)
    private var _currentUserName = ""

    var gold = mutableStateOf(-1)
        private set
    val currentLevel = mutableStateOf(0)
    val timerSeconds = mutableStateOf(40)
    val showResult = mutableStateOf(false)
    val showBuyGoldDialog = mutableStateOf(false)
    val showFinishDialog = mutableStateOf(false)
    val showTimeOutDialog = mutableStateOf(false)
    val showLoadingDialog = mutableStateOf(false)
    val showNetworkErrorDialog = mutableStateOf(false)
    val totalRight = mutableStateOf(0)
    val totalQuestion = mutableStateOf(0)
    val canPass = mutableStateOf(false)

    // Dữ liệu cho từng vòng
    var cards = mutableStateListOf<MatchCard>()
    var selectedIndices = mutableStateListOf<Int>()
    var shakingIndices = mutableStateListOf<Int>()
    var correctIndices = mutableStateListOf<Int>()
    var wrongIndices = mutableStateListOf<Int>()

    // Timer
    private var timerJob: Job? = null
    private lateinit var dataViewModel: DataViewModel

    init {
        loadWordPairsFromServer()
    }

    private fun loadWordPairsFromServer() {
        showLoadingDialog.value = true
        showNetworkErrorDialog.value = false

        viewModelScope.launch {
            try {
                val wordPairs = wordPairRepository.getAllWordPairs()

                if (wordPairs.isNotEmpty()) {
                    _allWordPairs.clear()
                    _allWordPairs.addAll(wordPairs.shuffled())
                    totalQuestion.value = _allWordPairs.size
                    _isDataLoaded.value = true
                    showLoadingDialog.value = false

                    // Bắt đầu level đầu tiên sau khi load xong data
                    startLevel(0)
                } else {
                    // Không có dữ liệu từ server, dùng dữ liệu mặc định
                    showNetworkErrorDialog.value = true
                    showLoadingDialog.value = false
                }
            } catch (e: Exception) {
                showLoadingDialog.value = false
                showNetworkErrorDialog.value = true
                println("Error loading word pairs from server: ${e.message}")
            }
        }
    }

    fun Init(data: DataViewModel, userName: String = "defaultUser") {
        this.dataViewModel = data
        this._currentUserName = userName

        viewModelScope.launch {
            delay(10)
            gold.value = data.gold.value ?: 0
            // Load user progress
            loadUserProgress()
        }
    }

    private fun loadUserProgress() {
        viewModelScope.launch {
            wordPairRepository.getUserProgress(_currentUserName)
                .onSuccess { (level, totalRight, totalQuestions) ->
                    // Có thể sử dụng dữ liệu này để khôi phục trạng thái game
                    // this@WordMatchGame.currentLevel.value = level
                    // this@WordMatchGame.totalRight.value = totalRight
                    // this@WordMatchGame.totalQuestion.value = totalQuestions
                }
                .onFailure {
                    // Không có progress hoặc lỗi, bắt đầu từ level 0
                    println("No previous progress found or error loading: ${it.message}")
                }
        }
    }

    fun spendCoins(amount: Int) {
        gold.value = (gold.value ?: 0) - amount
        dataViewModel.updateGold(gold.value ?: 0)
    }

    fun startLevel(level: Int) {
        if (!_isDataLoaded.value) {
            // Nếu data chưa load xong, đợi hoặc dùng dữ liệu mặc định
            showLoadingDialog.value = true
            return
        }

        if (_allWordPairs.isEmpty()) {
            showNetworkErrorDialog.value = true
            return
        }

        startLevelInternal(level)
    }

    private fun startLevelInternal(level: Int) {
        currentLevel.value = level

        viewModelScope.launch {
            try {
                // Lấy word pairs cho level cụ thể từ server
                val wordPairs = wordPairRepository.getWordPairsByLevel(level)

                if (wordPairs.isNotEmpty()) {
                    setupLevelWithWordPairs(wordPairs)
                } else {
                    // Fallback: sử dụng dữ liệu đã load trước đó
                    val start = level * 5
                    val end = minOf((level + 1) * 5, _allWordPairs.size)

                    if (start >= _allWordPairs.size) {
                        finishGame()
                        return@launch
                    }

                    val fallbackPairs = _allWordPairs.subList(start, end)
                    setupLevelWithWordPairs(fallbackPairs)
                }
            } catch (e: Exception) {
                println("Error loading level $level: ${e.message}")
                // Fallback như trước
                val start = level * 5
                val end = minOf((level + 1) * 5, _allWordPairs.size)

                if (start >= _allWordPairs.size) {
                    finishGame()
                    return@launch
                }

                val fallbackPairs = _allWordPairs.subList(start, end)
                setupLevelWithWordPairs(fallbackPairs)
            }
        }
    }

    private fun setupLevelWithWordPairs(wordPairs: List<WordPair>) {
        // Tạo cards từ word pairs
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
                // Đúng
                cards[selectedIndices[0]] = cards[selectedIndices[0]].copy(isMatched = true)
                cards[selectedIndices[1]] = cards[selectedIndices[1]].copy(isMatched = true)
                correctIndices.addAll(selectedIndices)
                gold.value += 5
                totalRight.value += 1
                selectedIndices.clear()

                // Lưu progress
                saveUserProgress()

                // Kiểm tra hoàn thành level
                if (cards.count { it.isMatched } == cards.size) {
                    viewModelScope.launch {
                        delay(1000)

                        // Lưu level completion lên server
                        saveLevelCompletion(currentLevel.value)

                        if (currentLevel.value < 3) {
                            nextLevel()
                        } else {
                            finishGame()
                        }
                    }
                }
            } else {
                // Sai
                shakingIndices.addAll(selectedIndices)
                wrongIndices.addAll(selectedIndices)
                viewModelScope.launch {
                    delay(300)
                    shakingIndices.clear()
                    selectedIndices.clear()
                    delay(1000)
                    wrongIndices.clear()
                }
            }
        }
    }

    private fun saveUserProgress() {
        viewModelScope.launch {
            try {
                wordPairRepository.saveUserProgress(
                    _currentUserName,
                    currentLevel.value,
                    totalRight.value,
                    totalQuestion.value
                )
            } catch (e: Exception) {
                println("Error saving user progress: ${e.message}")
            }
        }
    }

    private fun saveLevelCompletion(level: Int) {
        viewModelScope.launch {
            try {
                val levelId = "Level${level + 1}"
                wordPairRepository.saveLevelCompletion(
                    _currentUserName,
                    levelId,
                    true,
                    "${40 - timerSeconds.value}s"
                ).onSuccess {
                    println("Level $levelId completed and saved to server")
                }.onFailure { e ->
                    println("Error saving level completion: ${e.message}")
                }
            } catch (e: Exception) {
                println("Error in saveLevelCompletion: ${e.message}")
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
            val unmatched = cards.withIndex().filter { !it.value.isMatched }
            val pairs = unmatched.groupBy { it.value.pairId }.values.filter { it.size == 2 }

            if (pairs.isNotEmpty()) {
                val pair = pairs.random()
                cards[pair[0].index] = cards[pair[0].index].copy(isMatched = true)
                cards[pair[1].index] = cards[pair[1].index].copy(isMatched = true)
                correctIndices.add(pair[0].index)
                correctIndices.add(pair[1].index)
                totalRight.value += 1

                // Lưu progress
                saveUserProgress()

                if (cards.count { it.isMatched } == cards.size) {
                    viewModelScope.launch {
                        delay(1000)

                        // Lưu level completion
                        saveLevelCompletion(currentLevel.value)

                        if (currentLevel.value < 3) {
                            nextLevel()
                        } else {
                            finishGame()
                        }
                    }
                }
            }
        } else {
            showBuyGoldDialog.value = true
        }
    }

    fun skipLevel() {
        if (gold.value >= 100) {
            spendCoins(100)
            val unmatchedCount = cards.count { !it.isMatched }
            val pairsToAdd = unmatchedCount / 2

            cards.forEachIndexed { i, c ->
                if (!c.isMatched) {
                    cards[i] = c.copy(isMatched = true)
                    correctIndices.add(i)
                }
            }

            totalRight.value += pairsToAdd
            showResult.value = true

            // Lưu progress
            saveUserProgress()

            viewModelScope.launch {
                delay(1000)

                // Lưu level completion
                saveLevelCompletion(currentLevel.value)

                if (currentLevel.value < 3) {
                    nextLevel()
                } else {
                    finishGame()
                }
            }
        } else {
            showBuyGoldDialog.value = true
        }
    }

    fun buyGold(amount: Int) {
        gold.value += amount
        showBuyGoldDialog.value = false
        dataViewModel.updateGold(gold.value ?: 0)
    }

    fun finishGame() {
        canPass.value = totalRight.value >= 16
        showFinishDialog.value = true

        // Lưu progress cuối cùng
        saveUserProgress()
    }

    fun resetAll() {
        totalRight.value = 0
        gold.value = 200
        correctIndices.clear()
        wrongIndices.clear()
        startLevel(0)
        showFinishDialog.value = false
        dataViewModel.updateGold(200)
    }

    fun onTimeOut() {
        showResult.value = true
        timerJob?.cancel()
        showTimeOutDialog.value = true

        viewModelScope.launch {
            delay(5000)
            showTimeOutDialog.value = false
            totalRight.value = 0
            gold.value = 200
            correctIndices.clear()
            wrongIndices.clear()
            startLevel(0)
            showFinishDialog.value = false
            dataViewModel.updateGold(200)
        }
    }

    fun retryLoadData() {
        loadWordPairsFromServer()
    }

    fun getLevelCompletionStatus(): Map<String, Boolean> {
        var completions = emptyMap<String, Boolean>()
        viewModelScope.launch {
            wordPairRepository.getAllLevelCompletions(_currentUserName)
                .onSuccess { completions = it }
                .onFailure { println("Error loading level completions: ${it.message}") }
        }
        return completions
    }

    fun loadWordPairsByTopic(topicId: String) {
        showLoadingDialog.value = true
        viewModelScope.launch {
            wordPairRepository.getWordPairsByTopic(topicId)
                .onSuccess { wordMatchData ->
                    _allWordPairs.clear()
                    _allWordPairs.addAll(wordMatchData.wordPairs.shuffled())
                    totalQuestion.value = _allWordPairs.size
                    _isDataLoaded.value = true
                    showLoadingDialog.value = false
                    startLevel(0)
                }
                .onFailure { e ->
                    showLoadingDialog.value = false
                    showNetworkErrorDialog.value = true
                    println("Error loading topic $topicId: ${e.message}")
                    // Fallback to loading all word pairs
                    loadWordPairsFromServer()
                }
        }
    }

    fun dismissNetworkErrorDialog() {
        showNetworkErrorDialog.value = false
        // Có thể thử load lại data hoặc sử dụng dữ liệu mặc định
        retryLoadData()
    }

    fun getUserStatistics(): Map<String, Any> {
        var stats = emptyMap<String, Any>()
        viewModelScope.launch {
            try {
                // Gọi API để lấy thống kê từ server
                // wordPairRepository.getUserStatistics(_currentUserName)
                // Tạm thời trả về thống kê local
                stats = mapOf(
                    "completedLevels" to totalRight.value,
                    "totalLevels" to 4,
                    "completionRate" to if (totalQuestion.value > 0) (totalRight.value.toDouble() / totalQuestion.value * 100) else 0.0,
                    "currentGold" to (gold.value ?: 0)
                )
            } catch (e: Exception) {
                println("Error loading user statistics: ${e.message}")
            }
        }
        return stats
    }
}