package com.example.eduquizz.features.bubbleshot.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

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

import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import com.example.eduquizz.R
import androidx.compose.animation.core.*





import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.LaunchedEffect
import com.example.eduquizz.navigation.Routes
import com.example.eduquizz.features.bubbleshot.model.Bubble

@Composable
fun BubbleShotScreen(viewModel: BubbleShot, navController: NavHostController) {
    val answers = viewModel.answers  // Bây giờ là List<Bubble>
    val timer by viewModel.timer
    val question by viewModel.currentQuestion
    val score by viewModel.score
    val isGameOver by viewModel.isGameOver
    val isLoading by viewModel.isLoading

    LocalContext.current
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

                // ===== THAY ĐỔI CHÍNH: Dùng items() với key để tối ưu recomposition =====
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    items(
                        items = answers,
                        key = { bubble -> bubble.id }  // Key giúp Compose track từng bubble
                    ) { bubble ->
                        BubbleItem(
                            bubble = bubble,
                            isSelected = viewModel.selectedAnswer.value?.id == bubble.id,
                            onBubbleClick = {
                                AudioManager.playClickSfx()
                                // Tìm index của bubble
                                val index = answers.indexOf(bubble)
                                if (index != -1) {
                                    viewModel.onAnswerSelected(index)
                                }




























                            }
                        )
                    }
                }

                // Cannon
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

/**
 * Composable riêng cho mỗi bubble
 * Chỉ recompose khi bubble đó thay đổi
 */
@Composable
fun BubbleItem(
    bubble: Bubble,
    isSelected: Boolean,
    onBubbleClick: () -> Unit
) {
    // Animation cho từng bubble
    val infiniteTransition = rememberInfiniteTransition(label = "balloon-${bubble.id}")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = bubble.offsetY,
        targetValue = bubble.offsetY + 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "balloon-offset-${bubble.id}"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .offset(y = offsetY.dp)
            .clickable(enabled = !isSelected) { onBubbleClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.balloon),
            contentDescription = "Balloon",
        )
        Text(
            bubble.answer,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Yellow,
            modifier = Modifier.offset(y = (-8).dp)
        )
    }
}