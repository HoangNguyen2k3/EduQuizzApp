package com.example.eduquizz.features.quizzGame.network

import com.example.eduquizz.features.quizzGame.model.QuizGameLevelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface QuizGameApi {

    // Sửa endpoint để khớp với backend
    @GET("api/quiz/levels/{levelId}")
    suspend fun getQuizGameLevel(@Path("levelId") levelId: String): Response<QuizGameLevelResponse>

    // Lấy tất cả levels available
    @GET("api/quiz/levels")
    suspend fun getAllLevels(): Response<List<QuizGameLevelResponse>>

    // Health check - có thể thêm vào backend nếu cần
    @GET("api/quiz/health")
    suspend fun healthCheck(): Response<Map<String, String>>
}