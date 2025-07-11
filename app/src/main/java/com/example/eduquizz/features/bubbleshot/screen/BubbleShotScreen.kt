package com.example.eduquizz.features.bubbleshot.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.eduquizz.features.bubbleshot.viewmodel.BubbleShot
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import com.example.eduquizz.R
import androidx.compose.animation.core.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.LaunchedEffect
import com.example.eduquizz.navigation.Routes

@Composable
fun BubbleShotScreen(viewModel: BubbleShot, navController: NavHostController) {
    val answers = viewModel.answers
    val timer by viewModel.timer
    val question by viewModel.currentQuestion
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val isLoading by viewModel.isLoading

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AudioManager.setBgmEnabled(true)
    }
    DisposableEffect(Unit) {
        onDispose {
            AudioManager.setBgmEnabled(false)
        }
    }

    if (isGameOver) {
        val context = LocalContext.current
        Toast.makeText(context, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
        navController.navigate("result/${viewModel.score.value}/${viewModel.totalQuestions.value}/${Routes.BUBBLE_SHOT_INTRO}/${Routes.BUBBLE_SHOT_INTRO}")
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF5722),
                Color(0xFFFF9800),
                Color(0xFFFFC107),
                MaterialTheme.colorScheme.background)))) {

        if (isLoading) {
            // Loading screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color.White,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Đang tải câu hỏi...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .zIndex(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF000000)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("⏰ $timer", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Score: $score", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                val progress = timer.toFloat() / 10f
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.LightGray)
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxSize(),
                            color = if (timer <= 10) Color.Red else Color(0xFF3F51B5),
                            trackColor = Color.Transparent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hiển thị câu hỏi nếu có
                question?.let { currentQuestion ->
                    Text(
                        currentQuestion.question,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    items(answers.size) { idx ->
                        val answer = answers[idx]
                        if (answer == null) {
                            Spacer(modifier = Modifier.size(40.dp))
                        } else {
                            val selectedAnswer = viewModel.selectedAnswer.value

                            // Thêm hiệu ứng di chuyển lên xuống cho bóng
                            val infiniteTransition = rememberInfiniteTransition(label = "balloon-move")
                            val offsetY by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 20f, // Độ lệch tối đa (px)
                                animationSpec = infiniteRepeatable(
                                    animation = tween<Float>(1200, delayMillis = idx * 200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "balloon-offset"
                            )

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .offset(y = offsetY.dp)
                                    .then(
                                        if (selectedAnswer == null) Modifier.clickable {
                                            AudioManager.playClickSfx()
                                            viewModel.onAnswerSelected(idx)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.balloon),
                                    contentDescription = "Balloon",
                                )
                                Text(
                                    answer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Yellow,
                                    modifier = Modifier.offset(y = (-8).dp)
                                )
                            }
                        }
                    }
                }

                // Phần dưới cùng với cannon
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(R.drawable.cannon),
                        contentDescription = "Cannon",
                        modifier = Modifier
                            .size(180.dp)
                            .align(Alignment.BottomCenter)
                            .rotate(270f)
                            .offset(x = -20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleShotScreenPreview() {
    BubbleShotScreen(BubbleShot(), NavHostController(LocalContext.current))
}