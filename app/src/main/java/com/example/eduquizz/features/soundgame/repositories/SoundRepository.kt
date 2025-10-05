package com.example.eduquizz.features.soundgame.repositories

import android.content.Context
import android.util.Log
import com.example.eduquizz.features.soundgame.model.SoundClip
import com.example.eduquizz.features.soundgame.model.SoundLevel
import com.example.eduquizz.features.soundgame.model.SoundLevelData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton

// API Response Models (unchanged)
data class SoundClipResponse(
    val id: Long,
    val clipId: String,
    val name: String,
    val audioUrl: String,
    val answer: String
)

data class SoundLevelResponse(
    val id: Long,
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val clips: List<SoundClipResponse>
)

data class SoundCompletionRequest(
    val username: String,  // Changed from userName to username
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String
)

data class SoundUserProgressResponse(
    val username: String,
    val levelId: String,
    val completed: Boolean,
    val timeSpent: String?
)

// API Service Interface
interface SoundGameApiService {
    @GET("api/sound/levels")
    suspend fun getAllLevels(): Response<List<SoundLevelResponse>>

    @GET("api/sound/levels/{levelId}")
    suspend fun getLevelById(@Path("levelId") levelId: String): Response<SoundLevelResponse>

    @GET("api/sound/levels/{levelId}/random")
    suspend fun getRandomClip(@Path("levelId") levelId: String): Response<SoundClipResponse>

    @POST("api/sound/check")
    suspend fun checkAnswer(
        @Query("clipId") clipId: String,
        @Query("answer") answer: String
    ): Response<Boolean>

    @POST("api/sound/progress")
    suspend fun saveLevelCompletion(@Body request: SoundCompletionRequest): Response<SoundUserProgressResponse>

    @GET("api/sound/progress/{userName}/{levelId}")
    suspend fun getLevelCompletion(
        @Path("userName") userName: String,
        @Path("levelId") levelId: String
    ): Response<SoundUserProgressResponse>
}

