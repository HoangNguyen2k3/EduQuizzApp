package com.example.eduquizz.features.wordsearch.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.wordsearch.model.Cell
import com.example.wordsearch.model.Direction
import com.example.wordsearch.model.Word
import kotlin.math.abs

/**
 * ViewModel quản lý state và logic của game Word Search
 */
class WordSearchViewModel : ViewModel() {
    private val gridSize = 8

    private val _wordsToFind = mutableStateListOf(
        Word("ANDROID"),
        Word("KOTLIN"),
        Word("COMPOSE"),
        Word("JETPACK"),
        Word("MOBILE"),
        Word("APP"),
        Word("GAME")
    )
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

    init {
        initializeGrid()
    }

    /**
     * initializeGrid: khoi tao grid chu cai ngau nhien va dat cac tu can tim vao grid
     */
    private fun initializeGrid() {
        val emptyGrid = Array(gridSize) { row ->
            Array(gridSize) { col ->
                Cell(row, col, ' ')
            }
        }
        _wordsToFind.forEach { word ->
            if(!placeWordInGrid(word.word, emptyGrid)){
                println("Failed to place word: ${word.word}. Retrying grid initialization.")
                initializeGrid()
                return
            }
        }

        for (row in 0 until gridSize){
            for (col in 0 until gridSize) {
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

    /**
     * plactoWordInGrid dat 1 tu vao grid theo huong ngau nhien
     */
    private fun placeWordInGrid(word: String, grid: Array<Array<Cell>>): Boolean {
        val directions = Direction.values()
        val maxAttempts = 100
        var attempts = 0

        while (attempts < maxAttempts) {
            attempts++

            val direction = directions.random()

            val startRow: Int
            val startCol: Int
            val rowIncrement: Int
            val colIncrement: Int

            when (direction) {
                Direction.HORIZONTAL -> {
                    startRow = (0 until gridSize).random()
                    startCol = (0..gridSize - word.length).random()
                    rowIncrement = 0;
                    colIncrement = 1;
                }

                Direction.VERTICAL -> {
                    startRow = (0..gridSize - word.length).random()
                    startCol = (0 until gridSize).random()
                    rowIncrement = 1
                    colIncrement = 0
                }

                Direction.DIAGONAL_DOWN -> {
                    startRow = (0..gridSize - word.length).random()
                    startCol = (0..gridSize - word.length).random()
                    rowIncrement = 1
                    colIncrement = 1
                }

                Direction.DIAGONAL_UP -> {
                    startRow = (word.length - 1 until gridSize).random()
                    startCol = (0..gridSize - word.length).random()
                    rowIncrement = -1
                    colIncrement = 1
                }
            }
            // Kiem tra tu co dat duoc khong
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

    /**
     * Kiem tra xem tu do co the dat vao vi tri cu the trong grid khong
     */
    private fun canPlaceWord(
        word: String,
        startRow: Int,
        startCol: Int,
        rowIncrement: Int,
        colIncrement: Int,
        grid: Array<Array<Cell>>
    ): Boolean {
        for (i in word.indices) {
            val row = startRow + i * rowIncrement
            val col = startCol + i * colIncrement

            if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
                return false
            }
            if (grid[row][col].char != ' ' && grid[row][col].char != word[i]) {
                return false
            }
        }
        return true
    }

    /**
     * Khi người chơi chọn 1 ô
     */
    fun onCellSelected(cell: Cell) {
        if (_selectedCells.isNotEmpty()) {
            val lastCell = _selectedCells.last();

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

    /**
     * Kiểm tra 2 ô có kề nhau không
     */
    private fun isAdjacent(cell1: Cell, cell2: Cell): Boolean {
        val rowDiff = abs(cell1.row - cell2.row)
        val colDiff = abs(cell1.col - cell2.col)

        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)
    }

    /**
     * Update section trang thai
     */
    private fun updateSelectionState() {
        for (cell in _grid) {
            cell.isSelected = _selectedCells.any {
                it.row == cell.row && it.col == cell.col
            }
        }
    }

    /**
     * Kiểm tra từ chọn có khớp với 1 trong các từ cần tìm không
     */
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
                    // Cập nhật ô trong grid bằng copy với thuộc tính đã thay đổi
                    _grid[index] = _grid[index].copy(belongsToFoundWord = true)
                }
            }
            updateSelectionState()
            _selectedCells.clear()
        }
    }
    /**
     *  Su dung hint
     */
    fun useHint(): Boolean{
        val hintCost = 10
        return if(_coins.value >= hintCost){
            _coins.value -= hintCost
            true
        }else{
            false
        }
    }

    fun revealHint(): Boolean{
        if(!useHint()){
            return false
        }

        val unfoundWord = _wordsToFind.firstOrNull { !it.isFound } ?: return false
        val hintLetter = unfoundWord.word.random()

        _hintCell.value = _grid.firstOrNull { it.char == hintLetter && !it.belongsToFoundWord }

        return true
    }

    /**
     * reset lua chon
     */
    fun resetSelection() {
        _selectedCells.clear()
        updateSelectionState()
    }

    fun restartGame() {
        _selectedCells.clear()
        _wordsToFind.replaceAll { it.copy(isFound = false) }
        initializeGrid()
    }
}