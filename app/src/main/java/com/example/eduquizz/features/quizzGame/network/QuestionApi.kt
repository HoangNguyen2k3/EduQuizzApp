package com.example.eduquizz.features.quizzGame.network

import com.example.eduquizz.features.quizzGame.model.Question
import retrofit2.http.GET

interface QuestionApi {
    @GET("itmmckernan/triviaJSON/master/world.json")
    suspend fun getAllQuestions(): Question
}