package com.example.eduquizz.features.home.screens

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.airbnb.lottie.compose.*
import com.example.eduquizz.R
import com.example.quizapp.ui.theme.QuizAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToMain: () -> Unit = {}) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )
    val systemUiController = rememberSystemUiController()
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color(0xFFD6EFFF),
            darkIcons = true
        )
        systemUiController.setNavigationBarColor(
            color = Color(0xFFD6EFFF),
            darkIcons = true
        )
    }
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500)
            onNavigateToMain()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD6EFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))


            if (composition == null) {
                LoadingDots()
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val alpha by animateFloatAsState(
                targetValue = if(System.currentTimeMillis() / 500 % 3 == index.toLong()) 1f else 0.3f,
                animationSpec = tween(500),
                label = "dot_alpha"
            )

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        Color.White.copy(alpha = alpha),
                        shape = CircleShape
                    )
            )
        }
    }
}
// Dark Theme Splash Screen
@Composable
fun DarkSplashScreen(
    onNavigateToMain: () -> Unit = {}
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.splash_animation)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        isPlaying = true
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500)
            onNavigateToMain()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E3B4E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}

@Preview(
    name = "Splash Screen - Light",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun SplashScreenPreview(){
    QuizAppTheme {
        SplashScreen()
    }
}

@Preview(
    name = "Splash Screen - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SplashScreenDarkPreview() {
    QuizAppTheme {
        DarkSplashScreen()
    }
}
