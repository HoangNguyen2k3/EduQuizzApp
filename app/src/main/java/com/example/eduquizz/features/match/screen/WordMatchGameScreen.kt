package com.example.eduquizz.features.match.screen

import android.widget.Toast
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.navigation.Routes
import kotlinx.coroutines.delay
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.DisposableEffect

@Composable
fun WordMatchGameScreen(
    viewModel: WordMatchGame = hiltViewModel(),
    navController: NavHostController,
    dataviewModel: DataViewModel = hiltViewModel()
) {
    val gold by dataviewModel.gold.observeAsState(-1)
    val timer by viewModel.timerSeconds
    val level by viewModel.currentLevel
    val showResult by viewModel.showResult
    val showBuyGoldDialog by viewModel.showBuyGoldDialog
    val showFinishDialog by viewModel.showFinishDialog
    val showTimeOutDialog by viewModel.showTimeOutDialog

    val cards = viewModel.cards
    val selectedIndices = viewModel.selectedIndices
    val shakingIndices = viewModel.shakingIndices
    val correctIndices = viewModel.correctIndices
    val wrongIndices = viewModel.wrongIndices



    LocalContext.current
    LaunchedEffect(Unit) {
        AudioManager.setBgmEnabled(true)
    }
    DisposableEffect(Unit) {
        onDispose {
            AudioManager.setBgmEnabled(false)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.Init(dataviewModel)
    }
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
        }

        // Timer Progress Bar
        val progress = timer.toFloat() / 40f
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

        // Lưới thẻ
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
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

                val backgroundColor = when {
                    isCorrect -> Color(0xFF4CAF50)
                    isWrong -> Color(0xFFF44336)
                    isSelected -> Color(0xFF7E7E7E)
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
                        .graphicsLayer(
                            translationX = if (isShaking) shakeAnim.value * 16f else 0f,
                            alpha = if (card.isMatched) 0f else 1f
                        )
                        .background(backgroundColor)

                        .clickable(enabled = !showResult && !isSelected && !isCorrect && !isWrong && !card.isMatched) {
                            AudioManager.playClickSfx()
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

        // Hint/Skip buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    AudioManager.playClickSfx()
                    viewModel.useHint()
                },
                enabled = gold >= 20 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0A9F8)),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Hint (-20)", color = Color.White)
            }
            Button(
                onClick = {
                    AudioManager.playClickSfx()
                    viewModel.skipLevel()
                },
                enabled = gold >= 100 && !showResult,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7C873)),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Skip (-100)", color = Color.White)
            }
        }

        // Dialogs
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

        if (showTimeOutDialog) {
            val context = LocalContext.current
            Toast.makeText(context, "Hết thời gian!", Toast.LENGTH_SHORT).show()
            navController.navigate("result/${viewModel.totalRight.value}/${viewModel.totalQuestion.value}/${Routes.INTRO_THONG}/${Routes.INTRO_THONG}")
        }

        if (showFinishDialog) {
            val context = LocalContext.current
            Toast.makeText(context, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
            navController.navigate("result/${viewModel.totalRight.value}/${viewModel.totalQuestion.value}/${Routes.INTRO_THONG}/${Routes.INTRO_THONG}")
        }
    }
}