package com.example.eduquizz.features.mapping.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.mapping.viewmodel.MappingViewModel
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay

data class LevelData(
    val id: String,
    val title: String,
    val difficulty: String,
    val questionCount: Int,
    val isCompleted: Boolean = false,
    val difficultyColor: Color,
    val description: String
)

@Composable
private fun LevelCard(
    level: LevelData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable {
                println("Level selected: ${level.id}") // Debug log
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Difficulty color accent
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(level.difficultyColor)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = level.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = Color(0xFF2E7D32)
                        )

                        Text(
                            text = level.difficulty.uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = level.difficultyColor
                        )
                    }

                    // Completion status
                    if (level.isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Completed",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = "Not completed",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = level.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${level.questionCount} Questions",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF666666)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = level.difficultyColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "START",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            ),
                            color = level.difficultyColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// In your main composable, make sure you're loading actual levels:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingLevelSelectionScreen(
    onLevelSelected: (String) -> Unit,
    onBackPressed: () -> Unit,
    mappingViewModel: MappingViewModel = hiltViewModel()
) {
    var isVisible by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    // Load actual levels from the database
    val availableLevels by mappingViewModel.availableLevels.collectAsState()

    LaunchedEffect(Unit) {
        mappingViewModel.loadAllLevels() // Load from database
        delay(300)
        isDataLoaded = true
        delay(200)
        isVisible = true
    }

    // Convert database levels to UI levels, or use fallback
    val levels = remember(availableLevels) {
        if (availableLevels.isNotEmpty()) {
            availableLevels.map { level ->
                LevelData(
                    id = level.levelId,
                    title = level.title,
                    difficulty = level.difficulty,
                    questionCount = level.questionCount,
                    isCompleted = false, // You can load this from user progress
                    difficultyColor = when (level.difficulty.uppercase()) {
                        "EASY" -> Color(0xFF4CAF50)
                        "NORMAL", "MEDIUM" -> Color(0xFFFF9800)
                        "HARD" -> Color(0xFFE91E63)
                        else -> Color(0xFFFF9800)
                    },
                    description = "Database level with ${level.locations.size} locations"
                )
            }
        } else {
            // Fallback levels
            listOf(
                LevelData(
                    id = "LevelEasy",
                    title = "Easy Level",
                    difficulty = "Easy",
                    questionCount = 10,
                    isCompleted = false,
                    difficultyColor = Color(0xFF4CAF50),
                    description = "Perfect for beginners. Using sample data."
                ),
                LevelData(
                    id = "LevelNormal",
                    title = "Normal Level",
                    difficulty = "Normal",
                    questionCount = 10,
                    isCompleted = false,
                    difficultyColor = Color(0xFFFF9800),
                    description = "Moderate challenge. Using sample data."
                ),
                LevelData(
                    id = "LevelHard",
                    title = "Hard Level",
                    difficulty = "Hard",
                    questionCount = 10,
                    isCompleted = false,
                    difficultyColor = Color(0xFFE91E63),
                    description = "Expert level. Using sample data."
                )
            )
        }
    }

    // Rest of your UI...
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF66BB6A),
                        Color(0xFFE8F5E8)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(800, delayMillis = 200))
                        ) {
                            Text(
                                text = "Select Level",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Debug info
                    if (availableLevels.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "No levels loaded from database. Using fallback data.",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(800, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(800))
                    ) {
                        Text(
                            text = "Choose Your Challenge",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(800, delayMillis = 100, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 100))
                    ) {
                        Text(
                            text = "Select a difficulty level to begin your geography adventure",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                items(levels.size) { index ->
                    val level = levels[index]
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(
                                800,
                                delayMillis = 200 + (index * 150),
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 200 + (index * 150)))
                    ) {
                        LevelCard(
                            level = level,
                            onClick = { onLevelSelected(level.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MappingLevelSelectionScreenPreview() {
    QuizAppTheme {
        MappingLevelSelectionScreen(
            onLevelSelected = { levelId -> println("Selected level: $levelId") },
            onBackPressed = { }
        )
    }
}