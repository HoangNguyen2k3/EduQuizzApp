package com.example.wordsearch

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.eduquizz.MainActivity
import com.example.wordsearch.ui.screens.IntroductionScreen
import com.example.wordsearch.ui.theme.WordSearchGameTheme

class IntroductionActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val showContinue = intent.getBooleanExtra("showContinue", false)

        setContent {
            WordSearchGameTheme {
                IntroductionScreen(
                    onPlayClicked = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onBackPressed = {
                        finish()
                    },
                    showContinueButton = showContinue
                )
            }
        }
    }
}