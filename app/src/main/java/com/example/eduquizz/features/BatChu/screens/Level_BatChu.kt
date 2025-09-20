package com.example.eduquizz.features.BatChu.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.R
import com.example.eduquizz.data.models.Game
import com.example.eduquizz.features.BatChu.repository.BatChuLevel
import com.example.eduquizz.features.BatChu.viewmodel.ViewModelBatChu
import com.example.quizapp.ui.theme.QuizAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LevelChoiceBatChu(
    onBackClick: () -> Unit = {},
    onGameClick: (Game) -> Unit = {},
    viewModel: ViewModelBatChu = hiltViewModel()
) {
    // System UI Controller for status bar
    val systemUiController = rememberSystemUiController()
    var showRipple by remember { mutableStateOf(false) }
    var pendingGameClick by remember { mutableStateOf<Game?>(null) }

    // State to hold levels from repository
    var levels by remember { mutableStateOf<List<BatChuLevel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Load levels from repository through ViewModel
    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color(0xFF5A4FCF),
            darkIcons = false
        )

        // Use coroutineScope instead of viewModelScope
        coroutineScope.launch {
            try {
                // Call through ViewModel to get levels
                val result = viewModel.getAllLevels()
                result.onSuccess { fetchedLevels ->
                    levels = fetchedLevels
                    isLoading = false
                }.onFailure { error ->
                    errorMessage = error.message
                    isLoading = false
                    // Fallback to default levels if API call fails
                    levels = getDefaultLevels()
                }
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
                levels = getDefaultLevels()
            }
        }
    }

    // Handle ripple effect
    LaunchedEffect(showRipple) {
        if (showRipple) {
            delay(500)
            pendingGameClick?.let { game ->
                onGameClick(game)
            }
            showRipple = false
        }
    }

    // Map BatChuLevel to Game for UI
    val games = levels.map { level ->
        Game(
            id = level.levelId,
            name = level.title,
            iconRes = R.drawable.eng, // Replace with appropriate icon if needed
            progress = 0, // You can fetch progress from repository if needed
            totalQuestions = level.questionCount, // Use actual questionCount from database
            completedQuestions = 0, // Fetch from repository if needed
            totalLessons = 1, // Adjust based on your data model
            gradientColors = when (level.difficulty) {
                "Easy" -> listOf(Color(0xFF4A85F5), Color(0xFF7B61FF), Color(0xFF7B61FF))
                "Normal" -> listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
                "Hard" -> listOf(Color(0xFFFF6B9D), Color(0xFFFF8E9E), Color(0xFFFFB4A2))
                else -> listOf(Color.Gray, Color.LightGray)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF667EEA), // Blue Purple
                        Color(0xFF764BA2)  // Purple
                    )
                )
            )
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lỗi tải dữ liệu: $errorMessage",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.getAllLevels()
                                result.onSuccess { fetchedLevels ->
                                    levels = fetchedLevels
                                    isLoading = false
                                }.onFailure { error ->
                                    errorMessage = error.message
                                    isLoading = false
                                    levels = getDefaultLevels()
                                }
                            }
                        }
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header with modern gradient background
                EnglishGamesHeader(
                    onBackClick = onBackClick
                )

                // Games list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(vertical = 20.dp)
                ) {
                    items(games) { game ->
                        EnhancedGameCard(
                            game = game,
                            onClick = {
                                showRipple = true
                                pendingGameClick = game
                            }
                        )
                    }
                }
            }

            if (showRipple) {
                FullscreenRippleEffect()
            }
        }
    }
}

@Composable
private fun FullscreenRippleEffect() {
    val radius = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        radius.animateTo(
            targetValue = 2000f,
            animationSpec = tween(durationMillis = 500)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF4A85F5).copy(alpha = 0.6f),
                radius = radius.value,
                center = center
            )
        }
    }
}

@Composable
private fun EnglishGamesHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF667EEA), // Blue Purple
                        Color(0xFF764BA2)  // Purple
                    )
                )
            )
            .padding(top = 48.dp, bottom = 24.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Bat Chu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Chọn trò chơi yêu thích",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EnhancedGameCard(
    game: Game,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = game.gradientColors,
                        startX = 0f,
                        endX = 1200f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 400f
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.name.first().toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = game.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hiển thị số câu hỏi từ database
                    Text(
                        text = "${game.totalQuestions} câu hỏi • ${game.totalLessons} bài học",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Tiến độ: ${game.progress}/${game.totalQuestions}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Hoàn thành: ${game.completedQuestions}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "→",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Helper function for default levels
private fun getDefaultLevels(): List<BatChuLevel> {
    return listOf(
        BatChuLevel(
            levelId = "LevelEasy",
            title = "Easy Level",
            difficulty = "Easy",
            questionCount = 0, // Sẽ được cập nhật từ database
            questions = emptyList()
        ),
        BatChuLevel(
            levelId = "LevelNormal",
            title = "Normal Level",
            difficulty = "Normal",
            questionCount = 0, // Sẽ được cập nhật từ database
            questions = emptyList()
        ),
        BatChuLevel(
            levelId = "LevelHard",
            title = "Hard Level",
            difficulty = "Hard",
            questionCount = 0, // Sẽ được cập nhật từ database
            questions = emptyList()
        )
    )
}

@Preview(showBackground = true)
@Composable
fun EnglishGamesScreenPreview() {
    QuizAppTheme {
        LevelChoiceBatChu()
    }
}