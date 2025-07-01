package com.example.eduquizz.Screens.QuizzTracNhiem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eduquizz.R
import com.example.eduquizz.navigation.Routes

@Composable
fun IntroScreen(navController: NavController) {
    val sampleImages = listOf(
        R.drawable.image,
        R.drawable.image,
        R.drawable.image
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .clip(RoundedCornerShape(9.dp)) ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.quizzbanner),
            contentDescription = "Banner Game",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(0.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Fun Quiz Game",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Một hành trình khám phá kiến thức dành cho mọi lứa tuổi. Từ câu hỏi đơn giản đến hóc búa, trò chơi giúp bạn rèn luyện tư duy và tăng vốn hiểu biết mỗi ngày.",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp),
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(250.dp), // chiều cao cố định để làm ảnh dọc rõ ràng
        ) {
            items(sampleImages) { image ->
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Ảnh mẫu",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(160.dp)       // chiều rộng nhỏ hơn chiều cao -> ảnh dọc
                        .fillMaxHeight()     // cho ảnh chiếm toàn bộ chiều cao container
                        .clip(RoundedCornerShape(16.dp)) // bo góc ảnh mềm mại
                        .background(Color.LightGray)
                        .shadow(8.dp, RoundedCornerShape(16.dp)) // bóng nhẹ
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navController.navigate("main/LevelEasy") },
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC407A)),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
        ) {
            Text(
                text = "Start",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
