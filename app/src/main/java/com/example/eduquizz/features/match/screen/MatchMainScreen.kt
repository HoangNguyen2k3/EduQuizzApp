package com.example.eduquizz.features.match.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.eduquizz.features.match.viewmodel.MatchGameViewModel
import com.example.eduquizz.navigation.Routes

data class MatchCard(
    val id: String,
    val type: String,
    val value: String,
    val displayText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchMainScreen(
    levelId: String,
    onBackPressed: () -> Unit,
    navController: NavHostController? = null,
    userName: String = "defaultUser",
    viewModel: MatchGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCompletionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(levelId) {
        viewModel.loadLevel(levelId)
    }

    LaunchedEffect(uiState.isGameComplete) {
        if (uiState.isGameComplete) {
            showCompletionDialog = false
            // Save progress when game is complete
            viewModel.saveProgress(userName, levelId) { correct, total ->
                // Navigate to result screen if navController is provided
                navController?.navigate(
                    "result/$correct/$total/${Routes.MATCH_GAME_LEVEL_SELECTION}/${Routes.MATCH_GAME_INTRO}"
                )
            }
        }
    }

    val allCards = remember(uiState.currentLevel) {
        uiState.currentLevel?.pairs?.flatMap { pair ->
            listOf(
                MatchCard(
                    id = "${pair.word}_word",
                    type = "word",
                    value = pair.word,
                    displayText = pair.word
                ),
                MatchCard(
                    id = "${pair.word}_def",
                    type = "definition",
                    value = pair.definition,
                    displayText = pair.definition
                )
            )
        }?.shuffled() ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.currentLevel?.title ?: "Loading...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text(
                                text = "Attempts: ${uiState.attempts}",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Time: ${uiState.timeRemaining}s",
                                fontSize = 14.sp,
                                color = if (uiState.timeRemaining <= 10) Color.Red else Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetGame() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadLevel(levelId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Text("Retry", color = Color(0xFF6366F1))
                            }
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allCards) { card ->
                            MatchCardItem(
                                card = card,
                                isSelected = uiState.selectedPairs.any {
                                    it.first == card.type && it.second == card.value
                                },
                                isMatched = uiState.matchedPairs.contains(card.value),
                                onClick = {
                                    if (!uiState.matchedPairs.contains(card.value)) {
                                        viewModel.selectCard(card.type, card.value)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showCompletionDialog) {
            CompletionDialog(
                attempts = uiState.attempts,
                onDismiss = { showCompletionDialog = false },
                onPlayAgain = {
                    showCompletionDialog = false
                    viewModel.resetGame()
                },
                onBackToLevels = onBackPressed
            )
        }
    }
}

@Composable
fun MatchCardItem(
    card: MatchCard,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isMatched -> Color(0xFF4CAF50)
        isSelected -> Color(0xFFFFA726)
        else -> Color.White
    }

    val textColor = when {
        isMatched || isSelected -> Color.White
        else -> Color.DarkGray
    }

    val borderColor = if (card.type == "word") {
        Color(0xFF6366F1)
    } else {
        Color(0xFF8B5CF6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(enabled = !isMatched, onClick = onClick)
            .then(
                if (!isMatched && !isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.displayText,
                fontSize = 14.sp,
                fontWeight = if (card.type == "word") FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun CompletionDialog(
    attempts: Int,
    onDismiss: () -> Unit,
    onPlayAgain: () -> Unit,
    onBackToLevels: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Congratulations!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "You completed the level!",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total Attempts: $attempts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6366F1)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Text("Play Again")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onBackToLevels) {
                Text("Back to Levels", color = Color(0xFF6366F1))
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}