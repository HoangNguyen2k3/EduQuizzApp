package com.example.eduquizz.features.widget

import android.content.Context
import com.example.eduquizz.features.BatChu.model.DataBatChu
import com.example.eduquizz.features.mapping.model.SceneLocation

/**
 * Helper object để cache dữ liệu cho widgets
 * Giúp widgets có thể hiển thị data mới nhất ngay cả khi app đã đóng
 */
object WidgetCacheHelper {

    private const val BATCHU_CACHE = "batchu_cache"
    private const val MAPPING_CACHE = "mapping_cache"
    private const val WORD_SEARCH_CACHE = "word_search_cache"

    /**
     * Cache dữ liệu BatChu cho widget
     */
    fun cacheBatChuData(context: Context, question: DataBatChu) {
        val prefs = context.getSharedPreferences(BATCHU_CACHE, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("last_question", question.question)
            .putString("last_answer", question.answer)
            .putString("last_image_url", question.imageUrl)
            .putString("last_suggestion", question.suggestion)
            .putLong("last_update_time", System.currentTimeMillis())
            .apply()
    }

    /**
     * Lấy dữ liệu BatChu đã cache
     */
    fun getCachedBatChuData(context: Context): CachedBatChuData? {
        val prefs = context.getSharedPreferences(BATCHU_CACHE, Context.MODE_PRIVATE)
        val question = prefs.getString("last_question", null) ?: return null

        return CachedBatChuData(
            question = question,
            answer = prefs.getString("last_answer", ""),
            imageUrl = prefs.getString("last_image_url", ""),
            suggestion = prefs.getString("last_suggestion", ""),
            updateTime = prefs.getLong("last_update_time", 0)
        )
    }

    /**
     * Cache dữ liệu Mapping cho widget
     */
    fun cacheMappingData(context: Context, location: SceneLocation) {
        val prefs = context.getSharedPreferences(MAPPING_CACHE, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("last_location_name", location.locationName)
            .putString("last_country", location.country)
            .putString("last_image_url", location.imageUrl)
            .putFloat("last_latitude", location.latitude.toFloat())
            .putFloat("last_longitude", location.longitude.toFloat())
            .putString("last_difficulty", location.difficulty)
            .putLong("last_update_time", System.currentTimeMillis())
            .apply()
    }

    /**
     * Lấy dữ liệu Mapping đã cache
     */
    fun getCachedMappingData(context: Context): CachedMappingData? {
        val prefs = context.getSharedPreferences(MAPPING_CACHE, Context.MODE_PRIVATE)
        val locationName = prefs.getString("last_location_name", null) ?: return null

        return CachedMappingData(
            locationName = locationName,
            country = prefs.getString("last_country", ""),
            imageUrl = prefs.getString("last_image_url", ""),
            latitude = prefs.getFloat("last_latitude", 0f).toDouble(),
            longitude = prefs.getFloat("last_longitude", 0f).toDouble(),
            difficulty = prefs.getString("last_difficulty", "EASY"),
            updateTime = prefs.getLong("last_update_time", 0)
        )
    }

    /**
     * Cache Word of the Day
     */
    fun cacheWordOfTheDay(context: Context, word: String, topic: String = "") {
        val prefs = context.getSharedPreferences(WORD_SEARCH_CACHE, Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        prefs.edit()
            .putString("word_of_day", word)
            .putString("word_date", today)
            .putString("word_topic", topic)
            .putLong("last_update_time", System.currentTimeMillis())
            .apply()
    }

    /**
     * Lấy Word of the Day (chỉ valid trong ngày hôm đó)
     */
    fun getWordOfTheDay(context: Context): String? {
        val prefs = context.getSharedPreferences(WORD_SEARCH_CACHE, Context.MODE_PRIVATE)
        val cachedDate = prefs.getString("word_date", "")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        return if (cachedDate == today) {
            prefs.getString("word_of_day", null)
        } else {
            null // Word đã cũ, cần generate mới
        }
    }

    /**
     * Clear tất cả cache
     */
    fun clearAllCache(context: Context) {
        context.getSharedPreferences(BATCHU_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(MAPPING_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
        context.getSharedPreferences(WORD_SEARCH_CACHE, Context.MODE_PRIVATE).edit().clear().apply()
    }
}

/**
 * Data classes cho cached data
 */
data class CachedBatChuData(
    val question: String,
    val answer: String?,
    val imageUrl: String?,
    val suggestion: String?,
    val updateTime: Long
)

data class CachedMappingData(
    val locationName: String,
    val country: String?,
    val imageUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val difficulty: String?,
    val updateTime: Long
)