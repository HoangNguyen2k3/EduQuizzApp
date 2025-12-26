package com.example.eduquizz.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Game Session API Service - API calls for server-side score validation
 */
interface GameSessionApiService {

    /**
     * Start a new game session
     * Server stores questions and generates sessionId
     */
    @POST("api/game/session/start")
    suspend fun startSession(
        @Body request: StartSessionRequest
    ): Response<StartSessionResponse>

    /**
     * Submit game results for validation
     * Server verifies signature, calculates score, checks timing
     */
    @POST("api/game/session/submit")
    suspend fun submitScore(
        @Body request: SubmitScoreRequest
    ): Response<SubmitScoreResponse>
}

// ============ Request/Response DTOs ============

data class QuestionData(
    val questionId: String,
    val question: String,
    val correctAnswer: String,
    val choices: List<String>
)

data class AnswerData(
    val questionId: String,
    val answer: String,
    val timeToAnswer: Long
)

data class StartSessionRequest(
    val gameType: String,
    val levelId: String? = null,
    val questions: List<QuestionData>
)

data class StartSessionResponse(
    val success: Boolean,
    val sessionId: String?,
    val startTime: String?,
    val error: String? = null
)

data class SubmitScoreRequest(
    val sessionId: String,
    val answers: List<AnswerData>,
    val clientScore: Int,
    val signature: String
)

data class SubmitScoreResponse(
    val success: Boolean,
    val verifiedScore: Int? = null,
    val flaggedSuspicious: Boolean? = false,
    val warning: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
