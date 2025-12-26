package com.example.eduquizz.features.contest.screens

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.eduquizz.data.models.Game
import com.example.eduquizz.features.ContestOnline.ContestPrefs
import com.example.eduquizz.features.ContestOnline.GamePauseReceiver
import com.example.eduquizz.features.ContestOnline.Model.QuestionItemContest
import com.example.eduquizz.features.ContestOnline.viewModel.ContestSecureViewModel
import com.example.eduquizz.navigation.Routes
import kotlinx.coroutines.delay

/**
 * Contest Screen với UI Premium và Server-Side Validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    modifier: Modifier = Modifier,
    viewModel: ContestSecureViewModel = hiltViewModel(),
    userName: String = "Player1",
    onBackClick: () -> Unit = {},
    onGameClick: (Game) -> Unit = {},
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isPaused by remember { mutableStateOf(false) }

    // Game pause receiver
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

    // Start contest
    LaunchedEffect(Unit) {
        viewModel.startContest("English/QuizGame/LevelEasy")
    }

    // Timer countdown
    LaunchedEffect(uiState.sessionActive, isPaused) {
        if (uiState.sessionActive && !uiState.showResult) {
            while (uiState.timeLeft > 0 && !uiState.showResult) {
                if (!isPaused) {
                    delay(1000)
                    viewModel.updateTimeLeft(uiState.timeLeft - 1)
                } else {
                    delay(500)
                }
            }
            if (uiState.timeLeft <= 0) {
                viewModel.onTimeUp()
            }
        }
    }

    // Pause overlay
    if (isPaused) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏸️", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Trò chơi đã tạm dừng",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Quay lại ứng dụng để tiếp tục",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        return
    }

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar
            ContestTopBar(
                timeLeft = uiState.timeLeft,
                score = uiState.clientScore,
                currentQuestion = uiState.currentIndex + 1,
                totalQuestions = uiState.questions.size,
                onBackClick = {
                    viewModel.resetContest()
                    onBackClick()
                }
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when {
                    uiState.loading -> {
                        LoadingView()
                    }

                    uiState.error != null -> {
                        ErrorView(
                            error = uiState.error!!,
                            onRetry = onBackClick
                        )
                    }

                    uiState.showResult -> {
                        PremiumResultScreen(
                            clientScore = uiState.clientScore,
                            verifiedScore = uiState.verifiedScore,
                            validationError = uiState.validationError,
                            flaggedSuspicious = uiState.flaggedSuspicious,
                            submitting = uiState.submitting,
                            totalQuestions = uiState.questions.size,
                            onExit = {
                                ContestPrefs.saveJoinDate(context)
                                viewModel.resetContest()
                                navController.navigate(Routes.LEADERBOARD_GAMES_SCENE)
                            }
                        )
                    }

                    uiState.questions.isNotEmpty() -> {
                        val question = uiState.questions.getOrNull(uiState.currentIndex)
                        if (question != null) {
                            PremiumQuestionView(
                                question = question,
                                questionNumber = uiState.currentIndex + 1,
                                totalQuestions = uiState.questions.size,
                                onAnswerSelected = { answer ->
                                    viewModel.submitAnswer(answer)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContestTopBar(
    timeLeft: Int,
    score: Int,
    currentQuestion: Int,
    totalQuestions: Int,
    onBackClick: () -> Unit
) {
    val isTimeWarning = timeLeft <= 60
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isTimeWarning) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        color = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(12.dp))

            // Title
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "🏆 Cuộc thi Online",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Câu $currentQuestion / $totalQuestions",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Timer
            Surface(
                color = if (isTimeWarning) Color(0xFFFF5252) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.scale(pulseScale)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "⏱️",
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Score
            Surface(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "$score",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF667eea),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Đang kết nối...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF667eea)
                )
                Text(
                    "Chuẩn bị phiên thi đấu an toàn",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Có lỗi xảy ra",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667eea)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quay lại", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun PremiumQuestionView(
    question: QuestionItemContest,
    questionNumber: Int,
    totalQuestions: Int,
    onAnswerSelected: (String) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var answered by remember { mutableStateOf(false) }

    LaunchedEffect(question.question) {
        selectedAnswer = null
        answered = false
    }

    // Animation for question appearance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(question) {
        visible = false
        delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { -50 },
        exit = fadeOut()
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Security badge
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Bảo mật Server",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Progress indicator
                LinearProgressIndicator(
                    progress = { questionNumber.toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF667eea),
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(Modifier.height(24.dp))

                // Question text
                Text(
                    text = question.question,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF1A1A2E),
                    lineHeight = 32.sp
                )

                Spacer(Modifier.height(32.dp))

                // Answer options
                question.choices.forEachIndexed { index, choice ->
                    val optionLetter = ('A' + index).toString()
                    
                    val bgColor = when {
                        !answered -> Color(0xFF667eea)
                        choice == selectedAnswer && choice == question.answer -> Color(0xFF4CAF50)
                        choice == selectedAnswer && choice != question.answer -> Color(0xFFFF5252)
                        choice == question.answer && answered -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                        else -> Color(0xFFBDBDBD)
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
                            .padding(vertical = 6.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option letter badge
                            Surface(
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        optionLetter,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                choice,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumResultScreen(
    clientScore: Int,
    verifiedScore: Int?,
    validationError: String?,
    flaggedSuspicious: Boolean,
    submitting: Boolean,
    totalQuestions: Int,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    submitting -> {
                        // Loading state
                        CircularProgressIndicator(
                            color = Color(0xFF667eea),
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "🔐 Đang xác thực điểm số...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF667eea)
                        )
                        Text(
                            "Vui lòng đợi trong giây lát",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    validationError != null -> {
                        // Error state
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = CircleShape,
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "Xác thực thất bại!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )

                        Spacer(Modifier.height(16.dp))

                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    when (validationError) {
                                        "SCORE_MISMATCH" -> "⚠️ Điểm số không khớp"
                                        "INVALID_SIGNATURE" -> "⚠️ Phát hiện thay đổi dữ liệu"
                                        "SESSION_EXPIRED" -> "⚠️ Phiên chơi đã hết hạn"
                                        else -> "⚠️ Có lỗi xảy ra"
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Điểm của bạn không được ghi nhận",
                                    fontSize = 14.sp,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }

                    verifiedScore != null -> {
                        // Success state
                        Text(
                            "🎉",
                            fontSize = 64.sp,
                            modifier = Modifier.scale(scale)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "Chúc mừng!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )

                        Spacer(Modifier.height(24.dp))

                        // Score card
                        Surface(
                            color = Color(0xFF667eea),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(20.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Điểm đã xác thực",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "$verifiedScore",
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "điểm",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Stats
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${verifiedScore / 10}/$totalQuestions",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Text("Câu đúng", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${(verifiedScore * 100) / (totalQuestions * 10)}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF667eea)
                                )
                                Text("Tỷ lệ", fontSize = 12.sp, color = Color.Gray)
                            }
                        }

                        if (flaggedSuspicious) {
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "⚠️ Bài làm đang được xem xét",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Exit button
                Button(
                    onClick = onExit,
                    enabled = !submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667eea)
                    ),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        "🏆 Xem Bảng Xếp Hạng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}