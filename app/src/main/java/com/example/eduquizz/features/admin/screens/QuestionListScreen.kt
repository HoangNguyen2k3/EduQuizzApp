package com.example.eduquizz.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.admin.data.QuestionFilter
import com.example.eduquizz.features.admin.data.QuestionItem
import com.example.eduquizz.features.admin.viewmodel.AdminViewModel
import com.example.eduquizz.features.admin.viewmodel.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionListScreen(
    username: String,
    gameType: GameType,
    onBackClick: () -> Unit,
    onQuestionClick: (String) -> Unit,
    onAddQuestionClick: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val questions by viewModel.questions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val gameName = when (gameType) {
        GameType.WORD_SEARCH -> "Word Search"
        GameType.BAT_CHU -> "Bat Chu"
        GameType.MATCH_GAME -> "Match Game"
        GameType.QUIZ -> "Quiz Game"
        GameType.SCENE -> "Scene Game"
        GameType.SOUND -> "Sound Game"
    }

    LaunchedEffect(gameType, searchQuery, selectedDifficulty, selectedCategory) {
        val filter = QuestionFilter(
            gameType = gameType.name,
            difficulty = selectedDifficulty,
            category = selectedCategory,
            searchQuery = searchQuery.takeIf { it.isNotBlank() }
        )
        viewModel.loadQuestions(username, filter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "$gameName Questions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${questions.size} questions",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, "Filter", tint = Color.White)
                    }
                    IconButton(onClick = onAddQuestionClick) {
                        Icon(Icons.Default.Add, "Add Question", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF667EEA)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddQuestionClick,
                containerColor = Color(0xFF667EEA)
            ) {
                Icon(Icons.Default.Add, "Add Question", tint = Color.White)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearClick = { searchQuery = "" }
                )

                // Filter Chips
                if (selectedDifficulty != null || selectedCategory != null) {
                    FilterChipsRow(
                        selectedDifficulty = selectedDifficulty,
                        selectedCategory = selectedCategory,
                        onRemoveDifficulty = { selectedDifficulty = null },
                        onRemoveCategory = { selectedCategory = null }
                    )
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null -> {
                        ErrorContent(
                            message = errorMessage!!,
                            onRetry = {
                                viewModel.clearError()
                                val filter = QuestionFilter(
                                    gameType = gameType.name,
                                    difficulty = selectedDifficulty,
                                    category = selectedCategory,
                                    searchQuery = searchQuery.takeIf { it.isNotBlank() }
                                )
                                viewModel.loadQuestions(username, filter)
                            }
                        )
                    }
                    questions.isEmpty() -> {
                        EmptyQuestionsContent(onAddClick = onAddQuestionClick)
                    }
                    else -> {
                        QuestionsList(
                            questions = questions,
                            onQuestionClick = onQuestionClick,
                            onDeleteClick = { questionId ->
                                viewModel.deleteQuestion(username, questionId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            currentDifficulty = selectedDifficulty,
            currentCategory = selectedCategory,
            onDismiss = { showFilterDialog = false },
            onApply = { difficulty, category ->
                selectedDifficulty = difficulty
                selectedCategory = category
                showFilterDialog = false
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search questions...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Clear, "Clear", tint = Color(0xFF999999))
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedDifficulty: String?,
    selectedCategory: String?,
    onRemoveDifficulty: () -> Unit,
    onRemoveCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        selectedDifficulty?.let {
            FilterChip(
                selected = true,
                onClick = onRemoveDifficulty,
                label = { Text("Difficulty: $it") },
                trailingIcon = {
                    Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                }
            )
        }
        selectedCategory?.let {
            FilterChip(
                selected = true,
                onClick = onRemoveCategory,
                label = { Text("Category: $it") },
                trailingIcon = {
                    Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(16.dp))
                }
            )
        }
    }
}

@Composable
private fun QuestionsList(
    questions: List<QuestionItem>,
    onQuestionClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(questions) { question ->
            QuestionCard(
                question = question,
                onClick = { onQuestionClick(question.id) },
                onDelete = { onDeleteClick(question.id) }
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuestionItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = question.questionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DifficultyChip(question.difficulty)
                        if (question.category.isNotBlank()) {
                            CategoryChip(question.category)
                        }
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFFF6B6B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            if (question.choices.isNotEmpty()) {
                val correctAnswer = question.choices.find { it.isCorrect }
                correctAnswer?.let {
                    Text(
                        text = "Answer: ${it.choiceLabel}. ${it.choiceText}",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${question.choices.size} choices | ${question.points} pts | ${question.timeLimit}s",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            } else {
                Text(
                    text = "${question.points} points | ${question.timeLimit} seconds",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Question") },
            text = { Text("Are you sure you want to delete this question?") },
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun CategoryChip(category: String) {
    Surface(
        color = Color(0xFF667EEA).copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF667EEA)
        )
    }
}

@Composable
private fun EmptyQuestionsContent(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.QuestionAnswer,
            contentDescription = "No questions",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFCCCCCC)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Questions Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first question",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF667EEA)
            )
        ) {
            Icon(Icons.Default.Add, "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Question")
        }
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

@Composable
private fun FilterDialog(
    currentDifficulty: String?,
    currentCategory: String?,
    onDismiss: () -> Unit,
    onApply: (String?, String?) -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(currentDifficulty) }
    var selectedCategory by remember { mutableStateOf(currentCategory) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Questions") },
        text = {
            Column {
                Text("Difficulty", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { difficulty ->
                        FilterChip(
                            selected = selectedDifficulty == difficulty,
                            onClick = {
                                selectedDifficulty = if (selectedDifficulty == difficulty) null else difficulty
                            },
                            label = { Text(difficulty) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Category", fontWeight = FontWeight.Bold)
                TextField(
                    value = selectedCategory ?: "",
                    onValueChange = { selectedCategory = it.takeIf { str -> str.isNotBlank() } },
                    placeholder = { Text("Enter category...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(selectedDifficulty, selectedCategory) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
