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

        //  15 phút = 15 * 60 * 1000
        val fiftyMinutesMs = 15 * 60 * 1000L

        // Chỉ gửi notification nếu user không vào app >= 15 phút
        if (lastSeen != 0L && now - lastSeen >= fiftyMinutesMs) {
            showReminderNotification()
        }

        return Result.success()
    }

    private fun showReminderNotification() {
        val channelId = "study_reminder_channel"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc nhở học tập",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Thông báo nhắc nhở ôn tập định kỳ"
                enableVibration(false)
                setShowBadge(true)
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Nhắc nhở ôn tập")
            .setContentText("Học đều đặn mỗi ngày giúp củng cố kiến thức hiệu quả hơn")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Việc ôn tập thường xuyên giúp tăng cường khả năng ghi nhớ và hiểu sâu kiến thức. Hãy dành vài phút để tiếp tục hành trình học tập của bạn."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        nm.notify(1001, notification)
    }
}