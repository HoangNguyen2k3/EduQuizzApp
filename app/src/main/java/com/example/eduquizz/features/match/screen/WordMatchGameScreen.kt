package com.example.eduquizz.features.match.screen

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import com.example.eduquizz.features.match.viewmodel.WordMatchGame
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun WordMatchGameScreen(viewModel: WordMatchGame, navController: NavHostController) {
    val gold by viewModel.gold
    val timer by viewModel.timerSeconds
    val level by viewModel.currentLevel
    val showResult by viewModel.showResult
    val showBuyGoldDialog by viewModel.showBuyGoldDialog
    val showFinishDialog by viewModel.showFinishDialog
    val canPass by viewModel.canPass

    val cards = viewModel.cards
    val selectedIndices = viewModel.selectedIndices
    val shakingIndices = viewModel.shakingIndices

    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Level ${level + 1}/4", fontWeight = FontWeight.Bold)
            Text("Gold: $gold", color = Color(0xFFFFB800), fontWeight = FontWeight.Bold)
            Text("⏰ $timer", color = if (timer <= 10) Color.Red else Color.Black)
        }
        // Lưới thẻ
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 cột dọc sát nhau
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(cards) { idx, card ->
                if (!card.isMatched) {
                    val isSelected = selectedIndices.contains(idx)
                    val isShaking = shakingIndices.contains(idx)
                    val shakeAnim = remember { Animatable(0f) }
                    if (isShaking) {
                        LaunchedEffect(isShaking) {
                            shakeAnim.snapTo(0f)
                            shakeAnim.animateTo(
                                targetValue = 1f,
                                animationSpec = repeatable(
                                    iterations = 3,
                                    animation = tween(50, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            shakeAnim.snapTo(0f)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .graphicsLayer (
                                translationX = if (isShaking) shakeAnim.value * 16f else 0f
                            )
                            .background(if (isSelected) Color(0xFFFFF8B3) else Color.White)
                            .clickable(enabled = !showResult && !isSelected) {
                                viewModel.onCardClick(idx)
                            },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(card.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        // Hint/Skip/Next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.useHint() },
                enabled = gold >= 20 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0A9F8))
            ) {
                Text("Hint (-20)", color = Color.White)
            }
            Button(
                onClick = { viewModel.skipLevel() },
                enabled = gold >= 100 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7C873))
            ) {
                Text("Skip (-100)", color = Color.White)
            }
            Button(
                onClick = { viewModel.nextLevel() },
                enabled = cards.all { it.isMatched },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text("Next", color = Color.White)
            }
        }
        // Dialog hết vàng
        if (showBuyGoldDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showBuyGoldDialog.value = false },
                title = { Text("Bạn đã hết vàng!") },
                text = { Text("Bạn muốn mua thêm vàng để tiếp tục chơi?") },
                confirmButton = {
                    Button(onClick = { viewModel.buyGold(200) }) {
                        Text("Mua 200 vàng (20.000đ)")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.showBuyGoldDialog.value = false }) {
                        Text("Huỷ")
                    }
                }
            )
        }
        // Dialog kết thúc game
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Hoàn thành") },
                text = {
                    Text(
                        if (canPass) "Bạn đã qua màn! Số câu đúng: ${viewModel.totalRight.value}/20"
                        else "Bạn chưa đạt đủ KPI để qua màn!\nSố câu đúng: ${viewModel.totalRight.value}/20"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.resetAll()
                    }) { Text("Chơi lại") }
                }
            )
        }
    }
}

