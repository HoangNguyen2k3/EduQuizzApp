package com.example.quizapp.ui.english

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.quizapp.ui.theme.QuizAppTheme
import com.example.quizapp.ui.components.GameCard
import com.example.quizapp.data.Game

@Composable
fun EnglishGamesScreen(
    onBackClick: () -> Unit = {},
    onGameClick: (Game) -> Unit = {}
) {
    val games = listOf(
        Game(
            id = "word_find",
            name = "Tìm từ",
            iconRes = R.drawable.eng,
            progress = 8,
            totalQuestions = 550,
            completedQuestions = 8,
            totalLessons = 11,
            gradientColors = listOf(Color(0xFFFF69B4), Color(0xFFDA70D6))
        ),
        Game(
            id = "connect_blocks",
            name = "Nối ô",
            iconRes = R.drawable.eng,
            progress = 3,
            totalQuestions = 400,
            completedQuestions = 3,
            totalLessons = 8,
            gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFF8C00))
        ),
        Game(
            id = "quiz",
            name = "Quiz",
            iconRes = R.drawable.eng,
            progress = 23,
            totalQuestions = 875,
            completedQuestions = 23,
            totalLessons = 12,
            gradientColors = listOf(Color(0xFF00CED1), Color(0xFF20B2AA))
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD6EFFF))
    ) {
        // Header with back button
        EnglishGamesHeader(
            onBackClick = onBackClick
        )

        // Games list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(games) { game ->
                GameCard(
                    game = game,
                    onClick = { onGameClick(game) }
                )
            }
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color(0xFF1890FF)
            )
        }
        Text(
            text = "Tiếng Anh",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1890FF),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EnglishGamesScreenPreview() {
    QuizAppTheme {
        EnglishGamesScreen()
    }
}