package com.example.eduquizz.features.quizzGame.screens

import GifImage
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.eduquizz.navigation.Routes

@Composable
fun ResultsScreen(
    navController: NavController,
    correctAnswers: Int,
    totalQuestions: Int
) {
    val coinsEarned = correctAnswers * 5
    val encouragementText = when {
        correctAnswers == totalQuestions -> "🎉 Xuất sắc! Bạn đã trả lời đúng tất cả!"
        correctAnswers > totalQuestions / 2 -> "👍 Tốt lắm! Hãy cố gắng thêm nữa nhé!"
        else -> "💪 Đừng nản lòng! Lần sau sẽ tốt hơn!"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯 Kết Quả",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            GifImage(
                gifResId = R.drawable.congratgif,
                modifier = Modifier
                    .height(200.dp)
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "✅ Bạn trả lời đúng $correctAnswers / $totalQuestions câu",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "💰 Bạn nhận được $coinsEarned xu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFC107)
            )

            Text(
                text = encouragementText,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nút hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // 🔁 Chơi lại: navigate lại đến màn quiz
                        navController.navigate(Routes.MAIN)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text(text = "Chơi lại", color = Color.White)
                }

                Button(
                    onClick = {
                        // 🔙 Quay lại: ví dụ về quay về màn hình chọn chế độ chơi
                        navController.navigate(Routes.INTRO)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text(text = "Quay lại", color = Color.White)
                }
            }
        }
    }
}
