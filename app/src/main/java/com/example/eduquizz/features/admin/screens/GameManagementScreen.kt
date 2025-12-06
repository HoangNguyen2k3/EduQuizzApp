package com.example.eduquizz.features.admin.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.admin.data.GameLevel
import com.example.eduquizz.features.admin.viewmodel.AdminViewModel
import com.example.eduquizz.features.admin.viewmodel.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameManagementScreen(
    username: String,
    gameType: GameType,
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val levels by viewModel.selectedGameLevels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(gameType) {
        viewModel.loadGameLevels(username, gameType)
    }

    val gameName = when (gameType) {
        GameType.WORD_SEARCH -> "Word Search"
        GameType.BAT_CHU -> "Bat Chu"
        GameType.MATCH_GAME -> "Match Game"
        GameType.QUIZ -> "Quiz Game"
        GameType.SCENE -> "Scene Game"
        GameType.SOUND -> "Sound Game"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Manage $gameName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${levels.size} levels",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Add new level */ }) {
                        Icon(Icons.Default.Add, "Add Level", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667EEA)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Add new level */ },
                containerColor = Color(0xFF667EEA)
            ) {
                Icon(Icons.Default.Add, "Add Level", tint = Color.White)
            }
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    ErrorMessage(
                        message = errorMessage!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }
                levels.isEmpty() -> {
                    EmptyStateContent(gameName)
                }
                else -> {
                    LevelsList(
                        levels = levels,
                        onEditLevel = { level ->
                            // TODO: Navigate to edit screen
                        },
                        onDeleteLevel = { level ->
                            // TODO: Delete level
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelsList(
    levels: List<GameLevel>,
    onEditLevel: (GameLevel) -> Unit,
    onDeleteLevel: (GameLevel) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(levels) { level ->
            LevelCard(
                level = level,
                onEdit = { onEditLevel(level) },
                onDelete = { onDeleteLevel(level) }
            )
        }
    }
}

@Composable
private fun LevelCard(
    level: GameLevel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = level.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DifficultyChip(level.difficulty)
                        Text(
                            text = "${level.questionCount} questions",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                }
            }

            if (level.createdAt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Created: ${level.createdAt}",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Level") },
            text = { Text("Are you sure you want to delete '${level.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF6B6B)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DifficultyChip(difficulty: String) {
    val color = when (difficulty.lowercase()) {
        "easy" -> Color(0xFF4CAF50)
        "medium", "normal" -> Color(0xFFFFB74D)
        "hard" -> Color(0xFFFF6B6B)
        else -> Color(0xFF999999)
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = difficulty,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun EmptyStateContent(gameName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = "Empty",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFCCCCCC)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Levels Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first $gameName level",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    ) {
        Text(message)
    }
}