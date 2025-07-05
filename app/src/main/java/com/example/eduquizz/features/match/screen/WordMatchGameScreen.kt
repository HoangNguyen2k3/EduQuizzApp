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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay

@Composable
fun WordMatchGameScreen(viewModel: WordMatchGame, navController: NavHostController) {
    val gold by viewModel.gold
    val timer by viewModel.timerSeconds
    val level by viewModel.currentLevel
    val showResult by viewModel.showResult
    val showBuyGoldDialog by viewModel.showBuyGoldDialog
    val showFinishDialog by viewModel.showFinishDialog
    val showTimeOutDialog by viewModel.showTimeOutDialog
    val canPass by viewModel.canPass
    

    val cards = viewModel.cards
    val selectedIndices = viewModel.selectedIndices
    val shakingIndices = viewModel.shakingIndices
    val correctIndices = viewModel.correctIndices
    val wrongIndices = viewModel.wrongIndices

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
            // Ẩn timer số, chỉ hiển thị thanh tiến trình
        }
        
        // Timer Progress Bar - thay thế timer số
        val progress = timer.toFloat() / 40f
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Hiển thị thời gian còn lại
//            Text(
//                text = "⏰ $timer",
//                color = if (timer <= 10) Color.Red else Color.Black,
//                fontWeight = FontWeight.Bold,
//                fontSize = 16.sp,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )
            Spacer(modifier = Modifier.height(4.dp))
            // Thanh tiến trình
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
                val isSelected = selectedIndices.contains(idx)
                val isShaking = shakingIndices.contains(idx)
                val isCorrect = correctIndices.contains(idx)
                val isWrong = wrongIndices.contains(idx)
                val shakeAnim = remember { Animatable(0f) }
                
                // Xác định màu nền dựa trên trạng thái
                val backgroundColor = when {
                    isCorrect -> Color(0xFF4CAF50) // Xanh lá
                    isWrong -> Color(0xFFF44336) // Đỏ
                    isSelected -> Color(0xFF7E7E7E) // Vàng nhạt
                    else -> Color.White
                }
                
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
                            translationX = if (isShaking) shakeAnim.value * 16f else 0f,
                            alpha = if (card.isMatched) 0f else 1f
                        )
                        .background(backgroundColor)
                        .clickable(enabled = !showResult && !isSelected && !isCorrect && !isWrong && !card.isMatched) {
                            viewModel.onCardClick(idx)
                        },
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            card.text, 
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold, 
                            textAlign = TextAlign.Center,
                            color = if (isCorrect || isWrong) Color.White else Color.Black
                        )
                    }
                }
            }
        }
        // Hint/Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.useHint() },
                enabled = gold >= 20 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0A9F8)),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Hint (-20)", color = Color.White)
            }
            Button(
                onClick = { viewModel.skipLevel() },
                enabled = gold >= 100 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7C873)),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Skip (-100)", color = Color.White)
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
        // Dialog hết thời gian
        if (showTimeOutDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { 
                    Text(
                        "⏰ Hết thời gian!",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    ) 
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Bạn đã hết thời gian cho level này!",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Số cặp đúng: ${viewModel.totalRight.value}/20",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                        Text(
                            "Game sẽ reset về level đầu sau 5 giây...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.showTimeOutDialog.value = false
                            viewModel.resetAll()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) { 
                        Text("Reset ngay", color = Color.White) 
                    }
                }
            )
        }
        // Dialog kết thúc game
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { 
                    Text(
                        if (canPass) "🎉 Hoàn thành!" else "😔 Chưa đạt yêu cầu",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (canPass) "Chúc mừng! Bạn đã hoàn thành game!"
                            else "Bạn cần cải thiện thêm để qua màn!",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            "Số cặp đúng: ${viewModel.totalRight.value}/20",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canPass) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                        Text(
                            "Vàng kiếm được: ${viewModel.totalRight.value * 5}",
                            fontSize = 14.sp,
                            color = Color(0xFFFFB800),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetAll() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canPass) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    ) { 
                        Text("Chơi lại", color = Color.White) 
                    }
                }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun WordMatchGameScreenPreview() {
    val viewModel = WordMatchGame()
    val navController = NavHostController(LocalContext.current)
    WordMatchGameScreen(viewModel, navController)
}

