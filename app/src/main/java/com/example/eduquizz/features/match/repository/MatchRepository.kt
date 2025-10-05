package com.example.eduquizz.features.match.repository

import com.example.eduquizz.features.match.model.CompletionRequest
import com.example.eduquizz.features.match.model.MatchLevel
import com.example.eduquizz.features.match.model.MatchUserProgress
import com.example.eduquizz.features.match.model.UserStatistics
import retrofit2.Response
import retrofit2.http.*

interface MatchGameApiService {
    @GET("api/matchgame/levels")
    suspend fun getAllLevels(): Response<List<MatchLevel>>

    @GET("api/matchgame/levels/{levelId}")
    suspend fun getLevel(@Path("levelId") levelId: String): Response<MatchLevel>

    @POST("api/matchgame/progress")
    suspend fun saveProgress(@Body request: CompletionRequest): Response<MatchUserProgress>

    @GET("api/matchgame/progress/{userName}/{levelId}")
    suspend fun getLevelCompletion(
        @Path("userName") userName: String,
        @Path("levelId") levelId: String
    ): Response<Map<String, Boolean>>

    @GET("api/matchgame/progress/{userName}")
    suspend fun getAllCompletions(@Path("userName") userName: String): Response<Map<String, Boolean>>

    @GET("api/matchgame/users/{userName}/stats")
    suspend fun getUserStatistics(@Path("userName") userName: String): Response<UserStatistics>
}
class MatchRepository(private val apiService: MatchGameApiService) {

    suspend fun getAllLevels(): Result<List<MatchLevel>> {
        return try {
            val response = apiService.getAllLevels()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load levels: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLevel(levelId: String): Result<MatchLevel> {
        return try {
            val response = apiService.getLevel(levelId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load level: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveProgress(request: CompletionRequest): Result<MatchUserProgress> {
        return try {
            val response = apiService.saveProgress(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to save progress: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLevelCompletion(userName: String, levelId: String): Result<Boolean> {
        return try {
            val response = apiService.getLevelCompletion(userName, levelId)
            if (response.isSuccessful && response.body() != null) {
                val isCompleted = response.body()?.get("completed") ?: false
                Result.success(isCompleted)
            } else {
                Result.failure(Exception("Failed to get completion status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCompletions(userName: String): Result<Map<String, Boolean>> {
        return try {
            val response = apiService.getAllCompletions(userName)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get completions"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserStatistics(userName: String): Result<UserStatistics> {
        return try {
            val response = apiService.getUserStatistics(userName)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get statistics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}