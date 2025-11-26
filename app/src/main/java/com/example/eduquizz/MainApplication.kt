package com.example.eduquizz

import android.app.Application
import android.os.StrictMode
import android.util.Log
import androidx.work.Configuration
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import com.example.eduquizz.data_save.WorkScheduler

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)


        WorkScheduler.scheduleDailyLastSeenCheck(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

}