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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.features.admin.data.ContestCreateRequest
import com.example.eduquizz.features.admin.data.ContestUpdateRequest
import com.example.eduquizz.features.admin.data.ContestQuestion
import com.example.eduquizz.features.admin.data.QuestionFilter
import com.example.eduquizz.features.admin.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestEditorScreen(
    username: String,
    contestId: String? = null, // null = create new
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val availableQuestions by viewModel.contestQuestions.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableLongStateOf(System.currentTimeMillis() + 3600000) } // Default: 1 hour from now
    var duration by remember { mutableIntStateOf(30) } // Default 30 minutes
    var selectedQuestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showQuestionSelector by remember { mutableStateOf(false) }

    val isEditMode = contestId != null

    // Load existing contest if editing
    LaunchedEffect(contestId) {
        if (contestId != null) {
            viewModel.loadContestById(username, contestId)
        }
        // Load available questions for selection
        viewModel.loadContestQuestions(username, QuestionFilter())
    }

    // Update fields when contest is loaded
    val currentContest by viewModel.currentContest.collectAsState()
    LaunchedEffect(currentContest) {
        currentContest?.let { contest ->
            title = contest.title
            description = contest.description
            duration = contest.duration
            selectedQuestions = contest.questionIds
            startTime = contest.startTime
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Contest" else "New Contest",
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
                            if (title.isNotBlank() && selectedQuestions.isNotEmpty()) {
                                val endTime = startTime + (duration * 60 * 1000L)
                                if (isEditMode && contestId != null) {
                                    val request = ContestUpdateRequest(
                                        id = contestId,
                                        title = title,
                                        description = description,
                                        startTime = startTime,
                                        endTime = endTime,
                                        duration = duration,
                                        questionIds = selectedQuestions
                                    )
                                    viewModel.updateContest(username, request) {
                                        onSaveSuccess()
                                    }
                                } else {
                                    val request = ContestCreateRequest(
                                        title = title,
                                        description = description,
                                        startTime = startTime,
                                        endTime = endTime,
                                        duration = duration,
                                        questionIds = selectedQuestions,
                                        createdBy = username
                                    )
                                    viewModel.createContest(username, request) {
                                        onSaveSuccess()
                                    }
                                }
                            }
                        },
                        enabled = title.isNotBlank() && selectedQuestions.isNotEmpty() && !isLoading
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
                    ContestTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Contest Title",
                        placeholder = "Enter contest title...",
                        required = true
                    )
                }

                item {
                    ContestTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Description",
                        placeholder = "Enter contest description...",
                        singleLine = false
                    )
                }

                item {
                    DateTimeSelector(
                        startTime = startTime,
                        onTimeChange = { startTime = it }
                    )
                }

                item {
                    DurationSelector(
                        duration = duration,
                        onDurationChange = { duration = it }
                    )
                }

                item {
                    QuestionSelectionCard(
                        selectedCount = selectedQuestions.size,
                        onSelectClick = { showQuestionSelector = true }
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (title.isNotBlank() && selectedQuestions.isNotEmpty()) {
                                val endTime = startTime + (duration * 60 * 1000L)
                                if (isEditMode && contestId != null) {
                                    val request = ContestUpdateRequest(
                                        id = contestId,
                                        title = title,
                                        description = description,
                                        startTime = startTime,
                                        endTime = endTime,
                                        duration = duration,
                                        questionIds = selectedQuestions
                                    )
                                    viewModel.updateContest(username, request) {
                                        onSaveSuccess()
                                    }
                                } else {
                                    val request = ContestCreateRequest(
                                        title = title,
                                        description = description,
                                        startTime = startTime,
                                        endTime = endTime,
                                        duration = duration,
                                        questionIds = selectedQuestions,
                                        createdBy = username
                                    )
                                    viewModel.createContest(username, request) {
                                        onSaveSuccess()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = title.isNotBlank() && selectedQuestions.isNotEmpty() && !isLoading,
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
                                if (isEditMode) "Update Contest" else "Create Contest",
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

    // Question Selector Dialog
    if (showQuestionSelector) {
        QuestionSelectorDialog(
            questions = availableQuestions,
            selectedQuestionIds = selectedQuestions,
            onDismiss = { showQuestionSelector = false },
            onConfirm = { selected ->
                selectedQuestions = selected
                showQuestionSelector = false
            }
        )
    }
}

@Composable
private fun ContestTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    required: Boolean = false,
    singleLine: Boolean = true
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
                maxLines = if (singleLine) 1 else 5
            )
        }
    }
}

