package com.example.eduquizz.features.admin.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.admin.data.QuestionChoice
import com.example.eduquizz.features.admin.data.QuestionCreateRequest
import com.example.eduquizz.features.admin.data.QuestionUpdateRequest
import com.example.eduquizz.features.admin.viewmodel.AdminViewModel
import com.example.eduquizz.features.admin.viewmodel.GameType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    username: String,
    gameType: GameType,
    levelId: String,
    questionId: String? = null, // null = create new, not null = edit
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var questionText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Easy") }
    var choices by remember { mutableStateOf(listOf(
        QuestionChoice("A", "", false),
        QuestionChoice("B", "", false),
        QuestionChoice("C", "", false),
        QuestionChoice("D", "", false)
    )) }
    var points by remember { mutableIntStateOf(10) }
    var timeLimit by remember { mutableIntStateOf(30) }
    
    val isEditMode = questionId != null
    val gameName = when (gameType) {
        GameType.WORD_SEARCH -> "Word Search"
        GameType.BAT_CHU -> "Bat Chu"
        GameType.MATCH_GAME -> "Match Game"
        GameType.QUIZ -> "Quiz Game"
        GameType.SCENE -> "Scene Game"
        GameType.SOUND -> "Sound Game"
    }

    // Load existing question if editing
    LaunchedEffect(questionId) {
        if (questionId != null) {
            viewModel.loadQuestionById(username, questionId)
        }
    }

    // Update fields when question is loaded
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    LaunchedEffect(currentQuestion) {
        currentQuestion?.let { question ->
            questionText = question.questionText
            category = question.category
            difficulty = question.difficulty
            choices = question.choices.toMutableList().apply {
                while (size < 4) {
                    add(QuestionChoice((('A'.code + size).toChar()).toString(), "", false))
                }
            }
            points = question.points
            timeLimit = question.timeLimit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Question" else "New Question",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (questionText.isNotBlank() && choices.any { it.isCorrect }) {
                                val validChoices = choices.filter { it.choiceText.isNotBlank() }
                                
                                if (isEditMode && questionId != null) {
                                    val request = QuestionUpdateRequest(
                                        id = questionId,
                                        questionText = questionText,
                                        choices = validChoices,
                                        category = category,
                                        difficulty = difficulty,
                                        points = points,
                                        timeLimit = timeLimit
                                    )
                                    viewModel.updateQuestion(username, request) {
                                        onSaveSuccess()
                                    }
                                } else {
                                    val request = QuestionCreateRequest(
                                        questionText = questionText,
                                        choices = validChoices,
                                        category = category,
                                        difficulty = difficulty,
                                        points = points,
                                        timeLimit = timeLimit,
                                        gameType = gameType.name,
                                        levelId = levelId
                                    )
                                    viewModel.createQuestion(username, request) {
                                        onSaveSuccess()
                                    }
                                }
                            }
                        },
                        enabled = questionText.isNotBlank() && choices.any { it.isCorrect } && !isLoading
                    ) {
                        Icon(Icons.Default.Check, "Save", tint = Color.White)
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Game: $gameName",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF667EEA)
                            )
                            Text(
                                "Level: $levelId",
                                fontSize = 14.sp,
                                color = Color(0xFF999999)
                            )
                        }
                    }
                }

                item {
                    QuestionTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = "Question Text",
                        placeholder = "Enter your question...",
                        required = true
                    )
                }

                item {
                    QuestionTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Category",
                        placeholder = "e.g., Math, Science, History..."
                    )
                }

                item {
                    DifficultySelector(
                        selectedDifficulty = difficulty,
                        onDifficultySelected = { difficulty = it }
                    )
                }

                item {
                    ChoicesEditor(
                        choices = choices,
                        onChoicesChanged = { choices = it }
                    )
                }

                item {
                    PointsAndTimeSelector(
                        points = points,
                        timeLimit = timeLimit,
                        onPointsChange = { points = it },
                        onTimeLimitChange = { timeLimit = it }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Button(
                        onClick = {
                            if (questionText.isNotBlank() && choices.any { it.isCorrect }) {
                                val validChoices = choices.filter { it.choiceText.isNotBlank() }
                                
                                if (isEditMode && questionId != null) {
                                    val request = QuestionUpdateRequest(
                                        id = questionId,
                                        questionText = questionText,
                                        choices = validChoices,
                                        category = category,
                                        difficulty = difficulty,
                                        points = points,
                                        timeLimit = timeLimit
                                    )
                                    viewModel.updateQuestion(username, request) {
                                        onSaveSuccess()
                                    }
                                } else {
                                    val request = QuestionCreateRequest(
                                        questionText = questionText,
                                        choices = validChoices,
                                        category = category,
                                        difficulty = difficulty,
                                        points = points,
                                        timeLimit = timeLimit,
                                        gameType = gameType.name,
                                        levelId = levelId
                                    )
                                    viewModel.createQuestion(username, request) {
                                        onSaveSuccess()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = questionText.isNotBlank() && choices.any { it.isCorrect } && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.Check, "Save")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isEditMode) "Update Question" else "Create Question",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Error Snackbar
            errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
}

@Composable
private fun QuestionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    required: Boolean = false,
    singleLine: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
                if (required) {
                    Text(
                        text = " *",
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun DifficultySelector(
    selectedDifficulty: String,
    onDifficultySelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Difficulty Level",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Easy", "Medium", "Hard").forEach { difficulty ->
                    val isSelected = selectedDifficulty == difficulty
                    val color = when (difficulty) {
                        "Easy" -> Color(0xFF4CAF50)
                        "Medium" -> Color(0xFFFFB74D)
                        "Hard" -> Color(0xFFFF6B6B)
                        else -> Color(0xFF999999)
                    }
                    
                    Button(
                        onClick = { onDifficultySelected(difficulty) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) color else color.copy(alpha = 0.2f),
                            contentColor = if (isSelected) Color.White else color
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            difficulty,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoicesEditor(
    choices: List<QuestionChoice>,
    onChoicesChanged: (List<QuestionChoice>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Answer Choices",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )
            Text(
                text = "Mark the correct answer with checkbox",
                fontSize = 12.sp,
                color = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            choices.forEachIndexed { index, choice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = choice.isCorrect,
                        onCheckedChange = { checked ->
                            val newChoices = choices.mapIndexed { i, c ->
                                if (i == index) c.copy(isCorrect = checked)
                                else c.copy(isCorrect = false) // Only one correct answer
                            }
                            onChoicesChanged(newChoices)
                        }
                    )
                    Text(
                        text = choice.choiceLabel,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA),
                        modifier = Modifier.width(24.dp)
                    )
                    TextField(
                        value = choice.choiceText,
                        onValueChange = { newValue ->
                            val newChoices = choices.toMutableList()
                            newChoices[index] = choice.copy(choiceText = newValue)
                            onChoicesChanged(newChoices)
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Choice ${index + 1}") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
                if (index < choices.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = {
                    val newLabel = (('A'.code + choices.size).toChar()).toString()
                    onChoicesChanged(choices + QuestionChoice(newLabel, "", false))
                }
            ) {
                Icon(Icons.Default.Add, "Add choice")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add more choices")
            }
        }
    }
}

@Composable
private fun PointsAndTimeSelector(
    points: Int,
    timeLimit: Int,
    onPointsChange: (Int) -> Unit,
    onTimeLimitChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Points & Time Limit",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Points selector
                Column(modifier = Modifier.weight(1f)) {
                    Text("Points", fontSize = 14.sp, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = points.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { p ->
                                if (p > 0 && p <= 100) onPointsChange(p)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                // Time limit selector
                Column(modifier = Modifier.weight(1f)) {
                    Text("Time Limit (seconds)", fontSize = 14.sp, color = Color(0xFF666666))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = timeLimit.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { t ->
                                if (t > 0 && t <= 300) onTimeLimitChange(t)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        }
    }
}
