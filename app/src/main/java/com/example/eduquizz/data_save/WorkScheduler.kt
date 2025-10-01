package com.example.eduquizz.data_save

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val UNIQUE_LAST_SEEN_WORK = "last_seen_daily_check"

    fun scheduleDailyLastSeenCheck(context: Context) {
        val req = PeriodicWorkRequestBuilder<LastSeenWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_LAST_SEEN_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }
}



