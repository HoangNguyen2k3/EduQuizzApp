package com.example.eduquizz.features.widget

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object WidgetDataFetcher {
    private const val TAG = "WidgetDataFetcher"
    private val gson = Gson()

    /**
     * Fetch random location từ Mapping game
     */
    suspend fun fetchRandomMappingLocation(context: Context): MappingLocationWidget? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:8080/api/scene/levels")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Mapping API Response: $response")

                // Parse response
                val levelsType = object : TypeToken<List<MappingLevelResponse>>() {}.type
                val levels: List<MappingLevelResponse> = gson.fromJson(response, levelsType)

                // Lấy tất cả locations từ các levels
                val allLocations = levels.flatMap { it.locations }

                if (allLocations.isNotEmpty()) {
                    val randomLocation = allLocations.random()
                    return@withContext MappingLocationWidget(
                        imageUrl = randomLocation.imageUrl,
                        locationName = randomLocation.locationName,
                        country = randomLocation.country ?: "Unknown"
                    )
                }
            } else {
                Log.e(TAG, "Mapping API error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching mapping location", e)
        }
        null
    }

    /**
     * Fetch random question từ BatChu game
     */
    suspend fun fetchRandomBatChuQuestion(context: Context): BatChuQuestionWidget? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:8080/api/batchu/levels")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "BatChu API Response: $response")

                val levelsType = object : TypeToken<List<BatChuLevelResponse>>() {}.type
                val levels: List<BatChuLevelResponse> = gson.fromJson(response, levelsType)

                // Lấy tất cả questions từ các levels
                val allQuestions = levels.flatMap { it.questions }

                if (allQuestions.isNotEmpty()) {
                    val randomQuestion = allQuestions.random()
                    return@withContext BatChuQuestionWidget(
                        imageUrl = randomQuestion.imageUrl,
                        answer = randomQuestion.answer,
                        questionText = randomQuestion.questionText
                    )
                }
            } else {
                Log.e(TAG, "BatChu API error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching batchu question", e)
        }
        null
    }

    /**
     * Fetch random word từ WordSearch game
     */
    suspend fun fetchRandomWord(context: Context): WordWidget? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:8080/api/wordsearch/topics")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "WordSearch API Response: $response")

                val topicsType = object : TypeToken<List<WordSearchTopicResponse>>() {}.type
                val topics: List<WordSearchTopicResponse> = gson.fromJson(response, topicsType)

                // Lấy tất cả words từ các topics
                val allWords = topics.flatMap { it.words }

                if (allWords.isNotEmpty()) {
                    val randomWord = allWords.random()
                    val topic = topics.find { it.words.contains(randomWord) }
                    return@withContext WordWidget(
                        word = randomWord,
                        topic = topic?.title ?: "General",
                        difficulty = topic?.difficulty ?: "Medium"
                    )
                }
            } else {
                Log.e(TAG, "WordSearch API error: ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching word", e)
        }
        null
    }

    /**
     * Cache mapping location data
     */
    fun cacheMappingLocation(context: Context, widgetId: Int, data: MappingLocationWidget) {
        val prefs = context.getSharedPreferences("widget_cache_mapping", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("image_url_$widgetId", data.imageUrl)
            .putString("location_name_$widgetId", data.locationName)
            .putString("country_$widgetId", data.country)
            .putLong("last_update_$widgetId", System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Cached mapping location: ${data.locationName}")
    }

    /**
     * Cache batchu question data
     */
    fun cacheBatChuQuestion(context: Context, widgetId: Int, data: BatChuQuestionWidget) {
        val prefs = context.getSharedPreferences("widget_cache_batchu", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("image_url_$widgetId", data.imageUrl)
            .putString("answer_$widgetId", data.answer)
            .putString("question_text_$widgetId", data.questionText)
            .putLong("last_update_$widgetId", System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Cached batchu question: ${data.answer}")
    }

    /**
     * Cache word data
     */
    fun cacheWord(context: Context, data: WordWidget) {
        val prefs = context.getSharedPreferences("widget_cache_word", Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        prefs.edit()
            .putString("word_of_day", data.word)
            .putString("word_topic", data.topic)
            .putString("word_difficulty", data.difficulty)
            .putString("word_date", today)
            .apply()
        Log.d(TAG, "Cached word: ${data.word}")
    }

    /**
     * Get cached mapping location
     */
    fun getCachedMappingLocation(context: Context, widgetId: Int): MappingLocationWidget? {
        val prefs = context.getSharedPreferences("widget_cache_mapping", Context.MODE_PRIVATE)
        val imageUrl = prefs.getString("image_url_$widgetId", null)
        val locationName = prefs.getString("location_name_$widgetId", null)
        val country = prefs.getString("country_$widgetId", null)

        return if (imageUrl != null && locationName != null) {
            MappingLocationWidget(imageUrl, locationName, country ?: "Unknown")
        } else null
    }

    /**
     * Get cached batchu question
     */
    fun getCachedBatChuQuestion(context: Context, widgetId: Int): BatChuQuestionWidget? {
        val prefs = context.getSharedPreferences("widget_cache_batchu", Context.MODE_PRIVATE)
        val imageUrl = prefs.getString("image_url_$widgetId", null)
        val answer = prefs.getString("answer_$widgetId", null)
        val questionText = prefs.getString("question_text_$widgetId", null)

        return if (imageUrl != null && answer != null) {
            BatChuQuestionWidget(imageUrl, answer, questionText ?: "What is this?")
        } else null
    }

    /**
     * Get cached word
     */
    fun getCachedWord(context: Context): WordWidget? {
        val prefs = context.getSharedPreferences("widget_cache_word", Context.MODE_PRIVATE)
        val word = prefs.getString("word_of_day", null)
        val topic = prefs.getString("word_topic", null)
        val difficulty = prefs.getString("word_difficulty", null)

        return if (word != null) {
            WordWidget(word, topic ?: "General", difficulty ?: "Medium")
        } else null
    }

    /**
     * Check if cache is still valid
     */
    fun isCacheValid(context: Context, widgetId: Int, cacheType: String): Boolean {
        val prefs = context.getSharedPreferences("widget_cache_$cacheType", Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong("last_update_$widgetId", 0)
        val updateInterval = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            .getInt("update_interval_$widgetId", 3600000) // Default 1 hour

        return (System.currentTimeMillis() - lastUpdate) < updateInterval
    }
}

// Data models cho widget
data class MappingLocationWidget(
    val imageUrl: String,
    val locationName: String,
    val country: String
)

data class BatChuQuestionWidget(
    val imageUrl: String,
    val answer: String,
    val questionText: String
)

data class WordWidget(
    val word: String,
    val topic: String,
    val difficulty: String
)

// Response models từ API
data class MappingLevelResponse(
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val locations: List<MappingLocationResponse>
)

data class MappingLocationResponse(
    val locationId: String,
    val locationName: String,
    val imageUrl: String,
    val trueLat: Double,
    val trueLon: Double,
    val description: String?,
    val country: String?,
    val city: String?
)

data class BatChuLevelResponse(
    val levelId: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val questions: List<BatChuQuestionResponse>
)

data class BatChuQuestionResponse(
    val questionText: String,
    val answer: String,
    val imageUrl: String,
    val suggestion: String?
)

data class WordSearchTopicResponse(
    val topicId: String,
    val title: String,
    val difficulty: String,
    val gridSize: Int,
    val wordCount: Int,
    val words: List<String>
)