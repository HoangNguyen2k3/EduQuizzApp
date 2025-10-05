//package com.example.eduquizz.features.match.viewmodel
//
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.mutableStateListOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.eduquizz.data_save.DataViewModel
//import com.example.eduquizz.features.match.model.MatchCard
//import com.example.eduquizz.features.match.model.WordPair
//import com.example.eduquizz.features.match.repository.WordPairRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class WordMatchGame @Inject constructor(
//    val wordPairRepository: WordPairRepository
//) : ViewModel() {
//
//    // ==== GAME STATES ====
//    val currentLevel = mutableStateOf(0)
//    val timerSeconds = mutableStateOf(40)
//    val showResult = mutableStateOf(false)
//    val showBuyGoldDialog = mutableStateOf(false)
//    val showFinishDialog = mutableStateOf(false)
//    val showTimeOutDialog = mutableStateOf(false)
//    val showLoadingDialog = mutableStateOf(false)
//    val showNetworkErrorDialog = mutableStateOf(false)
//
//    val totalRight = mutableStateOf(0)
//    val totalQuestion = mutableStateOf(0)
//    val gold = mutableStateOf(200)
//    val canPass = mutableStateOf(false)
//
//    // ==== CARDS ====
//    var cards = mutableStateListOf<MatchCard>()
//    var selectedIndices = mutableStateListOf<Int>()
//    var shakingIndices = mutableStateListOf<Int>()
//    var correctIndices = mutableStateListOf<Int>()
//    var wrongIndices = mutableStateListOf<Int>()
//
//    // ==== TIMER ====
//    private var timerJob: Job? = null
//
//    // ==== USER DATA ====
//    private lateinit var dataViewModel: DataViewModel
//    private var currentUserName: String = "defaultUser"
//
//    // ==== INIT ====
//    fun Init(data: DataViewModel, userName: String = "defaultUser") {
//        this.dataViewModel = data
//        this.currentUserName = userName
//
//        viewModelScope.launch {
//            delay(10)
//            gold.value = data.gold.value ?: 200
//            startLevel(0) // Bắt đầu game với Level 0
//        }
//    }
//
//    // ==== START LEVEL ====
//    fun startLevel(level: Int) {
//        viewModelScope.launch {
//            showLoadingDialog.value = true
//            try {
//                val pairs = wordPairRepository.getWordPairsByLevel(level)
//                if (pairs.isNotEmpty()) {
//                    totalQuestion.value += pairs.size
//                    setupLevelWithWordPairs(pairs)
//                } else {
//                    val fallback = wordPairRepository.getDefaultWordPairs().take(5)
//                    totalQuestion.value += fallback.size
//                    setupLevelWithWordPairs(fallback)
//                }
//                currentLevel.value = level
//            } catch (e: Exception) {
//                showNetworkErrorDialog.value = true
//                val fallback = wordPairRepository.getDefaultWordPairs().take(5)
//                setupLevelWithWordPairs(fallback)
//            } finally {
//                showLoadingDialog.value = false
//            }
//        }
//    }
//
//    private fun setupLevelWithWordPairs(wordPairs: List<WordPair>) {
//        val newCards = mutableListOf<MatchCard>()
//        wordPairs.forEachIndexed { idx, pair ->
//            newCards.add(MatchCard(id = idx * 2, text = pair.word, pairId = idx))
//            newCards.add(MatchCard(id = idx * 2 + 1, text = pair.definition, pairId = idx))
//        }
//        cards.clear()
//        cards.addAll(newCards.shuffled())
//        resetSelections()
//        showResult.value = false
//        startTimer()
//    }
//
//    private fun resetSelections() {
//        selectedIndices.clear()
//        shakingIndices.clear()
//        correctIndices.clear()
//        wrongIndices.clear()
//    }
//
//    // ==== TIMER ====
//    private fun startTimer() {
//        timerJob?.cancel()
//        timerSeconds.value = 40
//        timerJob = viewModelScope.launch {
//            while (timerSeconds.value > 0 && !showResult.value) {
//                delay(1000)
//                timerSeconds.value--
//            }
//            if (timerSeconds.value == 0 && !showResult.value) {
//                onTimeOut()
//            }
//        }
//    }
//
//    // ==== GAMEPLAY ====
//    fun onCardClick(index: Int) {
//        if (cards[index].isMatched || selectedIndices.contains(index) || selectedIndices.size == 2) return
//
//        selectedIndices.add(index)
//
//        if (selectedIndices.size == 2) {
//            val first = cards[selectedIndices[0]]
//            val second = cards[selectedIndices[1]]
//
//            if (first.pairId == second.pairId && first.id != second.id) {
//                // Match đúng
//                markCorrect()
//            } else {
//                markWrong()
//            }
//        }
//    }
//
//    private fun markCorrect() {
//        cards[selectedIndices[0]] = cards[selectedIndices[0]].copy(isMatched = true)
//        cards[selectedIndices[1]] = cards[selectedIndices[1]].copy(isMatched = true)
//        correctIndices.addAll(selectedIndices)
//        gold.value += 5
//        totalRight.value += 1
//        selectedIndices.clear()
//
//        if (cards.count { it.isMatched } == cards.size) {
//            viewModelScope.launch {
//                delay(1000)
//                if (currentLevel.value < 3) {
//                    nextLevel()
//                } else {
//                    finishGame()
//                }
//            }
//        }
//    }
//
//    private fun markWrong() {
//        shakingIndices.addAll(selectedIndices)
//        wrongIndices.addAll(selectedIndices)
//        viewModelScope.launch {
//            delay(300)
//            shakingIndices.clear()
//            selectedIndices.clear()
//            delay(1000)
//            wrongIndices.clear()
//        }
//    }
//
//    // ==== NEXT LEVEL ====
//    fun nextLevel() {
//        startLevel(currentLevel.value + 1)
//    }
//
//    // ==== HINT & SKIP ====
//    fun useHint() {
//        if (gold.value < 20) {
//            showBuyGoldDialog.value = true
//            return
//        }
//        gold.value -= 20
//        val unmatched = cards.withIndex().filter { !it.value.isMatched }
//        val pairs = unmatched.groupBy { it.value.pairId }.values.filter { it.size == 2 }
//
//        if (pairs.isNotEmpty()) {
//            val pair = pairs.random()
//            cards[pair[0].index] = cards[pair[0].index].copy(isMatched = true)
//            cards[pair[1].index] = cards[pair[1].index].copy(isMatched = true)
//            correctIndices.add(pair[0].index)
//            correctIndices.add(pair[1].index)
//            totalRight.value += 1
//
//            if (cards.count { it.isMatched } == cards.size) {
//                viewModelScope.launch {
//                    delay(1000)
//                    if (currentLevel.value < 3) {
//                        nextLevel()
//                    } else {
//                        finishGame()
//                    }
//                }
//            }
//        }
//    }
//
//    fun skipLevel() {
//        if (gold.value < 100) {
//            showBuyGoldDialog.value = true
//            return
//        }
//        gold.value -= 100
//        cards.forEachIndexed { i, c ->
//            if (!c.isMatched) {
//                cards[i] = c.copy(isMatched = true)
//                correctIndices.add(i)
//            }
//        }
//        totalRight.value += cards.size / 2
//        showResult.value = true
//        viewModelScope.launch {
//            delay(1000)
//            if (currentLevel.value < 3) {
//                nextLevel()
//            } else {
//                finishGame()
//            }
//        }
//    }
//
//    // ==== FINISH & TIMEOUT ====
//    fun finishGame() {
//        canPass.value = totalRight.value >= 16
//        showFinishDialog.value = true
//    }
//
//    fun onTimeOut() {
//        showResult.value = true
//        timerJob?.cancel()
//        showTimeOutDialog.value = true
//    }
//
//    // ==== BUY GOLD ====
//    fun buyGold(amount: Int) {
//        gold.value += amount
//        showBuyGoldDialog.value = false
//        dataViewModel.updateGold(gold.value ?: 0)
//    }
//
//    // ==== RESET ====
//    fun resetAll() {
//        totalRight.value = 0
//        gold.value = 200
//        correctIndices.clear()
//        wrongIndices.clear()
//        startLevel(0)
//        showFinishDialog.value = false
//        dataViewModel.updateGold(200)
//    }
//}
