package com.example.eduquizz.dI

import com.example.eduquizz.features.auth.data.ApiService
import com.example.eduquizz.features.mapping.repositories.SceneApiService
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import com.example.eduquizz.features.quizzGame.network.QuizGameApi
import com.example.eduquizz.security.JwtAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        jwtAuthInterceptor: JwtAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(jwtAuthInterceptor)  // JWT first (adds auth header)
            .addInterceptor(loggingInterceptor)   // Logging second (logs full request)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.16:8080/")
            .client(okHttpClient)  // Use OkHttpClient with interceptors
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideQuizGameApi(retrofit: Retrofit): QuizGameApi {
        return retrofit.create(QuizGameApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSceneApiService(retrofit: Retrofit): SceneApiService {
        return retrofit.create(SceneApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSceneRepository(apiService: SceneApiService): SceneRepository {
        return SceneRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}