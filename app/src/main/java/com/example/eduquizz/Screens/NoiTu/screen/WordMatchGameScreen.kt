
package com.example.quizapp.screen

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import com.example.quizapp.viewmodel.WordMatchGame
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.sp
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.navigation.NavHostController

@Composable
fun WordMatchGameScreen(viewModel: WordMatchGame, navController: NavHostController) {
    val gold by viewModel.gold
    val timer by viewModel.timerSeconds
    val level by viewModel.currentLevel
    val showResult by viewModel.showResult
    val showBuyGoldDialog by viewModel.showBuyGoldDialog
    val showFinishDialog by viewModel.showFinishDialog
    val canPass by viewModel.canPass

    val wordPairs = viewModel.currentWordPairs
    val definitions = viewModel.currentDefinitions
    val connections = viewModel.connections
    val selectedWordIndex = viewModel.selectedWordIndex

    val wordPositions = remember { mutableStateListOf<Offset>() }
    val defPositions = remember { mutableStateListOf<Offset>() }

    Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = {
                    println("Đa nhan nut Back")
                    navController.popBackStack()
                },
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
        // Khu vực nối từ
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            DrawConnections(
                connections = connections,
                wordPositions = wordPositions,
                defPositions = defPositions,
                showResult = showResult,
                wordPairs = wordPairs,
                definitions = definitions,
                boxLeftWidth = 140.dp,
                boxHeight = 60.dp
            )
            Row(
                modifier = Modifier
                    .width(140.dp + 180.dp + 44.dp) // left + right + gap
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Word column
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    wordPairs.forEachIndexed { index, pair ->
                        val isCorrect = showResult && connections.find { it.wordIndex == index }?.let {
                            wordPairs[it.wordIndex].definition == definitions[it.definitionIndex]
                        } == true
                        val isIncorrect = showResult && connections.any { it.wordIndex == index } && !isCorrect

                        Surface(
                            modifier = Modifier
                                .width(140.dp)
                                .height(60.dp)
                                .onGloballyPositioned { coordinates ->
                                    val pos = coordinates.positionInRoot()
                                    if (wordPositions.size > index) wordPositions[index] = pos else wordPositions.add(pos)
                                }
                                .clickable(enabled = !showResult) { viewModel.selectWord(index) },
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp,
                            color = when {
                                isCorrect -> Color(0xFF7DE699)
                                isIncorrect -> Color(0xFFEB5353)
                                selectedWordIndex == index -> Color(0xFFFFF8B3)
                                else -> Color.White
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(pair.word, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                // Definition column
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    definitions.forEachIndexed { index, definition ->
                        val isCorrect = showResult && connections.find { it.definitionIndex == index }?.let {
                            wordPairs[it.wordIndex].definition == definitions[it.definitionIndex]
                        } == true
                        val isIncorrect = showResult && connections.any { it.definitionIndex == index } && !isCorrect

                        Surface(
                            modifier = Modifier
                                .width(180.dp)
                                .height(60.dp)
                                .onGloballyPositioned { coordinates ->
                                    val pos = coordinates.positionInRoot()
                                    if (defPositions.size > index) defPositions[index] = pos else defPositions.add(pos)
                                }
                                .clickable(enabled = !showResult) {
                                    viewModel.connectToDefinition(index)
                                },
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp,
                            color = when {
                                isCorrect -> Color(0xFF7DE699)
                                isIncorrect -> Color(0xFFEB5353)
                                else -> Color(0xFFE3F1FF)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(definition, fontSize = 15.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
        // Next/Check/Hint/Skip
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
                onClick = { if (showResult) viewModel.nextLevel() else viewModel.checkResultAndReward() },
                enabled = connections.size == 5,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
            ) {
                Text(if (showResult) "Next" else "Check", color = Color.White)
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

