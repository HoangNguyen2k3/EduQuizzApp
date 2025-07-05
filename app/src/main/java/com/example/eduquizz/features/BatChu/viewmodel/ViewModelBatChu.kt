package com.example.eduquizz.features.BatChu.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.eduquizz.DataSave.DataViewModel
import com.example.eduquizz.features.BatChu.model.DataBatChu

class ViewModelBatChu: ViewModel() {
    val sampleQuestions = listOf(
        DataBatChu(
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAO",
            suggestion = "Động từ",
            shuffledLetters = listOf('B', 'A', 'P', 'O', 'A', 'B', 'C', 'D', 'F', 'G', 'O', 'I', 'J', 'K')
        ),
        DataBatChu(
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAO",
            suggestion = "Động từ",
            shuffledLetters = listOf('B', 'A', 'P', 'O', 'A', 'B', 'C', 'D', 'F', 'G', 'O', 'I', 'J', 'K')
        ),
        DataBatChu(
            imageUrl = "https://3.bp.blogspot.com/-pzQILmYu4Jw/U8ePEjoEW2I/AAAAAAAACq8/QN8KosNpR70/s1600/2014-07-17+00.43.58-1.png",
            answer = "BAOCAO",
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
}