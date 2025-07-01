package com.example.eduquizz.features.home.english

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
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.eduquizz.R
import com.example.quizapp.ui.theme.QuizAppTheme
import com.example.eduquizz.data.models.Game
import kotlinx.coroutines.delay

@Composable
fun EnglishGamesScreen(
    onBackClick: () -> Unit = {},
    onGameClick: (Game) -> Unit = {}
) {

    // System UI Controller for status bar
    val systemUiController = rememberSystemUiController()

    var showRipple by remember { mutableStateOf(false) }
    var pendingGameClick by remember { mutableStateOf<Game?>(null) }

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = Color(0xFF5A4FCF),
            darkIcons = false
        )
    }

    LaunchedEffect(showRipple) {
        if (showRipple) {
            delay(500)
            pendingGameClick?.let { game ->
                onGameClick(game)
            }
            showRipple = false
        }
    }


    val games = listOf(
        Game(
            id = "word_find",
            name = "Word Search",
            iconRes = R.drawable.eng,
            progress = 8,
            totalQuestions = 550,
            completedQuestions = 8,
            totalLessons = 11,
            gradientColors = listOf(
                Color(0xFF4A85F5),
                Color(0xFF7B61FF),
                Color(0xFF7B61FF)
            )
        ),
        Game(
            id = "connect_blocks",
            name = "Match Game",
            iconRes = R.drawable.eng,
            progress = 3,
            totalQuestions = 400,
            completedQuestions = 3,
            totalLessons = 8,
            gradientColors = listOf(
                Color(0xFFFF5722),
                Color(0xFFFF9800),
                Color(0xFFFFC107)
            )
        ),
        Game(
            id = "quiz",
            name = "Quiz",
            iconRes = R.drawable.eng,
            progress = 23,
            totalQuestions = 875,
            completedQuestions = 23,
            totalLessons = 12,
            gradientColors = listOf(
                Color(0xFF4ECDC4), // Turquoise
                Color(0xFF44A08D), // Teal
                Color(0xFF096A5A)  // Dark Teal
            )
        ),
        Game(
            id = "bubble_shot",
            name = "Bubble Shot",
            iconRes = R.drawable.eng,
            progress = 20,
            totalQuestions = 100,
            completedQuestions = 23,
            totalLessons = 56,
            gradientColors = listOf(
                Color(0xFF00C9FF), // Bright Blue
                Color(0xFF92FE9D)  // Light Green
            )
        ),
        Game(
            id = "vocabulary",
            name = "Từ vựng",
            iconRes = R.drawable.eng,
            progress = 15,
            totalQuestions = 620,
            completedQuestions = 15,
            totalLessons = 10,
            gradientColors = listOf(
                Color(0xFFFF6B9D), // Vibrant Pink
                Color(0xFFFF8E9E), // Light Pink
                Color(0xFFFFB4A2)  // Peach
            )
        )
    )
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
                        onClick = { onGameClick(game) }
                    )
                }
            }
        }

        if (showRipple) {
            FullscreenRippleEffect()
        }
    }
}

@Composable
private fun FullscreenRippleEffect(){
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
        // Back button with modern styling
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
                text = "Tiếng Anh",
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
            // Add subtle overlay pattern
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
                // Game icon with modern background
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // You can replace this with actual game icon
                    Text(
                        text = game.name.first().toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Game info
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

                    Text(
                        text = "${game.totalQuestions} câu hỏi • ${game.totalLessons} bài học",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Progress indicator
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

                        // Completion indicator
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

                // Arrow indicator
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

@Preview(showBackground = true)
@Composable
fun EnglishGamesScreenPreview() {
    QuizAppTheme {
        EnglishGamesScreen()
    }
}