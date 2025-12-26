package com.example.eduquizz.features.quizzGame.viewmodel

import android.util.Log
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
import com.example.eduquizz.security.GameSessionManager
import com.example.eduquizz.data.api.GameSessionApiService
import com.example.eduquizz.data.api.StartSessionRequest
import com.example.eduquizz.data.api.QuestionData
import com.example.eduquizz.data.api.SubmitScoreRequest
import com.example.eduquizz.data.api.AnswerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val repository: QuestionRepository,
    private val gameSessionManager: GameSessionManager,
    private val gameSessionApiService: GameSessionApiService
) : ViewModel() {
    
    companion object {
        private const val TAG = "QuestionViewModel"
    }
    
    // Server-side validation states
    val isSubmittingScore = mutableStateOf(false)
    val serverValidationResult = mutableStateOf<ServerValidationResult?>(null)
    val useServerValidation = mutableStateOf(true) // Toggle for server validation
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

    //val hiddenChoices = mutableStateOf(mutableSetOf<String>())
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

    fun getAllQuestions(path: String, levelId: String = "LevelEasy") {
        viewModelScope.launch {
            data.value = DataOrException(null, true, null)
            try {
                val result = repository.getAllQuestionQuizGame(path)
                data.value = result

                // Tự động setup questions khi load xong
                result.data?.let { questions ->
                    if (questions.isNotEmpty()) {
                        setupQuestions(questions)
                        
                        // Bắt đầu server session SAU KHI questions đã được setup
                        if (useServerValidation.value && usedQuestions.isNotEmpty()) {
                            Log.d(TAG, "🚀 Auto-starting game session after questions loaded")
                            startGameSessionInternal(levelId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load questions: ${e.message}")
                data.value = DataOrException(null, false, e)
            }
        }
    }

    private fun setupQuestions(questions: ArrayList<QuestionItem>) {
        // Assign unique IDs to each question for server-side validation
        val questionsWithIds = questions.mapIndexed { index, question ->
            question.copy(id = index + 1)
        }
        val shuffled = questionsWithIds.shuffled()
        usedQuestions.clear()
        reserveQuestions.clear()

        // Lấy 10 câu đầu để chơi, phần còn lại làm reserve
        usedQuestions.addAll(shuffled.take(10))
        reserveQuestions.addAll(shuffled.drop(10))
    }

    fun getTotalQuestionCount(): Int {
        return data.value.data?.size ?: 0
    }

    private var currentLevelId: String = "LevelEasy"
    
    fun Init(dataVM: DataViewModel, currentLevel: String) {
        this.dataViewModel = dataVM
        this.currentLevelId = currentLevel
        getAllQuestions("English/QuizGame/$currentLevel", currentLevel)
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
                return when (index) {
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
        
        // Record answer for server-side validation
        recordAnswerForValidation(currentQuestion.id.toString(), choice)

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
        
        // Reset server validation
        gameSessionManager.endSession()
        serverValidationResult.value = null
    }
    
    // ============ SERVER-SIDE VALIDATION METHODS ============
    
    /**
     * Bắt đầu game session với server (internal - được gọi tự động)
     */
    private fun startGameSessionInternal(levelId: String) {
        if (!useServerValidation.value) {
            Log.d(TAG, "⏭️ Server validation disabled, skipping session start")
            return
        }
        
        // End any existing session first
        if (gameSessionManager.hasActiveSession()) {
            Log.d(TAG, "⚠️ Ending existing session before starting new one")
            gameSessionManager.endSession()
        }
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "🎮 Starting game session for level: $levelId")
                
                // Build questions data for server
                val questionsData = usedQuestions.map { q ->
                    QuestionData(
                        questionId = q.id.toString(),
                        question = q.questionText,
                        correctAnswer = q.answer,
                        choices = q.choices
                    )
                }
                
                val request = StartSessionRequest(
                    gameType = "quizgame",
                    levelId = levelId,
                    questions = questionsData
                )
                
                val response = gameSessionApiService.startSession(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val sessionId = response.body()?.sessionId ?: return@launch
                    gameSessionManager.startSession(sessionId, "quizgame", levelId)
                    Log.d(TAG, "✅ Session started: $sessionId")
                } else {
                    Log.e(TAG, "❌ Failed to start session: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception starting session: ${e.message}", e)
            }
        }
    }
    
    /**
     * Ghi nhận câu trả lời để gửi lên server
     * Gọi khi user chọn đáp án
     */
    fun recordAnswerForValidation(questionId: String, answer: String) {
        if (!useServerValidation.value || !gameSessionManager.hasActiveSession()) {
            return
        }
        gameSessionManager.recordAnswer(questionId, answer)
    }
    
    /**
     * Submit điểm lên server để xác thực
     * Gọi khi game kết thúc, trước khi cộng vàng
     * 
     * @param clientScoreOverride Optional score to use instead of score.value (for cases where ViewModel state resets)
     * @param onResult Callback với kết quả validation
     */
    fun submitScoreToServer(clientScoreOverride: Int? = null, onResult: (ServerValidationResult) -> Unit) {
        // Use override if provided, otherwise use score.value
        val clientScoreToUse = clientScoreOverride ?: score.value
        
        if (!useServerValidation.value || !gameSessionManager.hasActiveSession()) {
            Log.d(TAG, "⏭️ No active session, using client score: $clientScoreToUse")
            onResult(ServerValidationResult(
                success = true,
                verifiedScore = clientScoreToUse,
                useClientScore = true,
                message = "Client-side scoring (no server validation)"
            ))
            return
        }
        
        isSubmittingScore.value = true
        
        viewModelScope.launch {
            try {
                Log.d(TAG, "📤 Submitting score to server...")
                Log.d(TAG, "📊 Client score (override=$clientScoreOverride, score.value=${score.value}): $clientScoreToUse")
                Log.d(TAG, "📊 Answers recorded: ${gameSessionManager.getAnswers().size}")
                
                // Build submission with the correct score
                val submission = gameSessionManager.buildSubmission(clientScoreToUse)
                
                val request = SubmitScoreRequest(
                    sessionId = submission.sessionId,
                    answers = submission.answers.map { 
                        AnswerData(it.questionId, it.answer, it.timeToAnswer) 
                    },
                    clientScore = submission.clientScore,
                    signature = submission.signature
                )
                
                val response = gameSessionApiService.submitScore(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Log.d(TAG, "✅ Server verified: score=${body.verifiedScore}, suspicious=${body.flaggedSuspicious}")
                        
                        val result = ServerValidationResult(
                            success = true,
                            verifiedScore = body.verifiedScore ?: clientScoreToUse,
                            flaggedSuspicious = body.flaggedSuspicious ?: false,
                            message = if (body.flaggedSuspicious == true) body.warning else "Score verified"
                        )
                        serverValidationResult.value = result
                        onResult(result)
                    } else {
                        Log.e(TAG, "❌ Server rejected: ${body?.errorCode} - ${body?.errorMessage}")
                        
                        val result = ServerValidationResult(
                            success = false,
                            verifiedScore = 0,
                            errorCode = body?.errorCode,
                            message = body?.errorMessage ?: "Validation failed"
                        )
                        serverValidationResult.value = result
                        onResult(result)
                    }
                } else {
                    Log.e(TAG, "❌ API error: ${response.code()} - ${response.message()}")
                    
                    // Fallback to client score on network error
                    val result = ServerValidationResult(
                        success = true,
                        verifiedScore = score.value,
                        useClientScore = true,
                        message = "Network error, using client score"
                    )
                    serverValidationResult.value = result
                    onResult(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                
                // Fallback to client score on exception
                val result = ServerValidationResult(
                    success = true,
                    verifiedScore = score.value,
                    useClientScore = true,
                    message = "Error: ${e.message}, using client score"
                )
                serverValidationResult.value = result
                onResult(result)
            } finally {
                isSubmittingScore.value = false
                gameSessionManager.endSession()
            }
        }
    }
}

/**
 * Kết quả xác thực từ server
 */
data class ServerValidationResult(
    val success: Boolean,
    val verifiedScore: Int,
    val flaggedSuspicious: Boolean = false,
    val errorCode: String? = null,
    val message: String? = null,
    val useClientScore: Boolean = false
)