@Singleton
class SoundRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SoundRepository"
    }

    // CHANGE THIS to your computer's IP address when testing on physical device
    // Use "http://10.0.2.2:8080/" for emulator
    private val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiService: SoundGameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoundGameApiService::class.java)
    }

    suspend fun getAllLevels(): Result<List<SoundLevel>> {
        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Fetching all levels from: ${BASE_URL}api/sound/levels")
                val response = apiService.getAllLevels()

                if (response.isSuccessful && response.body() != null) {
                    val levels = response.body()!!.map { levelResponse ->
                        Log.d(TAG, "Level: ${levelResponse.levelId} has ${levelResponse.clips.size} clips")
                        SoundLevel(
                            id = levelResponse.id,
                            levelId = levelResponse.levelId,
                            title = levelResponse.title,
                            difficulty = levelResponse.difficulty,
                            questionCount = levelResponse.questionCount,
                            clips = levelResponse.clips.map { clip ->
                                SoundClip(
                                    id = clip.id,
                                    clipId = clip.clipId,
                                    name = clip.name,
                                    audioUrl = clip.audioUrl,
                                    answer = clip.answer
                                )
                            }
                        )
                    }
                    Log.d(TAG, "Successfully fetched ${levels.size} levels")
                    Result.success(levels)
                } else {
                    val error = "Failed to fetch levels: ${response.code()} - ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching levels", e)
            Result.failure(e)
        }
    }

    suspend fun getLevelById(levelId: String): Result<SoundLevel> {
        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Fetching level: $levelId from: ${BASE_URL}api/sound/levels/$levelId")
                val response = apiService.getLevelById(levelId)

                if (response.isSuccessful && response.body() != null) {
                    val levelResponse = response.body()!!
                    Log.d(TAG, "Level $levelId has ${levelResponse.clips.size} clips")

                    if (levelResponse.clips.isEmpty()) {
                        Log.w(TAG, "Warning: Level $levelId has no clips!")
                    }

                    val level = SoundLevel(
                        id = levelResponse.id,
                        levelId = levelResponse.levelId,
                        title = levelResponse.title,
                        difficulty = levelResponse.difficulty,
                        questionCount = levelResponse.questionCount,
                        clips = levelResponse.clips.map { clip ->
                            Log.d(TAG, "Clip: ${clip.clipId} - ${clip.name}")
                            SoundClip(
                                id = clip.id,
                                clipId = clip.clipId,
                                name = clip.name,
                                audioUrl = clip.audioUrl,
                                answer = clip.answer
                            )
                        }
                    )
                    Result.success(level)
                } else {
                    val error = "Level not found: ${response.code()} - ${response.message()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching level $levelId", e)
            Result.failure(e)
        }
    }

    suspend fun getRandomClip(levelId: String): Result<SoundClip> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getRandomClip(levelId)
                if (response.isSuccessful && response.body() != null) {
                    val clipResponse = response.body()!!
                    val clip = SoundClip(
                        id = clipResponse.id,
                        clipId = clipResponse.clipId,
                        name = clipResponse.name,
                        audioUrl = clipResponse.audioUrl,
                        answer = clipResponse.answer
                    )
                    Result.success(clip)
                } else {
                    Result.failure(Exception("No clip found: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAnswer(clipId: String, answer: String): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Checking answer for clip: $clipId, answer: $answer")
                val response = apiService.checkAnswer(clipId, answer)
                if (response.isSuccessful && response.body() != null) {
                    val isCorrect = response.body()!!
                    Log.d(TAG, "Answer is ${if (isCorrect) "correct" else "wrong"}")
                    Result.success(isCorrect)
                } else {
                    Result.failure(Exception("Failed to check answer: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking answer", e)
            Result.failure(e)
        }
    }

    suspend fun saveLevelCompletion(userName: String, levelId: String, isCompleted: Boolean, timeSpent: String = ""): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val request = SoundCompletionRequest(
                    username = userName,
                    levelId = levelId,
                    completed = isCompleted,
                    timeSpent = timeSpent
                )
                val response = apiService.saveLevelCompletion(request)
                if (response.isSuccessful) {
                    saveToLocalStorage(userName, levelId, isCompleted)
                    Result.success(Unit)
                } else {
                    saveToLocalStorage(userName, levelId, isCompleted)
                    Result.failure(Exception("Failed to save to server: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            saveToLocalStorage(userName, levelId, isCompleted)
            Result.failure(e)
        }
    }

    suspend fun getLevelCompletion(userName: String, levelId: String): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getLevelCompletion(userName, levelId)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!.completed)
                } else {
                    val isCompleted = getFromLocalStorage(userName, levelId)
                    Result.success(isCompleted)
                }
            }
        } catch (e: Exception) {
            val isCompleted = getFromLocalStorage(userName, levelId)
            Result.success(isCompleted)
        }
    }

    suspend fun getAvailableLevels(): Result<List<SoundLevelData>> {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiService.getAllLevels()
                if (response.isSuccessful && response.body() != null) {
                    val levels = response.body()!!.map { level ->
                        SoundLevelData(
                            id = level.levelId,
                            title = level.title,
                            difficulty = level.difficulty,
                            questionCount = level.questionCount
                        )
                    }
                    Result.success(levels)
                } else {
                    Result.success(getDefaultLevels())
                }
            }
        } catch (e: Exception) {
            Result.success(getDefaultLevels())
        }
    }

    private fun saveToLocalStorage(userName: String, levelId: String, isCompleted: Boolean) {
        val sharedPreferences = context.getSharedPreferences("sound_game_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean("user_${userName}_level_${levelId}_completed", isCompleted)
            .apply()
    }

    private fun getFromLocalStorage(userName: String, levelId: String): Boolean {
        val sharedPreferences = context.getSharedPreferences("sound_game_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
    }

    private fun getDefaultLevels(): List<SoundLevelData> {
        return listOf(
            SoundLevelData("LevelEasy", "Easy Level", "Easy", 10),
            SoundLevelData("LevelNormal", "Normal Level", "Normal", 10),
            SoundLevelData("LevelHard", "Hard Level", "Hard", 10)
        )
    }
}