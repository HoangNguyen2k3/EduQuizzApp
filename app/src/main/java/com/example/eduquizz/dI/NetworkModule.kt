package com.example.eduquizz.dI

import com.example.eduquizz.config.ApiConfig
import com.example.eduquizz.features.auth.data.ApiService
import com.example.eduquizz.features.mapping.repositories.SceneApiService
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import com.example.eduquizz.features.quizzGame.network.QuizGameApi
import com.example.eduquizz.features.soundgame.repositories.SoundGameApiService
import com.example.eduquizz.features.wordsearch.repository.WordSearchApiService
import com.example.eduquizz.features.BatChu.repository.BatChuApiService
import com.example.eduquizz.security.JwtAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.Interceptor
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
    
    /**
     * Interceptor để bypass ngrok browser warning
     */
    @Provides
    @Singleton
    fun provideNgrokHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        jwtAuthInterceptor: JwtAuthInterceptor,
        ngrokHeaderInterceptor: Interceptor
    ): OkHttpClient {
        // Certificate Pinning cho ngrok - chống MITM attack
        val certificatePinner = CertificatePinner.Builder()
            .add("*.ngrok-free.dev", "sha256/I2i3UibJLgMPr8JYgCTQqaXNpz6rPhDS794lc8Y+QtA=")
            .build()
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)     // Certificate Pinning enabled
            .addInterceptor(ngrokHeaderInterceptor)   // Bypass ngrok warning
            .addInterceptor(jwtAuthInterceptor)       // JWT auth
            .addInterceptor(loggingInterceptor)       // Logging
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)  // Sử dụng ApiConfig.BASE_URL
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
    
    // ======= Game API Services (shared OkHttpClient with JWT) =======
    
    @Provides
    @Singleton
    fun provideSoundGameApiService(retrofit: Retrofit): SoundGameApiService {
        return retrofit.create(SoundGameApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideWordSearchApiService(retrofit: Retrofit): WordSearchApiService {
        return retrofit.create(WordSearchApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideBatChuApiService(retrofit: Retrofit): BatChuApiService {
        return retrofit.create(BatChuApiService::class.java)
    }
}