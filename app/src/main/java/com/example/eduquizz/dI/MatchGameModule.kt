package com.example.eduquizz.dI

import com.example.eduquizz.features.match.repository.MatchGameApiService
import com.example.eduquizz.features.match.repository.MatchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * MatchGameModule - Sử dụng shared Retrofit từ NetworkModule (có JWT interceptor)
 */
@Module
@InstallIn(SingletonComponent::class)
object MatchGameModule {

    @Provides
    @Singleton
    fun provideMatchGameApiService(retrofit: Retrofit): MatchGameApiService {
        // Sử dụng shared Retrofit từ NetworkModule (có JWT)
        return retrofit.create(MatchGameApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchRepository(apiService: MatchGameApiService): MatchRepository {
        return MatchRepository(apiService)
    }
}