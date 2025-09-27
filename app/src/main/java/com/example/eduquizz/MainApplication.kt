package com.example.eduquizz

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import com.example.eduquizz.data_save.WorkScheduler

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        // Bật cache offline cho Firebase Realtime Database
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)

        // Lên lịch kiểm tra thông báo hằng ngày
        WorkScheduler.scheduleDailyLastSeenCheck(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

}
