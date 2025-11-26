package com.example.eduquizz.features.ContestOnline

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GamePauseReceiver(
    private val onPauseGame: () -> Unit,
    private val onResumeGame: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> onPauseGame()
            Intent.ACTION_USER_PRESENT -> onResumeGame()
        }
    }
}