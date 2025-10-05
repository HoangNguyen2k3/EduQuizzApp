package com.example.eduquizz.features.soundgame.screen

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.navigation.NavHostController
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.navigation.Routes
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.DisposableEffect
import com.example.eduquizz.features.soundgame.viewmodel.SoundGameViewModel

@Composable
fun SoundGameScreen(
    viewModel: SoundGameViewModel = hiltViewModel(),
    navController: NavHostController,
    dataViewModel: DataViewModel = hiltViewModel(),
    levelId: String = "LevelEasy"
) {
    val gold by dataViewModel.gold.observeAsState(-1)
    val timer by viewModel.timerSeconds
    val currentQuestionIndex by viewModel.currentQuestionIndex
    val totalQuestions by viewModel.totalQuestions
    val showResult by viewModel.showResult
    val showFinishDialog by viewModel.showFinishDialog
    val currentClip by viewModel.currentClip
    val userAnswer by viewModel.userAnswer
    val isAnswerSubmitted by viewModel.isAnswerSubmitted
    val isPlaying by viewModel.isPlaying
    val canReplay by viewModel.canReplay

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AudioManager.setBgmEnabled(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioManager.setBgmEnabled(false)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.Init(dataViewModel, levelId = levelId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Back button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Question ${currentQuestionIndex + 1}/$totalQuestions",
                fontWeight = FontWeight.Bold
            )
            Text(
                "Gold: $gold",
                color = Color(0xFFFFB800),
                fontWeight = FontWeight.Bold
            )
        }

        // Timer Progress Bar
        val progress = timer.toFloat() / 30f
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Time: ${timer}s",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.LightGray)
            ) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = if (timer <= 10) Color.Red else Color(0xFF3F51B5),
                    trackColor = Color.Transparent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Game content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sound instructions
            Text(
                text = "Listen to the sound and guess what it is",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Play button
            Button(
                onClick = {
                    viewModel.playAudio()
                },
                enabled = canReplay && !isAnswerSubmitted,
                modifier = Modifier
                    .size(120.dp),
                shape = RoundedCornerShape(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A85F5)
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Answer input
            OutlinedTextField(
                value = userAnswer,
                onValueChange = { viewModel.userAnswer.value = it },
                label = { Text("Your Answer") },
                enabled = !isAnswerSubmitted,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        viewModel.submitAnswer()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4A85F5),
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Show result after submission
            if (isAnswerSubmitted && currentClip != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentClip?.isCorrect == true)
                            Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else
                            Color(0xFFF44336).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentClip?.isCorrect == true) "Correct!" else "Wrong!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentClip?.isCorrect == true)
                                Color(0xFF4CAF50)
                            else
                                Color(0xFFF44336)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Answer: ${currentClip?.answer}",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    viewModel.skipQuestion()
                },
                enabled = gold >= 50 && !isAnswerSubmitted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF7C873)
                ),
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Skip (-50)", color = Color.White)
            }

            Button(
                onClick = {
                    viewModel.submitAnswer()
                },
                enabled = !isAnswerSubmitted && userAnswer.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4A85F5)
                ),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Submit", color = Color.White)
            }
        }

        // Finish dialog
        if (showFinishDialog) {
            Toast.makeText(context, "Game completed!", Toast.LENGTH_SHORT).show()
            navController.navigate(
                "result/${viewModel.totalRight.value}/${viewModel.totalQuestions.value}/${Routes.ENGLISH_GAMES_SCENE}/${Routes.ENGLISH_GAMES_SCENE}"
            )
        }
    }
}