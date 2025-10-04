package com.example.eduquizz.features.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.eduquizz.MainActivity
import com.example.eduquizz.R
import com.example.eduquizz.features.mapping.repositories.SceneApiService
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EduQuizzWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val widgetType = prefs.getString("widget_type_$appWidgetId", "streak") ?: "streak"

        val views = when (widgetType) {
            "image_quiz" -> createImageQuizWidget(context, appWidgetId)
            "word_of_day" -> createWordOfDayWidget(context)
            else -> createStreakWidget(context)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createStreakWidget(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_streak)

        // Lấy streak từ DataStore/SharedPreferences
        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        val streak = prefs.getInt("user_streak", 0)
        val lastActive = prefs.getLong("last_active_date", 0)

        // Kiểm tra xem streak còn hợp lệ không
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val validStreak = if (lastActive > 0) {
            val daysDiff = (currentDate - lastActive) / (1000 * 60 * 60 * 24)
            if (daysDiff <= 1) streak else 0
        } else 0

        views.setTextViewText(R.id.streak_count, "$validStreak")
        views.setTextViewText(R.id.streak_label, "Day Streak 🔥")

        // Intent để mở app
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        return views
    }

    private fun createImageQuizWidget(context: Context, widgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_image_quiz)

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val imageSource = prefs.getString("image_source_$widgetId", "mapping") ?: "mapping"
        val updateInterval = prefs.getInt("update_interval_$widgetId", 3600000) // mặc định 1 giờ

        // Lấy hình ảnh từ source (mapping hoặc batchu)
        CoroutineScope(Dispatchers.Main).launch {
            loadImageForWidget(context, views, imageSource, widgetId)
        }

        views.setTextViewText(R.id.quiz_hint, "Tap to play!")

        // Intent để mở app ở màn hình tương ứng
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", imageSource)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, widgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        return views
    }

    private fun createWordOfDayWidget(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_word_of_day)

        // Lấy Word of the Day
        CoroutineScope(Dispatchers.Main).launch {
            loadWordOfTheDay(context, views)
        }

        // Intent để mở WordSearch
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "word_search")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        return views
    }

    private suspend fun loadImageForWidget(
        context: Context,
        views: RemoteViews,
        source: String,
        widgetId: Int
    ) {

        try {
            // Load từ cache hoặc API
            val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
            val lastUpdate = prefs.getLong("last_image_update_$widgetId", 0)
            val updateInterval = prefs.getInt("update_interval_$widgetId", 3600000)

            if (System.currentTimeMillis() - lastUpdate > updateInterval) {
                // Cần update image mới
                if (source == "mapping") {
                    loadMappingImage(context, views, widgetId)
                } else {
                    loadBatChuImage(context, views, widgetId)
                }

                prefs.edit()
                    .putLong("last_image_update_$widgetId", System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            Log.e("Widget", "Error loading image", e)
        }
    }
    private suspend fun loadMappingImage(
        context: Context,
        views: RemoteViews,
        widgetId: Int
    ) {
        val repo = SceneRepository(
            retrofit2.Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // chỉnh baseUrl cho mapping API
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(SceneApiService::class.java)
        )

        val levels = repo.getAllLevels()
        if (levels.isNotEmpty()) {
            val firstLevel = levels.first()
            val firstLocation = firstLevel.locations.firstOrNull()
            firstLocation?.let {
                val bitmap = withContext(Dispatchers.IO) {
                    val url = java.net.URL(it.imageUrl)
                    android.graphics.BitmapFactory.decodeStream(url.openStream())
                }
                views.setImageViewBitmap(R.id.widget_container, bitmap)
            }
        }
    }


    private suspend fun loadBatChuImage(
        context: Context,
        views: RemoteViews,
        widgetId: Int
    ) {

    }

    private suspend fun loadWordOfTheDay(context: Context, views: RemoteViews) {
        // Lấy word từ cache hoặc API
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cachedDate = prefs.getString("word_date", "")

        val word = if (cachedDate == today) {
            prefs.getString("word_of_day", "ANDROID") ?: "ANDROID"
        } else {
            // Lấy word mới từ WordSearchRepository
            getRandomWord(context).also {
                prefs.edit()
                    .putString("word_of_day", it)
                    .putString("word_date", today)
                    .apply()
            }
        }

        views.setTextViewText(R.id.word_text, word)
        views.setTextViewText(R.id.date_text, today)
    }

    private fun getRandomWord(context: Context): String {
        // Danh sách từ mẫu
        val words = listOf(
            "ANDROID", "KOTLIN", "COMPOSE", "JETPACK",
            "MOBILE", "WIDGET", "CODING", "DEVELOP"
        )
        return words.random()
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (id in appWidgetIds) {
            editor.remove("widget_type_$id")
            editor.remove("image_source_$id")
            editor.remove("update_interval_$id")
        }
        editor.apply()
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.eduquizz.UPDATE_WIDGET"
    }


}