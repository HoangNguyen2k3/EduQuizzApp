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
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import com.example.eduquizz.R
@Composable
fun BubbleShotScreen(viewModel: BubbleShot, navController: NavHostController) {
//    ForceLandscape()
    val answers = viewModel.answers
    val timer by viewModel.timer
    val question by viewModel.currentQuestion
    val score by viewModel.score

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFF5722),
            Color(0xFFFF9800),
            Color(0xFFFFC107),
            MaterialTheme.colorScheme.background)))) {
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

        // Phần nội dung chính
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
        ) {
            // Lưới bóng bay
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
                    }
                    else {
                    val selectedAnswer = viewModel.selectedAnswer.value
                    val isCorrect = viewModel.isCorrectAnswer.value
                    val backgroundColor = when {
                        selectedAnswer == answer && isCorrect == true -> Color(0xFF4CAF50) // Green
                        selectedAnswer == answer && isCorrect == false -> Color(0xFFF44336) // Red
                        else -> Color(0xFFB3E5FC)
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .then(
                                if (selectedAnswer == null) Modifier.clickable { viewModel.onAnswerSelected(idx) } else Modifier
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

            // Phần dưới cùng với cannon và câu hỏi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.cannon),
                    contentDescription = "Cannon",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopCenter)
                        .rotate(270f)
                        .offset(x = -30.dp)
                )
                Text(
                    question.question,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
@Preview(showBackground = true)
@Composable
fun BubbleShotScreenPreview() {
    BubbleShotScreen(BubbleShot(), NavHostController(LocalContext.current))
}

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
