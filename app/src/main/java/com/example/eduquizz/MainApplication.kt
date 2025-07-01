package com.example.eduquizz

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication :Application(){
    override fun onCreate() {
        super.onCreate()
        // Bật cache offline cho Firebase Realtime Database
       // FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }
}