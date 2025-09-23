package com.example.eduquizz.features.quizzGame.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data_save.DataViewModel
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
    val count = mutableStateOf(0)
    val score = mutableStateOf(0)
    val choiceSelected = mutableStateOf("")
    val resetTimeTrigger = mutableStateOf(0)
    val usedQuestions = mutableStateListOf<QuestionItem>()
    val reserveQuestions = mutableStateListOf<QuestionItem>()
    val usedHelperThisQuestion = mutableStateOf(false)
    val showExpertDialog = mutableStateOf(false)
    val choiceAttempts = mutableStateOf(0)
    var coins = mutableStateOf(-1)
        private set
    val hiddenChoices = mutableStateListOf<String>()
    val helperCounts = mutableStateListOf(
        R.drawable.nammuoi_vip to 15,
        R.drawable.exchange to 20,
        R.drawable.chuyengiasmall to 15,
        R.drawable.time_two to 10
    )

    val showResultDialog = mutableStateOf(false)
    val expertAnswer = mutableStateOf("")
    val twoTimeChoice = mutableStateOf(false)

    val data: MutableState<DataOrException<ArrayList<QuestionItem>, Boolean, Exception>> =
        mutableStateOf(DataOrException(null, true, null))

    private lateinit var dataViewModel: DataViewModel

    fun getAllQuestions(path: String) {
        viewModelScope.launch {
            data.value = DataOrException(null, true, null)
            try {
                val result = repository.getAllQuestionQuizGame(path)
                data.value = result

                // Tự động setup questions khi load xong
                result.data?.let { questions ->
                    if (questions.isNotEmpty()) {
                        setupQuestions(questions)
                    }
                }
            } catch (e: Exception) {
                data.value = DataOrException(null, false, e)
            }
        }
    }

    private fun setupQuestions(questions: ArrayList<QuestionItem>) {
        val shuffled = questions.shuffled()
        usedQuestions.clear()
        reserveQuestions.clear()

        // Lấy 10 câu đầu để chơi, phần còn lại làm reserve
        usedQuestions.addAll(shuffled.take(10))
        reserveQuestions.addAll(shuffled.drop(10))
    }

    fun getTotalQuestionCount(): Int {
        return data.value.data?.size ?: 0
    }

    fun Init(dataVM: DataViewModel, currentLevel: String) {
        this.dataViewModel = dataVM
        getAllQuestions("English/QuizGame/$currentLevel")
        coins.value = dataVM.gold.value ?: 0
    }

    fun spendCoins(amount: Int) {
        coins.value = (coins.value ?: 0) - amount
        dataViewModel.updateGold(coins.value ?: 0)
    }

    fun ProcessHelperBar(index: Int) {
        if (index == 0 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
            // Helper 50:50
            val currentQuestion = usedQuestions.getOrNull(count.value)
            if (currentQuestion != null) {
                val wrongAnswers = currentQuestion.choices.filter { it != currentQuestion.answer }
                hiddenChoices.clear()
                hiddenChoices.addAll(wrongAnswers.shuffled().take(2))
                spendCoins(helperCounts[index].second)
                usedHelperThisQuestion.value = true
            }
        } else if (index == 1 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
            // Helper đổi câu hỏi
            if (count.value < usedQuestions.size && reserveQuestions.isNotEmpty()) {
                val newQuestion = reserveQuestions.removeAt(0)
                usedQuestions[count.value] = newQuestion
                spendCoins(helperCounts[index].second)
                hiddenChoices.clear()
                choiceSelected.value = ""
                resetTimeTrigger.value++
                usedHelperThisQuestion.value = true
            }
        } else if (index == 2 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
            // Helper chuyên gia
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
        } else if (index == 3 && helperCounts[index].second <= coins.value && choiceSelected.value.isEmpty()) {
            // Helper 2 lần chọn
            twoTimeChoice.value = true
            spendCoins(helperCounts[index].second)
            usedHelperThisQuestion.value = true
        }
    }

    fun nextQuestion() {
        if (count.value < usedQuestions.size - 1) {
            count.value++
            choiceSelected.value = ""
            usedHelperThisQuestion.value = false
            hiddenChoices.clear()
            choiceAttempts.value = 0
            twoTimeChoice.value = false
            resetTimeTrigger.value++
        } else {
            showResultDialog.value = true
        }
    }

    fun selectAnswer(choice: String) {
        if (choiceSelected.value.isNotEmpty() && !twoTimeChoice.value) {
            return // Đã chọn rồi và không có helper 2 lần
        }

        choiceSelected.value = choice
        val currentQuestion = usedQuestions[count.value]

        if (choice == currentQuestion.answer) {
            score.value += 10
        } else if (twoTimeChoice.value && choiceAttempts.value == 0) {
            // Cho phép chọn lần 2
            choiceAttempts.value = 1
            choiceSelected.value = ""
            return
        }

        choiceAttempts.value++
    }

    fun resetGame() {
        count.value = 0
        score.value = 0
        choiceSelected.value = ""
        usedHelperThisQuestion.value = false
        showExpertDialog.value = false
        showResultDialog.value = false
        choiceAttempts.value = 0
        expertAnswer.value = ""
        twoTimeChoice.value = false
        hiddenChoices.clear()
        usedQuestions.clear()
        reserveQuestions.clear()
        resetTimeTrigger.value++
    }
}