package com.example.wordsearch.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wordsearch.ui.components.*
import com.example.wordsearch.ui.theme.*
import com.example.eduquizz.features.wordsearch.viewmodel.WordSearchViewModel
import com.example.eduquizz.R

/**
 * Main Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordSearchGame(
    viewModel: WordSearchViewModel = viewModel(),
    onBackToIntroduction: (() -> Unit)? = null
) {
    val coins by viewModel.coins
    val hintCell by viewModel.hintCell
    val grid by remember { mutableStateOf(viewModel.grid) }
    val wordsToFind by remember { mutableStateOf(viewModel.wordsToFind) }
    val selectedCells by remember { mutableStateOf(viewModel.selectedCells) }
    val selectedWord = viewModel.selectedWord
    val foundWordsCount = wordsToFind.count { it.isFound }
    val totalWords = wordsToFind.size
    val context = LocalContext.current

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
                            text = "Word Search",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        if (onBackToIntroduction != null) {
                            IconButton(onClick = onBackToIntroduction) {
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
                        IconButton(onClick = { viewModel.restartGame() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart Game"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
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
                            painter = painterResource(id = R.drawable.ic_coin),
                            contentDescription = "Coins",
                            modifier = Modifier.size(24.dp),
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
                            if (!viewModel.revealHint()) {
                                Toast.makeText(context, "Not enough coins!", Toast.LENGTH_SHORT)
                                    .show()
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
                        .padding(bottom = 16.dp)
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
                    onClick = { viewModel.restartGame() },
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

@Preview(showBackground = true)
@Composable
fun WordSearchGamePreview() {
    WordSearchGameTheme {
        WordSearchGame()
    }
}