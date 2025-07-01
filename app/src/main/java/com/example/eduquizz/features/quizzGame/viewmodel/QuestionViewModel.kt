package com.example.eduquizz.features.quizzGame.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.DataSave.DataViewModel
import com.example.eduquizz.R
import com.example.eduquizz.data.repository.QuestionRepository
import com.example.eduquizz.data.models.DataOrException
import com.example.eduquizz.features.quizzGame.model.QuestionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository
) : ViewModel() {
    val count =  mutableStateOf(0)
    val score =   mutableStateOf(0)
    val choiceSelected =  mutableStateOf("")
    val resetTimeTrigger =   mutableStateOf(0)
    val usedQuestions =   mutableStateListOf<QuestionItem>()
    val reserveQuestions =   mutableStateListOf<QuestionItem>()
    val usedHelperThisQuestion =  mutableStateOf(false)
    val showExpertDialog =   mutableStateOf(false)
    val choiceAttempts =   mutableStateOf(0)
    var gold = mutableStateOf(-1)
        private set
    val hiddenChoices = mutableStateListOf<String>()
    val helperCounts =
        mutableStateListOf(
            R.drawable.nammuoi_vip to 15,
            R.drawable.exchange to 20,
            R.drawable.chuyengiasmall to 15,
            R.drawable.time_two to 10
        )

    val showResultDialog =   mutableStateOf(false)
    val expertAnswer =  mutableStateOf("")
    val twoTimeChoice =  mutableStateOf(false)
    val coins = mutableStateOf(-1)


    //private val _count = MutableStateFlow(0)
    val data: MutableState<DataOrException<ArrayList<QuestionItem>, Boolean, Exception>> =
        mutableStateOf(DataOrException(null, true, null))
    fun getAllQuestions(path:String) {
        viewModelScope.launch {
            data.value = DataOrException(null, true, null) // Bắt đầu loading
            try {
                val result = repository.getAllQuestionQuizGame(path)
                result.loading = false
                data.value = result
            } catch (e: Exception) {
                data.value = DataOrException(null, false, e)
            }
        }
    }
    fun getTotalQuestionCount(): Int {
        return data.value.data?.size ?: 0
    }

    private lateinit var dataViewModel: DataViewModel

    fun Init(data: DataViewModel,currentLevel:String) {
        this.dataViewModel = data
        getAllQuestions("English/QuizGame/$currentLevel")
        gold = mutableStateOf(data.gold.value ?: 0)
    }
    fun spendCoins(amount: Int) {
        coins.value = (coins.value ?: 0) - amount
        dataViewModel.updateGold(coins.value ?: 0) // <-- chỉ update khi cần
    }
    fun ProcessHelperBar(index : Int){
        if(index == 0&&(helperCounts[index].second <= coins.value)&&choiceSelected.value.isEmpty() ){
            val currentQuestion = usedQuestions?.get(count.value)
            if(currentQuestion!=null){
                val wrongAnswers = currentQuestion.choices.filter { it != currentQuestion.answer }
                hiddenChoices.clear()
                hiddenChoices.addAll(wrongAnswers.shuffled().take(2))
                spendCoins(helperCounts[index].second)
                usedHelperThisQuestion.value = true
            }
        }
        else if (index == 1 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
            if (count.value < usedQuestions.size && reserveQuestions.isNotEmpty()) {
                val newQuestion = reserveQuestions.removeAt(0)
                usedQuestions.removeAt(count.value)
                usedQuestions.add(count.value, newQuestion)
                spendCoins(helperCounts[index].second)
                hiddenChoices.clear()
                choiceSelected.value = ""
                resetTimeTrigger.value++
                usedHelperThisQuestion.value = true
            }
        }
        else if(index==2&&helperCounts[index].second <= coins.value&& choiceSelected.value.isEmpty())
        {
            showExpertDialog.value = true
            spendCoins(helperCounts[index].second)
            val currentQuestion = usedQuestions[count.value]
            val correctAnswer = currentQuestion.answer
            val wrongAnswers = currentQuestion.choices.filter { it != correctAnswer }
            fun getLetter(index: Int): String {
                return when(index) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> "?"
                }
            }
            expertAnswer.value = if (Random.nextFloat() < 0.9f) {
                getLetter(currentQuestion.choices.indexOf(correctAnswer))
            } else {
                getLetter(currentQuestion.choices.indexOf(wrongAnswers.random()))
            }
            showExpertDialog.value = true
            usedHelperThisQuestion.value = true
        }else if(index == 3&&helperCounts[index].second <= coins.value&& choiceSelected.value.isEmpty()){
            twoTimeChoice.value = true
            spendCoins(helperCounts[index].second)
            usedHelperThisQuestion.value = true
        }
    }









}

