package com.example.eduquizz.features.BatChu.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.features.BatChu.model.DataBatChu

class ViewModelBatChu: ViewModel() {
    val sampleQuestions = listOf(
        DataBatChu(
            question = "What is this?",
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAO",
            suggestion = "Động từ",
            shuffledLetters = listOf('B', 'A', 'P', 'O', 'A', 'B', 'C', 'D', 'F', 'G', 'O', 'I', 'J', 'K')
        ),
        DataBatChu(
            question = "What is this?",
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAOA",
            suggestion = "Động từ",
            shuffledLetters = listOf('H','K', 'A', 'P', 'O', 'A', 'B', 'C', 'D', 'F', 'G', 'O', 'I', 'J', 'K')
        ),
        DataBatChu(
            question = "What is this?",
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAOB",
            suggestion = "Động từ",
            shuffledLetters = listOf('B', 'A', 'P', 'O', 'A', 'B', 'C', 'D', 'F', 'G', 'O', 'I', 'J', 'K')
        )
    )
    var gold = mutableStateOf(-1)
        private set
    val coins = mutableStateOf(-1)
    val resetTimeTrigger =   mutableStateOf(0)
    private lateinit var dataViewModel: DataViewModel
    fun Init(data: DataViewModel) {
        this.dataViewModel = data
        gold = mutableStateOf(data.gold.value ?: 0)
    }
    fun spendCoins(amount: Int) {
        coins.value = (coins.value ?: 0) - amount
        dataViewModel.updateGold(coins.value ?: 0) // <-- chỉ update khi cần
    }
    fun autoSuggestLetter(
        selectedLetters: SnapshotStateList<Char?>,
        usedIndices: SnapshotStateList<Pair<Int, Char>>,
        question: DataBatChu
    ) {
        val answer = question.answer
        val shuffled = question.shuffledLetters

        val alreadySelected = selectedLetters.filterNotNull().toSet()
        val remainingIndices = answer.indices.filter { selectedLetters[it] == null }

        for (targetIndex in remainingIndices) {
            val correctChar = answer[targetIndex]
            val availableShuffledIndex = shuffled.indexOfFirst { ch ->
                ch == correctChar && usedIndices.none { it.first == shuffled.indexOf(ch) }
            }

            if (availableShuffledIndex != -1) {
                selectedLetters[targetIndex] = correctChar
                usedIndices.add(availableShuffledIndex to correctChar)
                break // chỉ gợi ý 1 chữ
            }
        }
    }

}