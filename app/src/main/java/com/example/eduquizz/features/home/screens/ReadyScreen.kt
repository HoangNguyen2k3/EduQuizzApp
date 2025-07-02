//package com.example.eduquizz.features.home.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Card
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import com.airbnb.lottie.compose.LottieAnimation
//import com.example.quizapp.ui.theme.QuizAppTheme
//import com.example.eduquizz.R
//
//@Composable
//fun ReadyScreen(
//
//){
//
//    Box(
//        modifier = Modifier.fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        colorResource(id = R.color.bg_very_light_gray),
//                        colorResource(id = R.color.bg_light_gray),
//                        colorResource(id = R.color.bg_darker_gray)
//                    )
//                )
//            )
//    ){
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//
//        ) {
//            Spacer(modifier = Modifier.height(80.dp))
//
//            LottieAnimation(
//                composition = compo
//            )
//            Card(
//
//            ) {
//
//            }
//
//        }
//
//    }
//}
//
//@Preview(
//    showBackground = true,
//    widthDp = 360,
//    heightDp = 640
//)
//@Composable
//fun ReadyScreenPreview(){
//    QuizAppTheme {
//        ReadyScreen()
//    }
//}
