package com.example.eduquizz.features.wordsearch.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.wordsearch.ui.components.*
import com.example.wordsearch.ui.theme.*
import com.example.eduquizz.features.wordsearch.viewmodel.WordSearchViewModel
import com.example.eduquizz.R
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.DisposableEffect
import com.example.eduquizz.data.local.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordSearchGame(
    topicId: String? = null,
    viewModel: WordSearchViewModel = hiltViewModel(),
    onBackToIntroduction: (() -> Unit)? = null,
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val coins by viewModel.coins
    val hintCell by viewModel.hintCell
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val currentTopic by viewModel.currentTopic
    val grid by remember { mutableStateOf(viewModel.grid) }
    val wordsToFind by remember { mutableStateOf(viewModel.wordsToFind) }
    val selectedCells by remember { mutableStateOf(viewModel.selectedCells) }
    val selectedWord = viewModel.selectedWord
    val foundWordsCount = wordsToFind.count { it.isFound }
    val totalWords = wordsToFind.size
    val isGameCompleted by viewModel.isGameCompleted
    val timeSpent by viewModel.timeSpent
    val context = LocalContext.current


    val userName by userViewModel.userName.collectAsState()
    LaunchedEffect(userName) {
        userName?.let { viewModel.setUserName(it) }
    }

    LaunchedEffect(topicId) {
        if (topicId != null) {
            viewModel.loadWordsFromFirebase(topicId)
        } else {
            viewModel.restartGame()
        }
    }

    LaunchedEffect(Unit) {
        AudioManager.setBgmEnabled(true)
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioManager.setBgmEnabled(false)
        }
    }

    LaunchedEffect(error) {
        error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(isGameCompleted) {
        if (isGameCompleted && currentTopic != null) {
            val coinsEarned = 50 // Có thể tính toán dựa trên hiệu suất
            navController.navigate("completion/$currentTopic/$totalWords/$timeSpent/$coinsEarned")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (topicId != null) "Word Search - ${topicId.replaceFirstChar { it.uppercase() }}" else "Word Search",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (onBackToIntroduction != null) {
                            IconButton(onClick = {
                                AudioManager.playClickSfx()
                                onBackToIntroduction()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to Introduction"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = {
                            AudioManager.playClickSfx()
                            viewModel.restartGame()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Game"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading words...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (wordsToFind.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No words available",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                AudioManager.playClickSfx()
                                if (topicId != null) {
                                    viewModel.loadWordsFromFirebase(topicId)
                                } else {
                                    viewModel.restartGame()
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.coinimg),
                                contentDescription = "Coins",
                                modifier = Modifier.size(30.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$coins",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        Button(
                            onClick = {
                                AudioManager.playClickSfx()
                                if (!viewModel.revealHint()) {
                                    Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_hint),
                                    contentDescription = "Hint",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hint")
                            }
                        }
                    }

                    GameProgressBar(
                        foundWords = foundWordsCount,
                        totalWords = totalWords,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GridBackground
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            ModernWordGrid(
                                grid = grid,
                                selectedCells = selectedCells,
                                hintCell = hintCell,
                                onCellSelected = { viewModel.onCellSelected(it) }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = selectedWord.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)),
                        exit = fadeOut(animationSpec = tween(200, easing = FastOutSlowInEasing))
                    ) {
                        ModernSelectedWordDisplay(
                            selectedWord = selectedWord,
                            onClearSelection = { viewModel.resetSelection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardBackground
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        ModernWordsToFindList(
                            wordsToFind = wordsToFind,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    Button(
                        onClick = {
                            AudioManager.playClickSfx()
                            viewModel.restartGame()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Game",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameProgressBar(
    foundWords: Int,
    totalWords: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$foundWords/$totalWords",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = if (totalWords > 0) foundWords.toFloat() / totalWords else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = ButtonPrimary,
            trackColor = GridStroke
        )
    }
}