package com.example.eduquizz.features.wordsearch.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

data class WordSearchData(
    val topicId: String = "",
    val title: String = "",
    val difficulty: String = "",
    val gridSize: Int = 8,
    val wordCount: Int = 0,
    val words: List<String> = emptyList()
)

data class CompletionRequest(
    val userName: String,
    val topicId: String,
    val completed: Boolean,
    val timeSpent: String? = null
)

data class CompletionResponse(
    val completed: Boolean
)

data class UserProgress(
    val id: Long? = null,
    val username: String = "",
    val topicId: String = "",
    val completed: Boolean = false,
    val timeSpent: String? = null,
    val completionDate: String? = null
)

// Retrofit API interface
interface WordSearchApiService {
    @GET("topics/{topicId}")
    suspend fun getWordsByTopic(@Path("topicId") topicId: String): WordSearchData

    @GET("topics")
    suspend fun getAllTopics(): List<WordSearchData>

    @POST("progress")
    suspend fun saveTopicCompletion(@Body request: CompletionRequest): UserProgress

    @GET("progress/{userName}/{topicId}")
    suspend fun getTopicCompletion(
        @Path("userName") userName: String,
        @Path("topicId") topicId: String
    ): CompletionResponse

    @GET("progress/{userName}")
    suspend fun getAllTopicCompletions(@Path("userName") userName: String): Map<String, Boolean>

    @GET("users/{userName}/stats")
    suspend fun getUserStatistics(@Path("userName") userName: String): Map<String, Any>

    @GET("health")
    suspend fun healthCheck(): Map<String, String>
}

@Singleton
class WordSearchRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api/wordsearch/"
    }

    private val apiService: WordSearchApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WordSearchApiService::class.java)
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("word_search_prefs", Context.MODE_PRIVATE)
    }

    suspend fun getWordsByTopic(topicId: String): Result<WordSearchData> {
        return withContext(Dispatchers.IO) {
            try {
                val data = apiService.getWordsByTopic(topicId)
                Result.success(data)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to default data if API fails
                Result.success(getDefaultWordSearchData(topicId))
            }
        }
    }

    private fun getDefaultWordSearchData(topicId: String): WordSearchData {
        return when (topicId) {
            "FunAndGames" -> WordSearchData(
                topicId = "FunAndGames",
                title = "Fun & Games",
                difficulty = "Easy",
                gridSize = 12,
                wordCount = 10,
                words = listOf("PUZZLE", "GAME", "FUN", "PLAY", "ARCADE",
                    "SPORT", "TENNIS", "SOCCER", "BOWLING", "CHESS")
            )
            else -> WordSearchData(
                topicId = "default",
                title = "Default",
                difficulty = "Medium",
                gridSize = 8,
                wordCount = 7,
                words = listOf("ANDROID", "KOTLIN", "COMPOSE", "JETPACK", "MOBILE", "APP", "GAME")
            )
        }
    }

    suspend fun getAllTopics(): Result<List<WordSearchData>> {
        return withContext(Dispatchers.IO) {
            try {
                val topics = apiService.getAllTopics()
                Result.success(topics)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to default topics
                Result.success(getDefaultTopics())
            }
        }
    }

    private fun getDefaultTopics(): List<WordSearchData> {
        return listOf(
            getDefaultWordSearchData("FunAndGames"),
        )
    }

    suspend fun saveTopicCompletion(userName: String, topicId: String, isCompleted: Boolean, timeSpent: String? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Try to save to backend first
                val request = CompletionRequest(userName, topicId, isCompleted, timeSpent)
                apiService.saveTopicCompletion(request)

                // Also save locally as backup
                saveToSharedPreferences(userName, topicId, isCompleted)
                Result.success(Unit)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage
                saveToSharedPreferences(userName, topicId, isCompleted)
                Result.success(Unit)
            }
        }
    }

    private fun saveToSharedPreferences(userName: String, topicId: String, isCompleted: Boolean) {
        sharedPreferences.edit()
            .putBoolean("user_${userName}_topic_${topicId}_completed", isCompleted)
            .apply()
    }

    suspend fun getTopicCompletion(userName: String, topicId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopicCompletion(userName, topicId)
                Result.success(response.completed)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage
                val isCompleted = sharedPreferences.getBoolean("user_${userName}_topic_${topicId}_completed", false)
                Result.success(isCompleted)
            }
        }
    }

    suspend fun getAllTopicCompletions(userName: String): Result<Map<String, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val completions = apiService.getAllTopicCompletions(userName)
                Result.success(completions)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage
                val completions = mutableMapOf<String, Boolean>()
                val topics = listOf("Travel", "FunAndGames", "StudyWork", "Technology")
                topics.forEach { topicId ->
                    completions[topicId] = sharedPreferences.getBoolean("user_${userName}_topic_${topicId}_completed", false)
                }
                Result.success(completions)
            }
        }
    }

    suspend fun getUserStatistics(userName: String): Result<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val stats = apiService.getUserStatistics(userName)
                Result.success(stats)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to basic local stats
                val completions = getAllTopicCompletions(userName).getOrNull() ?: emptyMap()
                val completedCount = completions.values.count { it }
                val stats = mapOf<String, Any>(
                    "completedTopics" to completedCount,
                    "totalTopics" to completions.size,
                    "completionRate" to if (completions.isNotEmpty()) (completedCount.toDouble() / completions.size * 100) else 0.0
                )
                Result.success(stats)
            }
        }
    }

    suspend fun healthCheck(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.healthCheck()
                Result.success(response["status"] ?: "unknown")
            } catch (e: Exception) {
                println("Health check failed: ${e.message}")
                Result.failure(e)
            }
        }
    }
}