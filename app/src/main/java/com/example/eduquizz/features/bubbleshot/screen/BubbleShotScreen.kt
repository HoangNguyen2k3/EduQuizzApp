package com.example.eduquizz.features.bubbleshot.screen

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
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun ForceLandscape() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val oldOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = oldOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
@Composable
fun BubbleShotScreen(viewModel: BubbleShot, navController: NavHostController) {
    val answers = viewModel.answers
    val timer by viewModel.timer
    val question by viewModel.currentQuestion
    val score by viewModel.score

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE3F2FD))) {
        // Nút quay lại
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1976D2)
            )
        }
        Row(Modifier.fillMaxSize()) {
            // Lưới bóng bay
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(answers.size) { idx ->
                    val answer = answers[idx]
                    Card(
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { viewModel.onAnswerSelected(answer) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB3E5FC)),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(answer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Bên phải: Timer, điểm
            Column(
                Modifier
                    .fillMaxHeight()
                    .width(80.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⏰ $timer", fontSize = 32.sp)
                Spacer(Modifier.height(32.dp))
                Text("Score: $score", fontSize = 24.sp)
            }
        }
        // Dưới cùng: Trụ cannon + câu hỏi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Cannon (sau này thay bằng hình)
            Box(
                Modifier
                    .size(80.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Gray, shape = MaterialTheme.shapes.large)
            )
            // Câu hỏi
            Text(
                question.question,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
