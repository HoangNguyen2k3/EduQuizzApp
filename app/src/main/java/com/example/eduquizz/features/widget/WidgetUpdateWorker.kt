package com.example.eduquizz.features.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.*
import com.example.eduquizz.features.mapping.repositories.SceneRepository
import com.example.eduquizz.features.wordsearch.repository.WordSearchRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val widgetProvider = ComponentName(applicationContext, EduQuizzWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)

            if (appWidgetIds.isNotEmpty()) {
                val updateIntent = Intent(applicationContext, EduQuizzWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                applicationContext.sendBroadcast(updateIntent)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "widget_update_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                1, TimeUnit.HOURS
            ).setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

// Helper class để quản lý widget updates
object WidgetUpdateManager {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, EduQuizzWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetProvider)

        val updateIntent = Intent(context, EduQuizzWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(updateIntent)
    }

    fun updateStreak(context: Context, newStreak: Int) {
        // Lưu streak mới
        val prefs = context.getSharedPreferences("app_data", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("user_streak", newStreak)
            .putLong("last_active_date", System.currentTimeMillis())
            .apply()

        // Update widgets
        updateAllWidgets(context)
    }

    fun scheduleWidgetUpdates(context: Context) {
        WidgetUpdateWorker.schedule(context)
    }
}

// Extension function để tích hợp vào ViewModels
fun Context.updateWidgetStreak(streak: Int) {
    WidgetUpdateManager.updateStreak(this, streak)
}