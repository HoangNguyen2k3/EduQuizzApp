package com.example.eduquizz.features.wordsearch.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eduquizz.features.home.screens.WithLoading
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.eduquizz.features.wordsearch.components.GameDescriptionCard
import com.example.eduquizz.features.wordsearch.components.GamePreviewCard
import com.example.eduquizz.features.wordsearch.components.StatisticsRow
import com.example.wordsearch.ui.theme.Primary
import com.example.wordsearch.ui.theme.WordSearchGameTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroductionScreen(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean = false,
    loadingViewModel: LoadingViewModel = viewModel()
) {
    val context = LocalContext.current
    val loadingState by loadingViewModel.loadingState.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadingViewModel.showLoading("Đang tải Word Search...", showProgress = true)

        loadingViewModel.updateProgress(0.2f, "Đang tải từ vựng...")
        delay(800)

        loadingViewModel.updateProgress(0.5f, "Đang tạo bảng chữ...")
        delay(800)

        loadingViewModel.updateProgress(0.8f, "Đang chuẩn bị game...")
        delay(600)

        loadingViewModel.updateProgress(1.0f, "Hoàn thành!")
        delay(400)

        loadingViewModel.hideLoading()
        isDataLoaded = true

        delay(300)
        isVisible = true


        delay(300)
        isVisible = true
    }

    WithLoading(
        isLoading = loadingState.isLoading,
        isDarkTheme = false,
        backgroundColors = listOf(
            Color(0xFF4A85F5),
            Color(0xFF7B61FF),
            MaterialTheme.colorScheme.background
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A85F5),
                            Color(0xFF7B61FF),
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
                            onClick = onPlayClicked,
                            showContinueButton = showContinueButton,
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

                        //Game Preview Card
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(800, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(800))
                        ) {
                            GamePreviewCard()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        //Game title
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
                        //Statistics Row
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
                            StatisticsRow()
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        //Description Section
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
                            GameDescriptionCard()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
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
fun IntroductionScreenSimple(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean = false
){
    var isLoading by remember {mutableStateOf(true)}
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2500) // Simulate data loading
        isLoading = false
        delay(300)
        isVisible = true
    }

    WithLoading(
        isLoading = isLoading,
        isDarkTheme = false
    ) {
        // Original IntroductionScreen content goes here
        IntroductionScreenContent(
            onPlayClicked = onPlayClicked,
            onBackPressed = onBackPressed,
            showContinueButton = showContinueButton,
            isVisible = isVisible
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntroductionScreenContent(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean,
    isVisible: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A85F5),
                        Color(0xFF7B61FF),
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
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(800, delayMillis = 1000, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(800, delayMillis = 1000))
                ) {
                    PlayButton(
                        onClick = onPlayClicked,
                        showContinueButton = showContinueButton,
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

                    //Game Preview Card
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(800, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(800))
                    ) {
                        GamePreviewCard()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    //Game title
                    AnimatedVisibility(
                        visible = isVisible,
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
                    //Statistics Row
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(
                                800,
                                delayMillis = 400,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
                    ) {
                        StatisticsRow()
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    //Description Section
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(
                                800,
                                delayMillis = 600,
                                easing = FastOutSlowInEasing
                            )
                        ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
                    ) {
                        GameDescriptionCard()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}


@Composable
private fun GameTitle() {
    Text(
        text = "Word Search Game",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        ),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}



@Composable
private fun PlayButton(
    onClick: () -> Unit,
    showContinueButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary
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
            text = if (showContinueButton) "CONTINUE" else "PLAY",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun IntroductionScreenPreview() {
    WordSearchGameTheme {
        IntroductionScreen(
            onPlayClicked = {},
            onBackPressed = {},
            showContinueButton = false
        )
    }
}