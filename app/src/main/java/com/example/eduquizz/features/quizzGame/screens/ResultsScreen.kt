package com.example.eduquizz.features.quizzGame.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.eduquizz.R
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.navigation.Routes
import com.example.eduquizz.data_save.AudioManager
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay

@Composable
fun ResultsScreen(
    navController: NavController,
    correctAnswers: Int,
    totalQuestions: Int,
    animationSize: Int = 220,
    backgroundColors: List<Color> = listOf(
        Color(0xFFFFF3E0),
        Color(0xFFFFEBEE),
        Color(0xFFE3F2FD)
    ),
    dataviewModel: DataViewModel = hiltViewModel(),
    questionViewModel: com.example.eduquizz.features.quizzGame.viewmodel.QuestionViewModel? = null,
    back_route:String = "",
    play_agian_route:String = "",
) {
    //Chặn back lại
    val context = LocalContext.current
    val activity = context as? Activity

    var backPressedOnce by remember { mutableStateOf(false) }
    var goldAdded by remember { mutableStateOf(false) }
    var verifiedCoins by remember { mutableStateOf(correctAnswers * 10) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (backPressedOnce) {
            activity?.finish() // Thoát app
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Bấm lần nữa để thoát", Toast.LENGTH_SHORT).show()

        }
    }
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000)
            backPressedOnce = false
        }
    }
    //_______end chặn back lại
    val lottieAnimationRes: Int = R.raw.iconwin
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieAnimationRes))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    // NOTE: correctAnswers param from URL is actually the SCORE (e.g., 50), not the count (e.g., 5)
    // So we need to calculate the actual correct count for display
    val actualCorrectCount = correctAnswers / 10  // Convert score to count
    val coinsEarned = correctAnswers  // Already the score, don't multiply by 10
    
    val encouragementText = when {
        actualCorrectCount == totalQuestions -> "🎉 Xuất sắc! Bạn đã trả lời đúng tất cả!"
        actualCorrectCount > totalQuestions / 2 -> "👍 Tốt lắm! Hãy cố gắng thêm nữa nhé!"
        else -> "💪 Đừng nản lòng! Lần sau sẽ tốt hơn!"
    }
    
    // Server-side score validation
    LaunchedEffect(Unit) {
        if (!goldAdded) {
            if (questionViewModel != null && questionViewModel.useServerValidation.value) {
                // Use server-side validation
                // NOTE: correctAnswers param is actually the SCORE (from URL), not the count!
                val scoreFromParams = correctAnswers  // Already the score, don't multiply by 10
                android.util.Log.d("ResultsScreen", "🔄 Submitting score to server for validation... (scoreFromParams=$scoreFromParams)")
                questionViewModel.submitScoreToServer(clientScoreOverride = scoreFromParams) { result ->
                    if (result.success) {
                        verifiedCoins = result.verifiedScore
                        dataviewModel.addGold(result.verifiedScore)
                        android.util.Log.d("ResultsScreen", "✅ Server verified score: ${result.verifiedScore}")
                        
                        if (result.flaggedSuspicious) {
                            validationMessage = "⚠️ ${result.message}"
                        }
                    } else {
                        // Validation failed - FALLBACK to client score for now
                        // (while we debug server-side issues)
                        android.util.Log.e("ResultsScreen", "❌ Server rejected score: ${result.errorCode}, falling back to client score")
                        verifiedCoins = scoreFromParams
                        dataviewModel.addGold(scoreFromParams)  // Use client score as fallback
                        validationMessage = "⚠️ Server validation failed, using client score"
                    }
                    goldAdded = true
                }
            } else {
                // Fallback: client-side (old behavior)
                android.util.Log.d("ResultsScreen", "⚠️ Using client-side scoring (no server validation)")
                dataviewModel.addGold(coinsEarned)
                goldAdded = true
            }
            
            // Update statistics (use actualCorrectCount, not correctAnswers which is the score)
            dataviewModel.addTotalQuestions(totalQuestions)
            dataviewModel.addCorrectAnsweredQuestions(actualCorrectCount)
            if(actualCorrectCount == totalQuestions){
                dataviewModel.addCorrectAllQuestions(1)
            }else if(actualCorrectCount > totalQuestions/2){
                dataviewModel.addCorrectAbove50Percent(1)
            }else{
                dataviewModel.addCorrectBelow50Percent(1)
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = backgroundColors)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

//                Button(
//                    onClick = {
//                        AudioManager.playClickSfx()
//                        // 🔁 Chơi lại: navigate lại đến màn quiz
//                        navController.navigate(Routes.MAIN)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                Text(
                    text = "🎯 Kết Quả",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5)

                )
                Card(
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieProgress },
                            modifier = Modifier.size(animationSize.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "✅ Trả lời đúng: $actualCorrectCount / $totalQuestions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "💰 Bạn nhận được $coinsEarned xu",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFC107)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = encouragementText,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF6D4C41),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }


//                Button(
//                    onClick = {
//                        AudioManager.playClickSfx()
//                        // 🔙 Quay lại: ví dụ về quay về màn hình chọn chế độ chơi
//                        navController.navigate(Routes.INTRO)
//                    },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(play_agian_route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Chơi lại", color = Color.White)
                    }

                    Button(
                        onClick = {
                            navController.navigate(back_route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("Quay lại", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WinGame() {
    QuizAppTheme {
        ResultsScreen(navController = rememberNavController(),10,10)
    }
}