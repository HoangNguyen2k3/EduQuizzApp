package com.example.eduquizz.features.contest.screens

import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.eduquizz.data.models.Game
import com.example.eduquizz.features.ContestOnline.ContestPrefs
import com.example.eduquizz.features.ContestOnline.GamePauseReceiver
import com.example.eduquizz.features.ContestOnline.Model.QuestionItemContest
import com.example.eduquizz.features.contest.viewmodel.QuestionViewModelFromFirebase
import com.example.eduquizz.navigation.Routes
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    modifier: Modifier = Modifier,
    viewModel: QuestionViewModelFromFirebase = hiltViewModel(),
    userName: String = "Player1",
    onBackClick: () -> Unit = {},
    onGameClick: (Game) -> Unit = {},
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    var timeLeft by remember { mutableStateOf(600) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var isPaused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val receiver = remember {
        GamePauseReceiver(
            onPauseGame = { isPaused = true },
            onResumeGame = { isPaused = false }
        )
    }
    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Nếu game bị pause → hiển thị thông báo
    if (isPaused) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("⏸ Trò chơi đã tạm dừng", fontSize = 20.sp, color = Color.Red)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.loadQuestions("English/QuizGame/LevelEasy")
    }

    /*    LaunchedEffect(uiState.questions.isNotEmpty()) {
            if (uiState.questions.isNotEmpty()) {
                while (timeLeft > 0 && !showResult) {
                    delay(1000)
                    timeLeft -= 1
                }
                showResult = true
                //saveResultToFirebase(userName, score)
            }
        }*/
    LaunchedEffect(uiState.questions.isNotEmpty()) {
        if (uiState.questions.isNotEmpty()) {
            while (timeLeft > 0 && !showResult) {
                if (!isPaused) {      // ⏸ chỉ trừ thời gian khi không bị pause
                    delay(1000)
                    timeLeft -= 1
                } else {
                    delay(500) // tạm chờ kiểm tra lại sau nửa giây
                }
            }
            showResult = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Cuộc thi online", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "⏰ ${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                            fontSize = 16.sp,
                            color = Color(0xFF1E88E5)
                        )
                        Text("Điểm: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                }
            )
        },
        content = { padding ->
            when {
                uiState.loading -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        Alignment.Center
                    ) {
                        Text("Lỗi tải dữ liệu: ${uiState.error}", color = Color.Red)
                    }
                }

                uiState.questions.isNotEmpty() -> {
                    val question = uiState.questions.getOrNull(currentIndex)

                    if (showResult || question == null) {
                        ResultScreen(score = score, onExit = {
                            currentIndex = 0
                            score = 0
                            timeLeft = 600
                            showResult = false
                            //onBackClick()
                            navController.navigate(Routes.LEADERBOARD_GAMES_SCENE)
                        })
                    } else {
                        QuestionQuizView(
                            question = question,
                            timeLeft = timeLeft,
                            score = score,
                            questionNumber = currentIndex + 1,
                            totalQuestions = uiState.questions.size,
                            onAnswerSelected = { answer ->
                                val correct = answer == question.answer
                                if (correct) score += 10

                                coroutineScope.launch {
                                    delay(600)
                                    if (currentIndex < uiState.questions.lastIndex) {
                                        currentIndex++
                                    } else {
                                        showResult = true
                                        saveResultToFirebase(userName, score)
                                    }
                                }
                            },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun QuestionQuizView(
    question: QuestionItemContest,
    timeLeft: Int,
    score: Int,
    questionNumber: Int,
    totalQuestions: Int,
    onAnswerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var answered by remember { mutableStateOf(false) }

    // 🔹 Reset lại khi câu hỏi mới hiển thị
    LaunchedEffect(question.question) {
        selectedAnswer = null
        answered = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color(0xFFF7F9FC)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Câu $questionNumber / $totalQuestions",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = question.question,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))

                question.choices.forEach { choice ->
                    val bgColor = when {
                        !answered -> Color(0xFF42A5F5)
                        choice == selectedAnswer && choice == question.answer -> Color(0xFF4CAF50)
                        choice == selectedAnswer && choice != question.answer -> Color(0xFFD32F2F)
                        else -> Color(0xFF90CAF9)
                    }

                    Button(
                        onClick = {
                            if (!answered) {
                                selectedAnswer = choice
                                answered = true
                                onAnswerSelected(choice)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(choice, fontSize = 18.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}


@Composable
fun ResultScreen(score: Int, onExit: () -> Unit) {
    val context = LocalContext.current
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text("🎉 Cuộc thi đã kết thúc!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Tổng điểm của bạn: $score", fontSize = 20.sp, color = Color(0xFF1E88E5))
                Spacer(Modifier.height(20.dp))
                Button(onClick = {
                    ContestPrefs.saveJoinDate(context) // lưu lại ngày đã chơi
                    onExit()
                }) {

                    Text("Thoát", fontSize = 18.sp)

                }
            }
        }
    }
}

/**
 * 🏆 Lưu kết quả lên Firebase để cập nhật BXH
 */
private val leaderboardRef by lazy {
    FirebaseDatabase.getInstance().getReference("Contest/Leaderboard")
}
private fun saveResultToFirebase(userName: String, score: Int) {
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    val entry = mapOf(
        "name" to userName,
        "score" to score,
        "date" to today,
        "timestamp" to System.currentTimeMillis()
    )
    leaderboardRef.push().setValue(entry)
}
/*private fun saveResultToFirebase(userName: String, score: Int) {
    val ref = FirebaseDatabase.getInstance().getReference("Contest/Leaderboard")
    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

    val entry = mapOf(
        "name" to userName,
        "score" to score,
        "date" to today,
        "timestamp" to System.currentTimeMillis()
    )
    ref.push().setValue(entry)
}*/