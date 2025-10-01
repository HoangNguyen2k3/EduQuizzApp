package com.example.eduquizz.features.BatChu.repository

import android.content.Context
import com.example.eduquizz.features.BatChu.model.DataBatChu
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton

// Data class to map to batChuQuestion.java
data class BatChuQuestion(
    val questionText: String = "",
    val answer: String = "",
    val imageUrl: String = "",
    val suggestion: String? = ""
)

// Data class to map to batChuLevel.java
data class BatChuLevel(
    val levelId: String = "",
    val title: String = "",
    val difficulty: String = "",
    val questionCount: Int = 0,
    val createdAt: String? = null,
    val questions: List<BatChuQuestion> = emptyList()
)

data class CompletionRequest(
    val userName: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String? = null
)

data class CompletionResponse(
    val completed: Boolean
)

data class UserProgress(
    val id: Long? = null,
    val username: String = "",
    val levelId: String = "",
    val completed: Boolean = false,
    val timeSpent: String? = null,
    val completionDate: String? = null
)

// Retrofit API interface for Bat Chu
interface BatChuApiService {
    @GET("levels/{levelId}")
    suspend fun getLevel(@Path("levelId") levelId: String): BatChuLevel

    @GET("levels")
    suspend fun getAllLevels(): List<BatChuLevel>

    @POST("progress")
    suspend fun saveLevelCompletion(@Body request: CompletionRequest): UserProgress

    @GET("progress/{userName}/{levelId}")
    suspend fun getLevelCompletion(
        @Path("userName") userName: String,
        @Path("levelId") levelId: String
    ): CompletionResponse

    @GET("progress/{userName}")
    suspend fun getAllLevelCompletions(@Path("userName") userName: String): Map<String, Boolean>

    @GET("users/{userName}/stats")
    suspend fun getUserStatistics(@Path("userName") userName: String): Map<String, Any>
}

@Singleton
class BatChuRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080/api/batchu/"
    }

    private val apiService: BatChuApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BatChuApiService::class.java)
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("batchu_prefs", Context.MODE_PRIVATE)
    }

    suspend fun getQuestionsByLevel(level: String): Result<List<DataBatChu>> {
        return withContext(Dispatchers.IO) {
            try {
                val batChuLevel = apiService.getLevel(level)
                val questions = batChuLevel.questions.map { q ->
                    val answerChars = q.answer.uppercase().toList()
                    val alphabet = ('A'..'Z').toList()
                    val extraLetters = (alphabet - answerChars.toSet()).shuffled().take(14 - answerChars.size)
                    val shuffled = (answerChars + extraLetters).shuffled()

                    DataBatChu(
                        question = q.questionText,
                        imageUrl = q.imageUrl,
                        answer = q.answer,
                        suggestion = q.suggestion ?: "",
                        shuffledLetters = shuffled
                    )
                }
                Result.success(questions)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage or default data
                Result.success(getDefaultQuestions())
            }
        }
    }

    suspend fun getAllLevels(): Result<List<BatChuLevel>> {
        return withContext(Dispatchers.IO) {
            try {
                val levels = apiService.getAllLevels()
                Result.success(levels)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to default levels
                Result.success(getDefaultLevels())
            }
        }
    }

    suspend fun saveLevelCompletion(request: CompletionRequest): Result<UserProgress> {
        return withContext(Dispatchers.IO) {
            try {
                val progress = apiService.saveLevelCompletion(request)
                // Save to local as fallback
                sharedPreferences.edit()
                    .putBoolean("user_${request.userName}_level_${request.levelId}_completed", request.completed)
                    .apply()
                Result.success(progress)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local
                sharedPreferences.edit()
                    .putBoolean("user_${request.userName}_level_${request.levelId}_completed", request.completed)
                    .apply()
                Result.failure(e)
            }
        }
    }

    suspend fun getLevelCompletion(userName: String, levelId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLevelCompletion(userName, levelId)
                Result.success(response.completed)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage
                val isCompleted = sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
                Result.success(isCompleted)
            }
        }
    }

    suspend fun getAllLevelCompletions(userName: String): Result<Map<String, Boolean>> {
        return withContext(Dispatchers.IO) {
            try {
                val completions = apiService.getAllLevelCompletions(userName)
                Result.success(completions)
            } catch (e: Exception) {
                println("API Error: ${e.message}")
                // Fallback to local storage
                val completions = mutableMapOf<String, Boolean>()
                val levels = listOf("LevelEasy", "LevelNormal", "LevelHard") // Adjust based on known levels
                levels.forEach { levelId ->
                    completions[levelId] = sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
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
                val completions = getAllLevelCompletions(userName).getOrNull() ?: emptyMap()
                val completedCount = completions.values.count { it }
                val stats = mapOf<String, Any>(
                    "completedLevels" to completedCount,
                    "totalLevels" to completions.size,
                    "completionRate" to if (completions.isNotEmpty()) (completedCount.toDouble() / completions.size * 100) else 0.0
                )
                Result.success(stats)
            }
        }
    }

    private fun getDefaultQuestions(): List<DataBatChu> {
        return listOf(
            DataBatChu(
                question = "What is this?",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/example.appspot.com/o/EN%2FCowboy.jpg?alt=media&token=example",
                answer = "COWBOY",
                suggestion = "",
                shuffledLetters = listOf('C', 'O', 'W', 'B', 'O', 'Y', 'X', 'Y', 'Z', 'A', 'B', 'C', 'D', 'E')
            ),
            DataBatChu(
                question = "What is this?",
                imageUrl = "https://firebasestorage.googleapis.com/v0/b/example.appspot.com/o/EN%2FEggplant.jpg?alt=media&token=example",
                answer = "EGGPLANT",
                suggestion = "",
                shuffledLetters = listOf('E', 'G', 'G', 'P', 'L', 'A', 'N', 'T', 'X', 'Y', 'Z', 'A', 'B', 'C')
            )
        )
    }

    private fun getDefaultLevels(): List<BatChuLevel> {
        return listOf(
            BatChuLevel(
                levelId = "LevelEasy",
                title = "Easy Level",
                difficulty = "Easy",
                questionCount = 2,
                questions = getDefaultQuestions().map { q ->
                    BatChuQuestion(
                        questionText = q.question,
                        answer = q.answer,
                        imageUrl = q.imageUrl,
                        suggestion = q.suggestion
                    )
                }
            ),
            BatChuLevel(
                levelId = "LevelNormal",
                title = "Normal Level",
                difficulty = "Normal",
                questionCount = 0,
                questions = emptyList()
            ),
            BatChuLevel(
                levelId = "LevelHard",
                title = "Hard Level",
                difficulty = "Hard",
                questionCount = 0,
                questions = emptyList()
            )
        )
    }
}