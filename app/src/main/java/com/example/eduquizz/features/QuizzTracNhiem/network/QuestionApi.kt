package com.example.eduquizz.features.QuizzTracNhiem.network

import com.example.eduquizz.features.QuizzTracNhiem.model.Question
import retrofit2.http.GET

interface QuestionApi {
    @GET("itmmckernan/triviaJSON/master/world.json")
    suspend fun getAllQuestions(): Question
}