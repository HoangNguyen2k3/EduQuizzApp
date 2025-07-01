//package com.example.eduquizz.features.home.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.res.colorResource
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.quizapp.ui.theme.QuizAppTheme
//import com.example.eduquizz.R
//
//@Composable
//fun ReadyScreen(
//    modifier: Modifier
//){
//    var userName by remember { mutableStateOf("") }
//    //
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    Box(
//        modifier = modifier.fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(
//                        colorResource(id=R.color.bg_very_light_gray),
//                        colorResource(id = R.color.bg_light_gray),
//                        colorResource(id = R.color.bg_darker_gray)
//                    )
//                )
//            )
//    ){
//        Column(
//            modifier = Modifier.fillMaxSize()
//                .padding()
//        ) {
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
//        ReadyScreen(modifier = )
//    }
//}
