package com.example.eduquizz.dI

import com.example.eduquizz.features.quizzGame.network.QuizGameApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/.") // Thay đổi thành URL backend của bạn
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideQuizGameApi(retrofit: Retrofit): QuizGameApi {
        return retrofit.create(QuizGameApi::class.java)
    }
}