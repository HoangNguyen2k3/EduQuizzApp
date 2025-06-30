package com.example.eduquizz.features.quizzGame.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eduquizz.R
import com.example.eduquizz.features.home.screens.WithLoading
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.eduquizz.navigation.Routes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroScreen(
    navController: NavController,
    onBackPressed: () -> Unit,
    loadingViewModel: LoadingViewModel = viewModel()
) {
    val loadingState by loadingViewModel.loadingState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadingViewModel.showLoading("Đang tải Quiz Game...", showProgress = true)

        loadingViewModel.updateProgress(0.2f, "Đang tải câu hỏi...")
        delay(800)

        loadingViewModel.updateProgress(0.5f, "Đang chuẩn bị nội dung...")
        delay(800)

        loadingViewModel.updateProgress(0.8f, "Đang khởi tạo game...")
        delay(600)

        loadingViewModel.updateProgress(1.0f, "Hoàn thành!")
        delay(400)

        loadingViewModel.hideLoading()
        isDataLoaded = true
        delay(300)
        isVisible = true
    }

    WithLoading(
        isLoading = loadingState.isLoading,
        isDarkTheme = false,
        backgroundColors = listOf(
            Color(0xFF096A5A),
            Color(0xFF44A08D),
            Color(0xFF4ECDC4),
            MaterialTheme.colorScheme.background
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF096A5A),
                            Color(0xFF44A08D),
                            Color(0xFF4ECDC4),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = onBackPressed) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBackIosNew,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                containerColor = Color.Transparent,
                bottomBar = {
                    AnimatedVisibility(
                        visible = isVisible && isDataLoaded,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(800, delayMillis = 1000, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 1000))
                    ) {
                        PlayButton(
                            onClick = { navController.navigate(Routes.MAIN) },
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Game Banner
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(800, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(800))
                        ) {
                            GameBanner()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Game Title
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(
                                    800,
                                    delayMillis = 200,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
                        ) {
                            GameTitle()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Game Description
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(
                                    800,
                                    delayMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
                        ) {
                            GameDescription()
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Sample Images
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(
                                    800,
                                    delayMillis = 600,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
                        ) {
                            SampleImagesSection(sampleImages = listOf(R.drawable.image, R.drawable.image, R.drawable.image))
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GameBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.quizzbanner),
            contentDescription = "Banner Game",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun GameTitle() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fun Quiz Game",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun GameDescription() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
        Text(
            text = "Một hành trình khám phá kiến thức dành cho mọi lứa tuổi. Từ câu hỏi đơn giản đến hóc búa, trò chơi giúp bạn rèn luyện tư duy và tăng vốn hiểu biết mỗi ngày.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            color = Color(0xFF1A237E),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun SampleImagesSection(sampleImages: List<Int>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Preview",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            modifier = Modifier.height(250.dp)
        ) {
            items(sampleImages) { image ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .fillMaxHeight()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Image(
                        painter = painterResource(id = image),
                        contentDescription = "Ảnh mẫu",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEC407A)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "START GAME",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color.White
        )
    }
}
