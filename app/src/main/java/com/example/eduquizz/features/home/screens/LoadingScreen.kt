package com.example.eduquizz.features.home.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.eduquizz.R
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    isLoading: Boolean = true,
    lottieAnimationRes: Int = R.raw.ghost_loading,
    backgroundColors: List<Color> = listOf(Color(0xFFD6EFFF)), // Mặc định chỉ một màu
    animationSize: Int = 300,
    onLoadingComplete: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottieAnimationRes)
    )

    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isLoading
    )

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(300)
            onLoadingComplete()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = backgroundColors
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { lottieProgress },
                    modifier = Modifier.size(animationSize.dp)
                )
            }

            // Hình ảnh ở dưới cùng
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.loadingtext),
                    contentDescription = "Banner Game",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(200.dp) // hoặc .fillMaxWidth(0.5f) tuỳ bạn
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }


    } else {
        content()
    }
}

@Composable
fun DarkLoadingScreen(
    isLoading: Boolean = true,
    lottieAnimationRes: Int = R.raw.ghost_loading,
    backgroundColors: List<Color> = listOf(Color(0xFF2E3B4E)),
    animationSize: Int = 180,
    onLoadingComplete: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    LoadingScreen(
        isLoading = isLoading,
        lottieAnimationRes = lottieAnimationRes,
        backgroundColors = backgroundColors,
        animationSize = animationSize,
        onLoadingComplete = onLoadingComplete,
        content = content
    )
}

@Composable
fun WithLoading(
    isLoading: Boolean,
    isDarkTheme: Boolean = false,
    backgroundColors: List<Color> = if (isDarkTheme) listOf(Color(0xFF2E3B4E)) else listOf(Color(0xFFD6EFFF)),
    content: @Composable () -> Unit
) {
    if (isDarkTheme) {
        DarkLoadingScreen(
            isLoading = isLoading,
            backgroundColors = backgroundColors,
            content = content
        )
    } else {
        LoadingScreen(
            isLoading = isLoading,
            backgroundColors = backgroundColors,
            content = content
        )
    }
}

@Preview(
    name = "Loading Screen - Light",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun LoadingScreenPreview() {
    QuizAppTheme {
        LoadingScreen(
            isLoading = true,
            backgroundColors = listOf(
                Color(0xFF4A85F5),
                Color(0xFF7B61FF),
                Color(0xFFD6EFFF)
            )
        )
    }
}

@Preview(
    name = "Loading Screen - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoadingScreenDarkPreview() {
    QuizAppTheme {
        DarkLoadingScreen(
            isLoading = true,
            backgroundColors = listOf(
                Color(0xFF2E3B4E),
                Color(0xFF1A2533)
            )
        )
    }
}