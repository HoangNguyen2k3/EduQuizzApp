package com.example.eduquizz.dI

import com.example.eduquizz.features.match.repository.MatchGameApiService
import com.example.eduquizz.features.match.repository.MatchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MatchGameRetrofit

@Module
@InstallIn(SingletonComponent::class)
object MatchGameModule {

    @Provides
    @Singleton
    @MatchGameRetrofit
    fun provideMatchGameOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @MatchGameRetrofit
    fun provideMatchGameRetrofit(@MatchGameRetrofit okHttpClient: OkHttpClient): Retrofit {
        // Thay đổi URL này theo server của bạn
        // 10.0.2.2 cho Android Emulator trỏ đến localhost
        // Hoặc dùng IP thực nếu test trên thiết bị
        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMatchGameApiService(@MatchGameRetrofit retrofit: Retrofit): MatchGameApiService {
        return retrofit.create(MatchGameApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchRepository(apiService: MatchGameApiService): MatchRepository {
        return MatchRepository(apiService)
    }
}