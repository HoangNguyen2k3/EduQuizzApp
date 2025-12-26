package com.example.eduquizz.security

import android.util.Base64
import android.util.Log
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GameSessionManager - Quản lý game session và tạo signature cho server validation
 * 
 * Chức năng:
 * 1. Theo dõi câu trả lời và thời gian
 * 2. Tạo HMAC signature để server verify
 * 3. Build submission request cho API
 */
@Singleton
class GameSessionManager @Inject constructor() {

    companion object {
        private const val TAG = "GameSessionManager"
        private const val HMAC_SECRET = "EduQuizz_Game_Session_Secret_2024"
    }

    // Session data
    private var sessionId: String? = null
    private var gameType: String? = null
    private var levelId: String? = null
    private var startTime: Long = 0
    private val answers = mutableListOf<AnswerData>()
    private var lastQuestionTime: Long = 0

    /**
     * Bắt đầu session mới (sau khi nhận sessionId từ server)
     */
    fun startSession(sessionId: String, gameType: String, levelId: String? = null) {
        this.sessionId = sessionId
        this.gameType = gameType
        this.levelId = levelId
        this.startTime = System.currentTimeMillis()
        this.lastQuestionTime = startTime
        this.answers.clear()
        
        Log.d(TAG, "🎮 Session started: $sessionId, game: $gameType")
    }

    /**
     * Ghi nhận khi hiển thị câu hỏi mới
     */
    fun onQuestionDisplayed() {
        lastQuestionTime = System.currentTimeMillis()
    }

    /**
     * Ghi nhận câu trả lời
     * 
     * @param questionId ID của câu hỏi
     * @param answer Đáp án được chọn
     */
    fun recordAnswer(questionId: String, answer: String) {
        val now = System.currentTimeMillis()
        val timeToAnswer = now - lastQuestionTime
        
        answers.add(AnswerData(
            questionId = questionId,
            answer = answer,
            timeToAnswer = timeToAnswer
        ))
        
        lastQuestionTime = now  // Reset for next question
        
        Log.d(TAG, "📝 Recorded: Q=$questionId, A=$answer, time=${timeToAnswer}ms")
    }

    /**
     * Tạo signature HMAC-SHA256
     */
    fun generateSignature(): String {
        val sessionId = this.sessionId ?: throw IllegalStateException("No active session")
        
        val dataBuilder = StringBuilder(sessionId)
        for (answer in answers) {
            dataBuilder.append("|")
                .append(answer.questionId)
                .append(":")
                .append(answer.answer)
        }
        
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(HMAC_SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(secretKey)
            val hash = mac.doFinal(dataBuilder.toString().toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate signature: ${e.message}")
            throw RuntimeException("Signature generation failed", e)
        }
    }

    /**
     * Tính điểm client-side (để gửi lên server verify)
     */
    fun calculateClientScore(correctAnswers: Map<String, String>): Int {
        var score = 0
        for (answer in answers) {
            if (correctAnswers[answer.questionId] == answer.answer) {
                score += 10  // 10 points per correct answer
            }
        }
        return score
    }

    /**
     * Build submission request để gửi lên server
     */
    fun buildSubmission(clientScore: Int): SecureScoreSubmission {
        val sessionId = this.sessionId ?: throw IllegalStateException("No active session")
        
        return SecureScoreSubmission(
            sessionId = sessionId,
            answers = answers.toList(),
            clientScore = clientScore,
            signature = generateSignature()
        )
    }

    /**
     * Lấy session ID hiện tại
     */
    fun getCurrentSessionId(): String? = sessionId

    /**
     * Kiểm tra có session active không
     */
    fun hasActiveSession(): Boolean = sessionId != null

    /**
     * Kết thúc session (clear data)
     */
    fun endSession() {
        Log.d(TAG, "🔚 Session ended: $sessionId")
        sessionId = null
        gameType = null
        levelId = null
        startTime = 0
        answers.clear()
    }

    /**
     * Lấy danh sách answers (for debugging)
     */
    fun getAnswers(): List<AnswerData> = answers.toList()

    // ============ Data Classes ============

    data class AnswerData(
        val questionId: String,
        val answer: String,
        val timeToAnswer: Long  // milliseconds
    )

    data class SecureScoreSubmission(
        val sessionId: String,
        val answers: List<AnswerData>,
        val clientScore: Int,
        val signature: String
    )

    data class QuestionData(
        val questionId: String,
        val question: String,
        val correctAnswer: String,
        val choices: List<String>
    )
}
