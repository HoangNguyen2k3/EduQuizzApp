package com.example.eduquizz.features.mapping.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.eduquizz.features.mapping.components.GeographyPreviewCard
import com.example.eduquizz.features.mapping.components.GeographyDescriptionCard
import com.example.eduquizz.features.mapping.components.GeographyStatisticsRow
import com.example.eduquizz.features.mapping.viewmodel.MappingViewModel
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingGamesIntroductionScreen(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean = false,
    loadingViewModel: LoadingViewModel = viewModel(),
    mappingViewModel: MappingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loadingState by loadingViewModel.loadingState.collectAsState()

    val countryCount by mappingViewModel.countryCount
    val continentCount by mappingViewModel.continentCount
    val totalQuestions by mappingViewModel.totalQuestions

    var isVisible by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            loadingViewModel.showLoading("Đang tải Geography Games...", showProgress = true)

            loadingViewModel.updateProgress(0.2f, "Đang tải bản đồ...")

            mappingViewModel.loadStatistics()
            delay(800)

            loadingViewModel.updateProgress(0.5f, "Đang chuẩn bị các quốc gia...")
            delay(800)

            loadingViewModel.updateProgress(0.8f, "Đang thiết lập game...")
            delay(600)

            loadingViewModel.updateProgress(1.0f, "Hoàn thành!")
            delay(400)

            loadingViewModel.hideLoading()
            isDataLoaded = true

            delay(300)
            isVisible = true
        } catch (e: Exception) {
            println("Error in MappingGamesIntroductionScreen LaunchedEffect: ${e.message}")
            e.printStackTrace()
            loadingViewModel.hideLoading()
            isDataLoaded = true
            isVisible = true
        }
    }

    WithLoading(
        isLoading = loadingState.isLoading,
        isDarkTheme = false,
        backgroundColors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF66BB6A),
            Color(0xFFE8F5E8)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF66BB6A),
                            Color(0xFFE8F5E8)
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

                        // Geography Preview Card
                        AnimatedVisibility(
                            visible = isVisible && isDataLoaded,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(800, easing = FastOutSlowInEasing)
                            ) + fadeIn(animationSpec = tween(800))
                        ) {
                            GeographyPreviewCard()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Game title
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
                        // Statistics Row with database values
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
                            GeographyStatisticsRow(
                                countryCount = countryCount,
                                continentCount = continentCount,
                                totalQuestions = totalQuestions
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Description Section
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
                            GeographyDescriptionCard()
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
fun MappingGamesIntroductionScreenSimple(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean = false,
    mappingViewModel: MappingViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        mappingViewModel.loadStatistics()
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
        MappingGamesIntroductionScreenContent(
            onPlayClicked = onPlayClicked,
            onBackPressed = onBackPressed,
            showContinueButton = showContinueButton,
            isVisible = isVisible,
            mappingViewModel = mappingViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingGamesIntroductionScreenContent(
    onPlayClicked: () -> Unit,
    onBackPressed: () -> Unit,
    showContinueButton: Boolean,
    isVisible: Boolean,
    mappingViewModel: MappingViewModel
) {
    // Get statistics from ViewModel
    val countryCount by mappingViewModel.countryCount
    val continentCount by mappingViewModel.continentCount
    val totalQuestions by mappingViewModel.totalQuestions

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF66BB6A),
                        Color(0xFFE8F5E8)
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

                    // Geography Preview Card
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(800, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(800))
                    ) {
                        GeographyPreviewCard()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    // Game title
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
                    // Statistics Row with database values
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
                        GeographyStatisticsRow(
                            countryCount = countryCount,
                            continentCount = continentCount,
                            totalQuestions = totalQuestions
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    // Description Section
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
                        GeographyDescriptionCard()
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
        text = "Mapping Games",
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
            containerColor = Color(0xFF2E7D32)
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
fun MappingGamesIntroductionScreenPreview() {
    QuizAppTheme {
        MappingGamesIntroductionScreen(
            onPlayClicked = {},
            onBackPressed = {},
            showContinueButton = false
        )
    }
}