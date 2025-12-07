package com.example.eduquizz.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.admin.data.AdminUiState
import com.example.eduquizz.features.admin.viewmodel.AdminViewModel
import com.example.eduquizz.features.admin.viewmodel.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    username: String,
    onBackClick: () -> Unit,
    onGameManagementClick: (GameType) -> Unit,
    onContestManagementClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},  // NEW: Logout callback
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(username) {
        viewModel.checkAdminStatus(username)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Admin Panel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Welcome, $username",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667EEA)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF5F5F5),
                            Color(0xFFE8E8E8)
                        )
                    )
                )
        ) {
            when (uiState) {
                is AdminUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AdminUiState.AccessDenied -> {
                    AccessDeniedContent()
                }
                is AdminUiState.Success -> {
                    AdminDashboardContent(
                        stats = dashboardStats,
                        onGameManagementClick = onGameManagementClick,
                        onContestManagementClick = onContestManagementClick,
                        onLogoutClick = onLogoutClick  // NEW: Pass logout callback
                    )
                }
                is AdminUiState.Error -> {
                    ErrorContent(
                        message = (uiState as AdminUiState.Error).message,
                        onRetry = { viewModel.loadDashboardStats(username) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    stats: com.example.eduquizz.features.admin.data.AdminDashboardStats,
    onGameManagementClick: (GameType) -> Unit,
    onContestManagementClick: () -> Unit,
    onLogoutClick: () -> Unit  // NEW: Logout callback
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Dashboard Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Games",
                        value = (stats.wordSearchTopics + stats.batChuLevels +
                                stats.matchLevels + stats.quizLevels +
                                stats.sceneLevels + stats.soundLevels).toString(),
                        icon = Icons.Default.Games,
                        color = Color(0xFF667EEA),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Questions",
                        value = stats.totalQuestions.toString(),
                        icon = Icons.Default.QuestionAnswer,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Total Contests",
                        value = stats.totalContests.toString(),
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Now",
                        value = stats.activeContests.toString(),
                        icon = Icons.Default.Speed,
                        color = Color(0xFFFF6B9D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Contest Management Section
        item {
            Text(
                "Contest Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        item {
            ContestManagementCard(
                totalContests = stats.totalContests,
                activeContests = stats.activeContests,
                onClick = onContestManagementClick
            )
        }

        item {
            Text(
                "Game Management",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // Game Management Cards
        items(
            listOf(
                Triple(GameType.WORD_SEARCH, "Word Search", stats.wordSearchTopics),
                Triple(GameType.BAT_CHU, "Bat Chu", stats.batChuLevels),
                Triple(GameType.MATCH_GAME, "Match Game", stats.matchLevels),
                Triple(GameType.QUIZ, "Quiz Game", stats.quizLevels),
                Triple(GameType.SCENE, "Scene Game", stats.sceneLevels),
                Triple(GameType.SOUND, "Sound Game", stats.soundLevels)
            )
        ) { (gameType, name, count) ->
            GameManagementCard(
                gameName = name,
                levelCount = count,
                onClick = { onGameManagementClick(gameType) }
            )
        }

        // NEW: Logout Button at the bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF6B6B)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Color(0xFFFF6B6B)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContestManagementCard(
    totalContests: Int,
    activeContests: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFB74D).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Contest",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFFB74D)
                )
                Column {
                    Text(
                        text = "Online Contests",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "$totalContests total",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                        if (activeContests > 0) {
                            Text(
                                text = "• $activeContests active",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Manage",
                tint = Color(0xFFFFB74D)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GameManagementCard(
    gameName: String,
    levelCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = gameName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$levelCount levels",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Manage",
                tint = Color(0xFF667EEA)
            )
        }
    }
}

@Composable
private fun AccessDeniedContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Access Denied",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFF6B6B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Access Denied",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You don't have admin privileges",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFFF6B6B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667EEA)
            )
        ) {
            Text("Retry")
        }
    }
}