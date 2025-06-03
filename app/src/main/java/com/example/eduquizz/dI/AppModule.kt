package com.example.eduquizz.dI

import com.example.eduquizz.network.QuestionApi
import com.example.eduquizz.repository.QuestionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
/*    @Singleton
    @Provides
    fun providesQuestionRepository(api:QuestionApi)
    = QuestionRepository(api)*/
/*    @Singleton
    @Provides
    fun provideQuestionApi(): QuestionApi{
        return Retrofit.Builder().baseUrl("https://raw.githubusercontent.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(QuestionApi::class.java)
    }*/
@Provides
@Singleton
fun provideQuestionRepository(): QuestionRepository {
    return QuestionRepository()  // constructor không cần tham số nếu bạn đã viết lại theo Firebase
}
}