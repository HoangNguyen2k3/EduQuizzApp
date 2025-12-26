package com.example.eduquizz.features.ContestOnline.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.data.api.AnswerData
import com.example.eduquizz.data.api.GameSessionApiService
import com.example.eduquizz.data.api.QuestionData
import com.example.eduquizz.data.api.StartSessionRequest
import com.example.eduquizz.data.api.SubmitScoreRequest
import com.example.eduquizz.features.ContestOnline.Data.QuestionRepositoryFromFirebase
import com.example.eduquizz.features.ContestOnline.Model.QuestionItemContest
import com.example.eduquizz.security.GameSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Contest Secure ViewModel - ViewModel với Server-Side Score Validation
 * 
 * Chức năng bảo mật:
 * 1. Server-side session management
 * 2. HMAC signature verification
 * 3. Score recalculation on server
 * 4. Timing analysis for bot detection
 * 5. Session expiry (30 phút)
 */
@HiltViewModel
class ContestSecureViewModel @Inject constructor(
    private val repository: QuestionRepositoryFromFirebase,
    private val gameSessionManager: GameSessionManager,
    private val gameSessionApiService: GameSessionApiService
) : ViewModel() {

    companion object {
        private const val TAG = "ContestSecureVM"
        private const val POINTS_PER_CORRECT = 10
    }

    // UI State
    data class ContestUiState(
        val loading: Boolean = true,
        val questions: List<QuestionItemContest> = emptyList(),
        val error: String? = null,
        val currentIndex: Int = 0,
        val clientScore: Int = 0,
        val timeLeft: Int = 600, // 10 phút
        val showResult: Boolean = false,
        val sessionActive: Boolean = false,
        val submitting: Boolean = false,
        val verifiedScore: Int? = null,
        val validationError: String? = null,
        val flaggedSuspicious: Boolean = false
    )

    private val _uiState = MutableStateFlow(ContestUiState())
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()

    // Map questionIndex -> (questionId, correctAnswer) để tính điểm
    private val questionIdMap = mutableMapOf<Int, Pair<String, String>>()

    /**
     * Load câu hỏi từ Firebase và bắt đầu session với server
     */
    fun startContest(path: String = "English/QuizGame/LevelEasy") {
        viewModelScope.launch {
            _uiState.value = ContestUiState(loading = true)
            
            try {
                // 1. Load câu hỏi từ Firebase
                Log.d(TAG, "🎮 Loading questions from Firebase...")
                val questions = repository.getQuestionsFromFirebase(path)
                
                if (questions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Không có câu hỏi nào"
                    )
                    return@launch
                }

                Log.d(TAG, "📝 Loaded ${questions.size} questions")

                // 2. Tạo QuestionData cho server (có questionId và correctAnswer)
                val serverQuestions = questions.mapIndexed { index, q ->
                    val questionId = "contest_q_$index"
                    questionIdMap[index] = questionId to q.answer
                    
                    QuestionData(
                        questionId = questionId,
                        question = q.question,
                        correctAnswer = q.answer,
                        choices = q.choices
                    )
                }

                // 3. Gọi API bắt đầu session
                Log.d(TAG, "🚀 Starting server session...")
                val request = StartSessionRequest(
                    gameType = "contest",
                    levelId = "online_contest",
                    questions = serverQuestions
                )

                val response = gameSessionApiService.startSession(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val sessionId = response.body()?.sessionId!!
                    
                    // 4. Khởi tạo local session manager
                    gameSessionManager.startSession(sessionId, "contest", "online_contest")
                    
                    Log.d(TAG, "✅ Session started: $sessionId")

                    _uiState.value = ContestUiState(
                        loading = false,
                        questions = questions,
                        sessionActive = true,
                        currentIndex = 0,
                        clientScore = 0,
                        timeLeft = 600
                    )
                } else {
                    Log.e(TAG, "❌ Failed to start session: ${response.body()?.error}")
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = "Không thể kết nối server: ${response.body()?.error ?: "Unknown error"}"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = "Lỗi: ${e.message}"
                )
            }
        }
    }

    /**
     * Ghi nhận câu trả lời và chuyển sang câu tiếp theo
     */
    fun submitAnswer(selectedAnswer: String) {
        val state = _uiState.value
        if (!state.sessionActive || state.showResult) return

        val currentQuestion = state.questions.getOrNull(state.currentIndex) ?: return
        val (questionId, correctAnswer) = questionIdMap[state.currentIndex] ?: return

        // Ghi nhận answer với timing
        gameSessionManager.recordAnswer(questionId, selectedAnswer)

        // Tính điểm client-side (để hiển thị)
        val isCorrect = selectedAnswer == correctAnswer
        val newScore = if (isCorrect) state.clientScore + POINTS_PER_CORRECT else state.clientScore

        Log.d(TAG, "📝 Answer: Q${state.currentIndex + 1}, selected=$selectedAnswer, correct=$isCorrect")

        // Chuyển câu tiếp hoặc kết thúc
        if (state.currentIndex < state.questions.lastIndex) {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                clientScore = newScore
            )
            // Thông báo đã hiển thị câu hỏi mới
            gameSessionManager.onQuestionDisplayed()
        } else {
            // Hết câu hỏi → submit lên server
            _uiState.value = state.copy(
                clientScore = newScore,
                showResult = true
            )
            submitToServer(newScore)
        }
    }

    /**
     * Submit kết quả lên server để validate
     * Server sẽ:
     * 1. Verify HMAC signature
     * 2. Tính lại điểm từ answers
     * 3. So sánh với clientScore
     * 4. Phân tích timing để phát hiện bot
     */
    private fun submitToServer(clientScore: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(submitting = true)

            try {
                if (!gameSessionManager.hasActiveSession()) {
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        validationError = "SESSION_NOT_ACTIVE"
                    )
                    return@launch
                }

                // Build correct answers map để tính điểm
                val correctAnswersMap = questionIdMap.values.associate { it.first to it.second }

                // Tính client score từ GameSessionManager
                val calculatedScore = gameSessionManager.calculateClientScore(correctAnswersMap)

                // Build submission với signature
                val submission = gameSessionManager.buildSubmission(calculatedScore)

                Log.d(TAG, "📤 Submitting to server...")
                Log.d(TAG, "   SessionId: ${submission.sessionId}")
                Log.d(TAG, "   ClientScore: ${submission.clientScore}")
                Log.d(TAG, "   Answers: ${submission.answers.size}")

                val request = SubmitScoreRequest(
                    sessionId = submission.sessionId,
                    answers = submission.answers.map { 
                        AnswerData(it.questionId, it.answer, it.timeToAnswer)
                    },
                    clientScore = submission.clientScore,
                    signature = submission.signature
                )

                val response = gameSessionApiService.submitScore(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val body = response.body()!!
                    Log.d(TAG, "✅ Validation SUCCESS! Verified score: ${body.verifiedScore}")
                    
                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        verifiedScore = body.verifiedScore,
                        flaggedSuspicious = body.flaggedSuspicious ?: false,
                        validationError = null
                    )

                    // Cleanup
                    gameSessionManager.endSession()

                } else {
                    val errorCode = response.body()?.errorCode ?: "UNKNOWN_ERROR"
                    val errorMessage = response.body()?.errorMessage ?: "Validation failed"
                    
                    Log.e(TAG, "❌ Validation FAILED: $errorCode - $errorMessage")

                    _uiState.value = _uiState.value.copy(
                        submitting = false,
                        validationError = errorCode,
                        verifiedScore = null
                    )

                    gameSessionManager.endSession()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Submit exception: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    submitting = false,
                    validationError = "NETWORK_ERROR: ${e.message}"
                )
                gameSessionManager.endSession()
            }
        }
    }

    /**
     * Hết thời gian → auto submit
     */
    fun onTimeUp() {
        if (!_uiState.value.showResult) {
            _uiState.value = _uiState.value.copy(showResult = true)
            submitToServer(_uiState.value.clientScore)
        }
    }

    /**
     * Cập nhật thời gian còn lại
     */
    fun updateTimeLeft(time: Int) {
        _uiState.value = _uiState.value.copy(timeLeft = time)
    }

    /**
     * Reset để chơi lại
     */
    fun resetContest() {
        gameSessionManager.endSession()
        questionIdMap.clear()
        _uiState.value = ContestUiState()
    }

    override fun onCleared() {
        super.onCleared()
        gameSessionManager.endSession()
    }
}
