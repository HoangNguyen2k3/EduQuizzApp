package com.example.eduquizz.features.bubbleshot.screen

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
import com.example.eduquizz.features.bubbleshot.viewmodel.BubbleShot
import androidx.compose.foundation.clickable
import androidx.navigation.NavHostController

@Composable
fun BubbleShotScreen(viewModel: BubbleShot, navController: NavHostController) {
    val answers by viewModel.answers
    val timer by viewModel.timer
    val question by viewModel.currentQuestion
    val gold by viewModel.gold
    val canUseHint by viewModel.canUseHint

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Đáp án phía trên
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(answers) { answer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable { viewModel.onAnswerSelected(answer) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(answer, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // Timer
        Text(
            "⏰ $timer s",
            fontSize = 24.sp,
            color = if (timer <= 2) Color.Red else Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // Câu hỏi phía dưới
        Text(
            question.question,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // Hint + vàng
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Button(
                onClick = { viewModel.useHint() },
                enabled = canUseHint
            ) { Text("Hint (-10)") }
            Text("Gold: $gold", fontWeight = FontWeight.Bold)
        }
    }
} 