@Composable
private fun DateTimeSelector(
    startTime: Long,
    onTimeChange: (Long) -> Unit
) {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = startTime
    }
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val month = calendar.get(java.util.Calendar.MONTH)
    val year = calendar.get(java.util.Calendar.YEAR)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Day", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = day.toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { d ->
                                if (d in 1..31) {
                                    val newCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = startTime
                                        set(java.util.Calendar.DAY_OF_MONTH, d)
                                    }
                                    onTimeChange(newCal.timeInMillis)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Month", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = (month + 1).toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { m ->
                                if (m in 1..12) {
                                    val newCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = startTime
                                        set(java.util.Calendar.MONTH, m - 1)
                                    }
                                    onTimeChange(newCal.timeInMillis)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Year", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = { 
                            it.toIntOrNull()?.let { y ->
                                if (y in 2024..2030) {
                                    val newCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = startTime
                                        set(java.util.Calendar.YEAR, y)
                                    }
                                    onTimeChange(newCal.timeInMillis)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hour", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = hour.toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { h ->
                                if (h in 0..23) {
                                    val newCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = startTime
                                        set(java.util.Calendar.HOUR_OF_DAY, h)
                                    }
                                    onTimeChange(newCal.timeInMillis)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                Column(modifier = Modifier.weight(1f)) {
                    Text("Minute", fontSize = 12.sp, color = Color(0xFF999999))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = minute.toString().padStart(2, '0'),
                        onValueChange = { 
                            it.toIntOrNull()?.let { m ->
                                if (m in 0..59) {
                                    val newCal = java.util.Calendar.getInstance().apply {
                                        timeInMillis = startTime
                                        set(java.util.Calendar.MINUTE, m)
                                    }
                                    onTimeChange(newCal.timeInMillis)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            Text(
                text = "Contest starts at ${dateFormat.format(java.util.Date(startTime))}",
                fontSize = 12.sp,
                color = Color(0xFF667EEA)
            )
        }
    }
}

@Composable
private fun DurationSelector(
    duration: Int,
    onDurationChange: (Int) -> Unit
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
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF667EEA),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Duration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    Button(
                        onClick = { onDurationChange(minutes) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (duration == minutes) 
                                Color(0xFF667EEA) 
                            else 
                                Color(0xFF667EEA).copy(alpha = 0.2f),
                            contentColor = if (duration == minutes) 
                                Color.White 
                            else 
                                Color(0xFF667EEA)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("$minutes min")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = duration.toString(),
                onValueChange = { 
                    it.toIntOrNull()?.let { min ->
                        if (min > 0 && min <= 180) onDurationChange(min)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Custom duration (minutes)") },
                singleLine = true
            )
        }
    }
}

@Composable
private fun QuestionSelectionCard(
    selectedCount: Int,
    onSelectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        tint = Color(0xFF667EEA),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Questions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )
                    if (selectedCount == 0) {
                        Text(
                            text = " *",
                            color = Color(0xFFFF6B6B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedCount > 0) 
                        "$selectedCount questions selected" 
                    else 
                        "Tap to select questions",
                    fontSize = 14.sp,
                    color = if (selectedCount > 0) Color(0xFF4CAF50) else Color(0xFF999999)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = Color(0xFF667EEA)
            )
        }
    }
}

@Composable
private fun QuestionSelectorDialog(
    questions: List<ContestQuestion>,
    selectedQuestionIds: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedQuestionIds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("Select Questions")
                Text(
                    "${tempSelected.size} selected",
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    fontWeight = FontWeight.Normal
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(questions) { question ->
                    val isSelected = tempSelected.contains(question.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelected = if (isSelected) {
                                    tempSelected - question.id
                                } else {
                                    tempSelected + question.id
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) 
                                Color(0xFF667EEA).copy(alpha = 0.1f) 
                            else 
                                Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = question.questionText,
                                    fontSize = 14.sp,
                                    maxLines = 2
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = question.category,
                                        fontSize = 11.sp,
                                        color = Color(0xFF999999)
                                    )
                                    Text(
                                        text = question.difficulty,
                                        fontSize = 11.sp,
                                        color = when (question.difficulty.lowercase()) {
                                            "easy" -> Color(0xFF4CAF50)
                                            "medium" -> Color(0xFFFFB74D)
                                            else -> Color(0xFFFF6B6B)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(tempSelected) },
                enabled = tempSelected.isNotEmpty()
            ) {
                Text("Confirm (${tempSelected.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
