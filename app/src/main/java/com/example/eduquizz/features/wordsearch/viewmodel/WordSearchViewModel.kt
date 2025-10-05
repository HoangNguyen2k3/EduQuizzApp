package com.example.eduquizz.features.wordsearch.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.widget.StreakManager
import com.example.eduquizz.features.widget.WidgetUpdateManager
import com.example.eduquizz.features.wordsearch.model.Cell
import com.example.eduquizz.features.wordsearch.model.Direction
import com.example.eduquizz.features.wordsearch.model.Word
import com.example.eduquizz.features.wordsearch.repository.WordSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class WordSearchViewModel @Inject constructor(
    private val repository: WordSearchRepository
) : ViewModel() {
    private var userName: String? = null

    fun setUserName(name: String) {
        userName = name
    }

    private var _gridSize = mutableStateOf(8)
    val gridSize: State<Int> get() = _gridSize

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> get() = _error

    private val _currentTopic = mutableStateOf<String?>(null)
    val currentTopic: State<String?> get() = _currentTopic

    private val _wordsToFind = mutableStateListOf<Word>()
    val wordsToFind: List<Word> get() = _wordsToFind

    private val _grid = mutableStateListOf<Cell>()
    val grid: List<Cell> get() = _grid

    private val _selectedCells = mutableStateListOf<Cell>()
    val selectedCells: List<Cell> get() = _selectedCells

    val selectedWord: String get() = _selectedCells.joinToString("") { it.char.toString() }

    private var _coins = mutableStateOf(100)
    val coins: State<Int> get() = _coins

    private var _hintCell = mutableStateOf<Cell?>(null)
    val hintCell: State<Cell?> get() = _hintCell

    private val _isGameCompleted = mutableStateOf(false)
    val isGameCompleted: State<Boolean> get() = _isGameCompleted

    private val _showCompletionDialog = mutableStateOf(false)
    val showCompletionDialog: State<Boolean> get() = _showCompletionDialog

    private var startTime = 0L
    private val _timeSpent = mutableStateOf("00:00")
    val timeSpent: State<String> get() = _timeSpent

    private val _topicCount = mutableStateOf(0)
    val topicCount: State<Int> get() = _topicCount

    private val _totalWordCount = mutableStateOf(0)
    val totalWordCount: State<Int> get() = _totalWordCount

    private val _statisticsLoaded = mutableStateOf(false)
    val statisticsLoaded: State<Boolean> get() = _statisticsLoaded

    fun updateWidgetAfterCompletion(context: Context) {
        StreakManager.updateStreak(context)
        WidgetUpdateManager.updateAllWidgets(context)
    }

    fun getWordOfTheDay(): String {
        return if (_wordsToFind.isNotEmpty()) {
            _wordsToFind.random().word
        } else {
            listOf("ANDROID", "KOTLIN", "COMPOSE", "JETPACK", "MOBILE").random()
        }
    }


    fun loadStatistics() {
        viewModelScope.launch {
            try {
                println("Starting to load statistics...")
                _statisticsLoaded.value = false

                val result = repository.getAllTopics()
                result.onSuccess { topics ->
                    println("Successfully loaded ${topics.size} topics")
                    _topicCount.value = topics.size
                    _totalWordCount.value = topics.sumOf { topic ->
                        topic.wordCount.takeIf { it > 0 } ?: 0
                    }
                    _statisticsLoaded.value = true
                    println("Statistics loaded: Topics=${_topicCount.value}, Words=${_totalWordCount.value}")
                }.onFailure { exception ->
                    println("Failed to load statistics: ${exception.message}")
                    exception.printStackTrace()
                    // Set safe default values
                    _topicCount.value = 1
                    _totalWordCount.value = 7
                    _statisticsLoaded.value = true
                }
            } catch (e: Exception) {
                println("Exception in loadStatistics: ${e.message}")
                e.printStackTrace()
                // Set safe default values
                _topicCount.value = 1
                _totalWordCount.value = 7
                _statisticsLoaded.value = true
            }
        }
    }

    fun loadWordsFromFirebase(topicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _currentTopic.value = topicId

            try {
                val result = repository.getWordsByTopic(topicId)
                result.onSuccess { wordSearchData ->
                    println("Loaded topic: ${wordSearchData.topicId}, Grid size: ${wordSearchData.gridSize}, Words: ${wordSearchData.words.size}")

                    _gridSize.value = wordSearchData.gridSize
                    _wordsToFind.clear()
                    _wordsToFind.addAll(wordSearchData.words.map { Word(it) })
                    initializeGrid()
                }.onFailure { exception ->
                    _error.value = "Failed to load words: ${exception.message}"
                    initializeDefaultWords()
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                initializeDefaultWords()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun testBackendConnection() {
        viewModelScope.launch {
            try {
                val result = repository.healthCheck()
                result.onSuccess { status ->
                    println("Backend connection successful: $status")
                }.onFailure { error ->
                    println("Backend connection failed: ${error.message}")
                }
            } catch (e: Exception) {
                println("Backend connection error: ${e.message}")
            }
        }
    }

    fun initializeWithTopic(topicId: String) {
        if (_currentTopic.value != topicId) {
            loadWordsFromFirebase(topicId)
        }
    }

    private fun initializeDefaultWords() {
        _wordsToFind.clear()
        _wordsToFind.addAll(
            listOf(
                Word("ANDROID"),
                Word("KOTLIN"),
                Word("COMPOSE"),
                Word("JETPACK"),
                Word("MOBILE"),
                Word("APP"),
                Word("GAME")
            )
        )
        initializeGrid()
    }

    private fun initializeGrid() {
        startTime = System.currentTimeMillis()
        val currentGridSize = _gridSize.value
        val emptyGrid = Array(currentGridSize) { row ->
            Array(currentGridSize) { col ->
                Cell(row, col, ' ')
            }
        }

        _wordsToFind.forEach { word ->
            if (!placeWordInGrid(word.word, emptyGrid)) {
                println("Failed to place word: ${word.word}. Retrying grid initialization.")
                initializeGrid()
                return
            }
        }

        for (row in 0 until currentGridSize) {
            for (col in 0 until currentGridSize) {
                if (emptyGrid[row][col].char == ' ') {
                    emptyGrid[row][col] = Cell(row, col, ('A'..'Z').random())
                }
            }
        }

        _grid.clear()
        _grid.addAll(emptyGrid.flatten())

        for (row in emptyGrid) {
            println(row.joinToString(" ") { it.char.toString() })
        }
    }

    private fun placeWordInGrid(word: String, grid: Array<Array<Cell>>): Boolean {
        val currentGridSize = _gridSize.value
        val directions = Direction.values()
        val maxAttempts = 100
        var attempts = 0

        if (word.length > currentGridSize) {
            println("Skip word: $word because it exceeds grid size $currentGridSize")
            return false
        }

        while (attempts < maxAttempts) {
            attempts++
            val direction = directions.random()
            val startRow: Int
            val startCol: Int
            val rowIncrement: Int
            val colIncrement: Int

            when (direction) {
                Direction.HORIZONTAL -> {
                    startRow = (0 until currentGridSize).random()
                    startCol = (0..currentGridSize - word.length).random()
                    rowIncrement = 0
                    colIncrement = 1
                }

                Direction.VERTICAL -> {
                    startRow = (0..currentGridSize - word.length).random()
                    startCol = (0 until currentGridSize).random()
                    rowIncrement = 1
                    colIncrement = 0
                }

                Direction.DIAGONAL_DOWN -> {
                    startRow = (0..currentGridSize - word.length).random()
                    startCol = (0..currentGridSize - word.length).random()
                    rowIncrement = 1
                    colIncrement = 1
                }

                Direction.DIAGONAL_UP -> {
                    startRow = (word.length - 1 until currentGridSize).random()
                    startCol = (0..currentGridSize - word.length).random()
                    rowIncrement = -1
                    colIncrement = 1
                }
            }

            if (canPlaceWord(word, startRow, startCol, rowIncrement, colIncrement, grid)) {
                for (i in word.indices) {
                    val row = startRow + i * rowIncrement
                    val col = startCol + i * colIncrement
                    grid[row][col] = Cell(row, col, word[i])
                }
                println("Placed word: $word at ($startRow, $startCol), direction: $direction")
                return true
            }
        }
        return false
    }

    private fun canPlaceWord(
        word: String,
        startRow: Int,
        startCol: Int,
        rowIncrement: Int,
        colIncrement: Int,
        grid: Array<Array<Cell>>
    ): Boolean {
        val currentGridSize = _gridSize.value
        for (i in word.indices) {
            val row = startRow + i * rowIncrement
            val col = startCol + i * colIncrement
            if (row < 0 || row >= currentGridSize || col < 0 || col >= currentGridSize) {
                return false
            }
            if (grid[row][col].char != ' ' && grid[row][col].char != word[i]) {
                return false
            }
        }
        return true
    }

    fun onCellSelected(cell: Cell) {
        if (_selectedCells.isNotEmpty()) {
            val lastCell = _selectedCells.last()
            val isAdjacent = isAdjacent(lastCell, cell)

            if (lastCell.row == cell.row && lastCell.col == cell.col) {
                _selectedCells.removeAt(_selectedCells.lastIndex)
                updateSelectionState()
                return
            }

            if (!isAdjacent) {
                return
            }

            for (i in 0 until _selectedCells.size - 1) {
                if (_selectedCells[i].row == cell.row && _selectedCells[i].col == cell.col) {
                    while (_selectedCells.size > i + 1) {
                        _selectedCells.removeAt(_selectedCells.lastIndex)
                    }
                    updateSelectionState()
                    return
                }
            }
        }
        _selectedCells.add(cell)
        updateSelectionState()
        checkForMatch()
    }

    private fun isAdjacent(cell1: Cell, cell2: Cell): Boolean {
        val rowDiff = abs(cell1.row - cell2.row)
        val colDiff = abs(cell1.col - cell2.col)
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)
    }

    private fun updateSelectionState() {
        for (cell in _grid) {
            cell.isSelected = _selectedCells.any {
                it.row == cell.row && it.col == cell.col
            }
        }
    }

    private fun checkForMatch() {
        val selectedWord = selectedWord
        val foundWordIndex = _wordsToFind.indexOfFirst {
            !it.isFound && (it.word == selectedWord || it.word == selectedWord.reversed())
        }

        if (foundWordIndex >= 0) {
            _wordsToFind[foundWordIndex] = _wordsToFind[foundWordIndex].copy(isFound = true)
            val cellsToMark = _selectedCells.toList()

            for (cell in cellsToMark) {
                val index = _grid.indexOfFirst { it.row == cell.row && it.col == cell.col }
                if (index >= 0) {
                    _grid[index] = _grid[index].copy(belongsToFoundWord = true)
                }
            }
            updateSelectionState()
            _selectedCells.clear()
            checkGameCompletion()
        }
    }

    private fun checkGameCompletion() {
        val allWordsFound = _wordsToFind.all { it.isFound }
        if (allWordsFound && _wordsToFind.isNotEmpty()) {
            val endTime = System.currentTimeMillis()
            val elapsedMillis = endTime - startTime
            val minutes = (elapsedMillis / 60000).toInt()
            val seconds = ((elapsedMillis % 60000) / 1000).toInt()
            _timeSpent.value = String.format("%02d:%02d", minutes, seconds)
            _isGameCompleted.value = true
            _showCompletionDialog.value = true

            _currentTopic.value?.let { topic ->
                userName?.let { name ->
                    saveTopicCompletion(name, topic)

                } ?: run {
                    println("Lỗi: userName chưa được thiết lập")
                }
            }
        }
    }

    private fun saveTopicCompletion(userName: String, topicId: String) {
        viewModelScope.launch {
            try {
                repository.saveTopicCompletion(userName, topicId, true)
            } catch (e: Exception) {
                println("Error saving topic completion: ${e.message}")
            }
        }
    }

    fun resetCompletionState() {
        _isGameCompleted.value = false
        _showCompletionDialog.value = false
    }

    fun useHint(): Boolean {
        val hintCost = 10
        return if (_coins.value >= hintCost) {
            _coins.value -= hintCost
            true
        } else {
            false
        }
    }

    // Cơ chế Hint
    fun revealHint(): Boolean {
        if (!useHint()) {
            return false
        }

        // tim 1 chu cai trong tu chua dc tim
        val unfoundWord = _wordsToFind.firstOrNull { !it.isFound } ?: return false
        val wordCells = findWordCellsInGrid(unfoundWord.word)
        if (wordCells.isEmpty()) {
            return false
        }

        _hintCell.value = wordCells.random()
        return true
    }

    // tim cell cua 1 tu trong grid
    private fun findWordCellsInGrid(word: String): List<Cell> {
        val currentGridSize = _gridSize.value
        val wordCells = mutableListOf<Cell>()

        for (row in 0 until currentGridSize) {
            for (col in 0 until currentGridSize) {
                if (col + word.length <= currentGridSize) {
                    if (checkWordMatch(word, row, col, 0, 1)) {
                        for (i in word.indices) {
                            wordCells.add(_grid[row * currentGridSize + col + i])
                        }
                    }
                }

                if (row + word.length <= currentGridSize) {
                    if (checkWordMatch(word, row, col, 1, 0)) {
                        for (i in word.indices) {
                            wordCells.add(_grid[(row + i) * currentGridSize + col])
                        }
                    }
                }

                if (row + word.length <= currentGridSize && col + word.length <= currentGridSize) {
                    if (checkWordMatch(word, row, col, 1, 1)) {
                        for (i in word.indices) {
                            wordCells.add(_grid[(row + i) * currentGridSize + col + i])
                        }
                    }
                }

                if (row - word.length + 1 >= 0 && col + word.length <= currentGridSize) {
                    if (checkWordMatch(word, row, col, -1, 1)) {
                        for (i in word.indices) {
                            wordCells.add(_grid[(row - i) * currentGridSize + col + i])
                        }
                    }
                }
            }
        }
        return wordCells.distinctBy { "${it.row},${it.col}" }
    }

    //ktra vtri, huong
    private fun checkWordMatch(
        word: String,
        startRow: Int,
        startCol: Int,
        rowIncrement: Int,
        colIncrement: Int
    ): Boolean {
        val currentGridSize = _gridSize.value

        var matches = true
        for (i in word.indices) {
            val row = startRow + i * rowIncrement
            val col = startCol + i * colIncrement
            if (row < 0 || row >= currentGridSize || col < 0 || col >= currentGridSize) {
                matches = false
                break
            }
            if (_grid[row * currentGridSize + col].char != word[i]) {
                matches = false
                break
            }
        }

        if (matches) return true

        matches = true
        for (i in word.indices) {
            val row = startRow + i * rowIncrement
            val col = startCol + i * colIncrement
            if (row < 0 || row >= currentGridSize || col < 0 || col >= currentGridSize) {
                matches = false
                break
            }
            if (_grid[row * currentGridSize + col].char != word[word.length - 1 - i]) {
                matches = false
                break
            }
        }

        return matches
    }

    fun resetSelection() {
        _selectedCells.clear()
        updateSelectionState()
    }

    fun restartGame() {
        _selectedCells.clear()
        _wordsToFind.replaceAll { it.copy(isFound = false) }
        _isGameCompleted.value = false
        _showCompletionDialog.value = false
        _hintCell.value = null
        initializeGrid()
    }

    fun clearError() {
        _error.value = null
    }
}