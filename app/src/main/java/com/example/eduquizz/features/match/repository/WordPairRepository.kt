//package com.example.eduquizz.features.match.repository
//
//import android.content.Context
//import com.example.eduquizz.features.match.model.WordPair
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.*
//import javax.inject.Inject
//import javax.inject.Singleton
//
//// API Response Models
//data class MatchWordPair(
//    val word: String,
//    val definition: String
//)
//
//data class MatchLevelResponse(
//    val levelId: String,
//    val title: String,
//    val difficulty: String,
//    val pairCount: Int,
//    val pairs: List<MatchWordPair>
//)
//
//data class MatchLevel(
//    val levelId: String,
//    val title: String,
//    val difficulty: String,
//    val pairCount: Int,
//    val pairs: List<MatchWordPair>
//)
//
//data class CompletionRequest(
//    val userName: String,
//    val levelId: String,
//    val completed: Boolean,
//    val timeSpent: String
//)
//
//data class CompletionResponse(
//    val completed: Boolean
//)
//
//data class UserProgressResponse(
//    val username: String,
//    val levelId: String,
//    val completed: Boolean,
//    val timeSpent: String?
//)
//
//// API Service Interface
//interface MatchGameApiService {
//    @GET("api/matchgame/levels")
//    suspend fun getAllLevels(): Response<List<MatchLevel>>
//
//    @GET("api/matchgame/levels/{levelId}")
//    suspend fun getLevelById(@Path("levelId") levelId: String): Response<MatchLevelResponse>
//
//    @POST("api/matchgame/progress")
//    suspend fun saveLevelCompletion(@Body request: CompletionRequest): Response<UserProgressResponse>
//
//    @GET("api/matchgame/progress/{userName}/{levelId}")
//    suspend fun getLevelCompletion(
//        @Path("userName") userName: String,
//        @Path("levelId") levelId: String
//    ): Response<CompletionResponse>
//
//    @GET("api/matchgame/progress/{userName}")
//    suspend fun getAllLevelCompletions(
//        @Path("userName") userName: String
//    ): Response<Map<String, Boolean>>
//
//    @GET("api/matchgame/users/{userName}/stats")
//    suspend fun getUserStatistics(
//        @Path("userName") userName: String
//    ): Response<Map<String, Any>>
//}
//
//data class WordMatchData(
//    val difficulty: String = "",
//    val levelCount: Int = 4,
//    val title: String = "",
//    val pairCount: Int = 0,
//    val wordPairs: List<WordPair> = emptyList()
//)
//
//data class LevelData(
//    val id: String,
//    val title: String,
//    val difficulty: String,
//    val pairCount: Int,
//    val isCompleted: Boolean = false
//)
//
//@Singleton
//class WordPairRepository @Inject constructor(
//    @ApplicationContext private val context: Context
//) {
//    // Thay đổi BASE_URL thành địa chỉ server Spring Boot của bạn
//    private val BASE_URL = "http://192.168.1.100:8080/"  // Thay đổi IP này
//
//    private val apiService: MatchGameApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(MatchGameApiService::class.java)
//    }
//
//    suspend fun getWordPairsByTopic(topicId: String): Result<WordMatchData> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.getLevelById(topicId)
//                if (response.isSuccessful && response.body() != null) {
//                    val levelData = response.body()!!
//                    val wordPairs = levelData.pairs.map {
//                        WordPair(word = it.word, definition = it.definition)
//                    }
//                    val data = WordMatchData(
//                        difficulty = levelData.difficulty,
//                        levelCount = 4,
//                        title = levelData.title,
//                        pairCount = levelData.pairCount,
//                        wordPairs = wordPairs
//                    )
//                    Result.success(data)
//                } else {
//                    Result.failure(Exception("Level not found: ${response.message()}"))
//                }
//            }
//        } catch (e: Exception) {
//            // Fallback to local data on network error
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getAllWordPairs(): List<WordPair> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.getAllLevels()
//                if (response.isSuccessful && response.body() != null) {
//                    val allWordPairs = mutableListOf<WordPair>()
//                    response.body()!!.forEach { level ->
//                        level.pairs.forEach { pair ->
//                            allWordPairs.add(WordPair(word = pair.word, definition = pair.definition))
//                        }
//                    }
//                    allWordPairs.shuffled()
//                } else {
//                    getDefaultWordPairs()
//                }
//            }
//        } catch (e: Exception) {
//            // Fallback to default data on network error
//            getDefaultWordPairs()
//        }
//    }
//
//    suspend fun getWordPairsByLevel(level: Int): List<WordPair> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val levelId = "Level${level + 1}"
//                val response = apiService.getLevelById(levelId)
//                if (response.isSuccessful && response.body() != null) {
//                    response.body()!!.pairs.map {
//                        WordPair(word = it.word, definition = it.definition)
//                    }
//                } else {
//                    // Fallback: lấy từ tất cả levels
//                    val allPairs = getAllWordPairs()
//                    val start = level * 5
//                    val end = minOf((level + 1) * 5, allPairs.size)
//                    if (start < allPairs.size) {
//                        allPairs.subList(start, end)
//                    } else {
//                        emptyList()
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            // Fallback: lấy 5 cặp từ dữ liệu mặc định
//            getDefaultWordPairs().drop(level * 5).take(5)
//        }
//    }
//
//    suspend fun saveLevelCompletion(userName: String, levelId: String, isCompleted: Boolean, timeSpent: String = ""): Result<Unit> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val request = CompletionRequest(
//                    userName = userName,
//                    levelId = levelId,
//                    completed = isCompleted,
//                    timeSpent = timeSpent
//                )
//                val response = apiService.saveLevelCompletion(request)
//                if (response.isSuccessful) {
//                    // Cũng lưu vào SharedPreferences làm backup
//                    saveToLocalStorage(userName, levelId, isCompleted)
//                    Result.success(Unit)
//                } else {
//                    // Lưu vào local storage nếu API call thất bại
//                    saveToLocalStorage(userName, levelId, isCompleted)
//                    Result.failure(Exception("Failed to save to server: ${response.message()}"))
//                }
//            }
//        } catch (e: Exception) {
//            // Lưu vào local storage nếu có lỗi network
//            saveToLocalStorage(userName, levelId, isCompleted)
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getLevelCompletion(userName: String, levelId: String): Result<Boolean> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.getLevelCompletion(userName, levelId)
//                if (response.isSuccessful && response.body() != null) {
//                    Result.success(response.body()!!.completed)
//                } else {
//                    // Fallback to local storage
//                    val isCompleted = getFromLocalStorage(userName, levelId)
//                    Result.success(isCompleted)
//                }
//            }
//        } catch (e: Exception) {
//            // Fallback to local storage
//            val isCompleted = getFromLocalStorage(userName, levelId)
//            Result.success(isCompleted)
//        }
//    }
//
//    suspend fun getAllLevelCompletions(userName: String): Result<Map<String, Boolean>> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.getAllLevelCompletions(userName)
//                if (response.isSuccessful && response.body() != null) {
//                    Result.success(response.body()!!)
//                } else {
//                    // Fallback to local storage
//                    val completions = getAllFromLocalStorage(userName)
//                    Result.success(completions)
//                }
//            }
//        } catch (e: Exception) {
//            // Fallback to local storage
//            val completions = getAllFromLocalStorage(userName)
//            Result.success(completions)
//        }
//    }
//
//    suspend fun saveUserProgress(userName: String, level: Int, totalRight: Int, totalQuestions: Int): Result<Unit> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
//                sharedPreferences.edit()
//                    .putInt("user_${userName}_current_level", level)
//                    .putInt("user_${userName}_total_right", totalRight)
//                    .putInt("user_${userName}_total_questions", totalQuestions)
//                    .apply()
//                Result.success(Unit)
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getUserProgress(userName: String): Result<Triple<Int, Int, Int>> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
//                val currentLevel = sharedPreferences.getInt("user_${userName}_current_level", 0)
//                val totalRight = sharedPreferences.getInt("user_${userName}_total_right", 0)
//                val totalQuestions = sharedPreferences.getInt("user_${userName}_total_questions", 0)
//                Result.success(Triple(currentLevel, totalRight, totalQuestions))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getAvailableLevels(): Result<List<LevelData>> {
//        return try {
//            withContext(Dispatchers.IO) {
//                val response = apiService.getAllLevels()
//                if (response.isSuccessful && response.body() != null) {
//                    val levels = response.body()!!.map { level ->
//                        LevelData(
//                            id = level.levelId,
//                            title = level.title,
//                            difficulty = level.difficulty,
//                            pairCount = level.pairCount
//                        )
//                    }
//                    Result.success(levels)
//                } else {
//                    Result.success(getDefaultLevels())
//                }
//            }
//        } catch (e: Exception) {
//            Result.success(getDefaultLevels())
//        }
//    }
//
//    // Helper methods for local storage
//    private fun saveToLocalStorage(userName: String, levelId: String, isCompleted: Boolean) {
//        val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
//        sharedPreferences.edit()
//            .putBoolean("user_${userName}_level_${levelId}_completed", isCompleted)
//            .apply()
//    }
//
//    private fun getFromLocalStorage(userName: String, levelId: String): Boolean {
//        val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
//        return sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
//    }
//
//    private fun getAllFromLocalStorage(userName: String): Map<String, Boolean> {
//        val sharedPreferences = context.getSharedPreferences("word_match_prefs", Context.MODE_PRIVATE)
//        val completions = mutableMapOf<String, Boolean>()
//        val levels = listOf("Level1", "Level2", "Level3", "Level4")
//        levels.forEach { levelId ->
//            completions[levelId] = sharedPreferences.getBoolean("user_${userName}_level_${levelId}_completed", false)
//        }
//        return completions
//    }
//
//    private fun getDefaultLevels(): List<LevelData> {
//        return listOf(
//            LevelData("Level1", "Beginner", "Easy", 5),
//            LevelData("Level2", "Intermediate", "Medium", 5),
//            LevelData("Level3", "Advanced", "Hard", 5),
//            LevelData("Level4", "Expert", "Expert", 5)
//        )
//    }
//
//    fun getDefaultWordPairs(): List<WordPair> {
//        return listOf(
//            WordPair("Apple", "a round fruit with firm, white flesh and a green, red, or yellow skin"),
//            WordPair("Dog", "A domestic animal"),
//            WordPair("Sun", "The star at the center of our solar system"),
//            WordPair("Book", "A collection of pages"),
//            WordPair("Computer", "An electronic device for processing data"),
//            WordPair("Flower", "A plant's reproductive organ"),
//            WordPair("Tiger", "A big cat"),
//            WordPair("River", "A natural water flow"),
//            WordPair("Mountain", "A high landform"),
//            WordPair("Car", "A road vehicle"),
//            WordPair("Banana", "A yellow fruit"),
//            WordPair("Cat", "A domestic feline"),
//            WordPair("Moon", "Earth's natural satellite"),
//            WordPair("Notebook", "A collection of blank pages"),
//            WordPair("Phone", "A device for calling"),
//            WordPair("Rose", "A type of flower"),
//            WordPair("Lion", "The king of the jungle"),
//            WordPair("Lake", "A body of water surrounded by land"),
//            WordPair("Hill", "A small elevation of land"),
//            WordPair("Bus", "A large passenger vehicle")
//        )
//    }
//}