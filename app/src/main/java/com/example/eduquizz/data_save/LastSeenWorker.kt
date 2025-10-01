package com.example.eduquizz.data_save

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.eduquizz.MainActivity
import com.example.eduquizz.R
import kotlinx.coroutines.flow.first

class LastSeenWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesManager(applicationContext)
        val lastSeen = prefs.lastSeenTsFlow.first()
        val now = System.currentTimeMillis()

        val oneDayMs = 24 * 60 * 60 * 1000L
        if (lastSeen == 0L || now - lastSeen >= oneDayMs) {
            showReminderNotification()
        }
        return Result.success()
    }

    private fun showReminderNotification() {
        val channelId = "study_reminder_channel"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Study Reminder", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(ch)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText("Đã lâu bạn chưa vào học. Quay lại luyện tập nhé!")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        nm.notify(1001, notification)
    }
}


