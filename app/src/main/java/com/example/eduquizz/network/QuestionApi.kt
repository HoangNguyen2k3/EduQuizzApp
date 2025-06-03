package com.example.eduquizz.network

import com.example.eduquizz.model.Question
import retrofit2.http.GET

interface QuestionApi {
    @GET("itmmckernan/triviaJSON/master/world.json")
    suspend fun getAllQuestions(): Question
}