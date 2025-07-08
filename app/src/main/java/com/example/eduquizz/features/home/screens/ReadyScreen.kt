package com.example.eduquizz.features.home.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.eduquizz.R
import com.example.eduquizz.data.local.UserViewModel
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch

@Composable
fun ReadyScreen(
    onStartClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = hiltViewModel()
) {
    var userName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Kiểm tra xem đã có tên được lưu chưa
    val existingUserName by userViewModel.userName.collectAsState()

    // Nếu đã có tên được lưu, chuyển thẳng đến màn hình chính
    LaunchedEffect(existingUserName) {
        if (existingUserName.isNotEmpty()) {
            onStartClick(existingUserName)
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcome_learn))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val textScale by animateFloatAsState(
        targetValue = if (userName.isNotEmpty()) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "textScale"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Hàm xử lý khi nhấn Start
    fun handleStartClick() {
        if (userName.isNotBlank()) {
            isLoading = true
            isError = false

            // Lưu tên vào local storage
            userViewModel.setUserName(userName.trim())

            // Simulate a short delay for better UX
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(500)
                isLoading = false
                onStartClick(userName.trim())
            }
        } else {
            isError = true
            errorMessage = "Vui lòng nhập tên của bạn"
        }
    }

    // Chỉ hiển thị màn hình này nếu chưa có tên được lưu
    if (existingUserName.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF667eea).copy(alpha = 0.8f + animatedOffset * 0.2f),
                            Color(0xFF764ba2).copy(alpha = 0.6f),
                            Color(0xFF2d1b4e)
                        ),
                        radius = 1000f + animatedOffset * 200f
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = 100.dp + (animatedOffset * 20).dp)
                    .background(
                        Color.White.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .blur(50.dp)
            )

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 250.dp, y = 200.dp + (animatedOffset * -15).dp)
                    .background(
                        colorResource(id = R.color.english_coral).copy(alpha = 0.2f),
                        CircleShape
                    )
                    .blur(40.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                Card(
                    modifier = Modifier
                        .size(260.dp)
                        .offset(y = floatingOffset.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = colorResource(id = R.color.english_coral),
                            spotColor = colorResource(id = R.color.english_coral)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(250.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Welcome to",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(textScale)
                        .alpha(0.8f)
                )

                Text(
                    text = "PlayQuiz!",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(textScale)
                        .padding(top = 4.dp)
                )

                Text(
                    text = "Học không chơi đánh rơi tuổi trẻ Chơi không học bán rẻ tương lai",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 12.dp, start = 10.dp, end = 10.dp)
                        .alpha(0.9f)
                )

                Spacer(modifier = Modifier.weight(1f))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            OutlinedTextField(
                                value = userName,
                                onValueChange = {
                                    userName = it
                                    if (isError && it.isNotBlank()) {
                                        isError = false
                                        errorMessage = ""
                                    }
                                },
                                label = {
                                    Text(
                                        "Enter your name",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                placeholder = {
                                    Text(
                                        "What's your name?",
                                        color = Color.Gray.copy(alpha = 0.6f)
                                    )
                                },
                                isError = isError,
                                supportingText = if (isError) {
                                    {
                                        Text(
                                            errorMessage.ifEmpty { "Please enter your name" },
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else null,
                                leadingIcon = {
                                    Card(
                                        modifier = Modifier.size(32.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isError)
                                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                            else
                                                colorResource(id = R.color.english_coral).copy(alpha = 0.1f)
                                        ),
                                        shape = CircleShape
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Name",
                                                tint = if (isError) MaterialTheme.colorScheme.error
                                                else colorResource(id = R.color.english_coral),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        handleStartClick()
                                    }
                                ),
                                singleLine = true,
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedLabelColor = colorResource(id = R.color.english_coral),
                                    cursorColor = colorResource(id = R.color.english_coral),
                                    unfocusedLabelColor = Color.Gray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { handleStartClick() },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    ambientColor = colorResource(id = R.color.english_coral),
                                    spotColor = colorResource(id = R.color.english_coral)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                colorResource(id = R.color.english_coral)
                                                    .copy(alpha = if (isLoading) 0.6f else 1f),
                                                colorResource(id = R.color.english_coral)
                                                    .copy(alpha = if (isLoading) 0.4f else 0.8f)
                                            )
                                        ),
                                        RoundedCornerShape(20.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Start",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Let's Start Playing!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun ReadyScreenPreview() {
    QuizAppTheme {
        ReadyScreen()
    }
}