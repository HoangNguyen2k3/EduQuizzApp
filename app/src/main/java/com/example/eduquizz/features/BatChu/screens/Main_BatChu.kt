package com.example.eduquizz.features.BatChu.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import com.example.eduquizz.R
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.eduquizz.data_save.DataViewModel
import com.example.eduquizz.features.BatChu.model.DataBatChu
import com.example.eduquizz.features.BatChu.viewmodel.ViewModelBatChu
import com.example.eduquizz.features.home.screens.WithLoading
import com.example.eduquizz.features.home.viewmodel.LoadingViewModel
import com.example.eduquizz.features.wordsearch.model.Cell
import com.example.eduquizz.navigation.Routes
import com.example.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import com.example.eduquizz.data_save.AudioManager
import androidx.compose.runtime.DisposableEffect

val CardBackground = Color(0xFFE3F2FD)
val ButtonPrimary = Color(0xFF1976D2)
val CellBackground = Color.White
val SelectedCell = Color(0xFFBBDEFB)
@OptIn(ExperimentalLayoutApi::class)


@Composable
fun Main_BatChu(navController: NavController,
                dataviewModel: DataViewModel = hiltViewModel(),
                currentLevel: String ="",
                loadingViewModel: LoadingViewModel = viewModel()
) {
    val viewModelBatChu: ViewModelBatChu = hiltViewModel()

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AudioManager.setBgmEnabled(true)
    }
    DisposableEffect(Unit) {
        onDispose {
            AudioManager.setBgmEnabled(false)
        }
    }
    var currentQuestionIndex by remember { mutableStateOf(0) }



    val gold by dataviewModel.gold.observeAsState(-1)
    val coins = viewModelBatChu.coins
    var showHintDialog by remember { mutableStateOf(false) }
    // var hintUsedForCurrentQuestion by remember { mutableStateOf(false) }

    // Reset coin ban đầu
    LaunchedEffect(gold) {
        if (gold > -1 && coins.value == -1) {
            coins.value = gold
        }
    }
    val loadingState by loadingViewModel.loadingState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }
    var isDataLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        viewModelBatChu.Init(dataviewModel)
        viewModelBatChu.loadLevel(currentLevel)

        loadingViewModel.showLoading("Đang tải Quiz Game...", showProgress = true)

        loadingViewModel.updateProgress(0.2f, "Đang tải câu hỏi...")
        delay(600)

        loadingViewModel.updateProgress(0.5f, "Đang chuẩn bị nội dung...")
        delay(600)

        loadingViewModel.updateProgress(0.8f, "Đang khởi tạo game...")
        delay(400)

        loadingViewModel.updateProgress(1.0f, "Hoàn thành!")
        delay(200)

        loadingViewModel.hideLoading()
        isDataLoaded = true
        delay(100)
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
    ){

    }
    if(isDataLoaded == false){
        return
    }
    if (viewModelBatChu.questionList.isEmpty()) {
        return
    }
    val question = viewModelBatChu.questionList[currentQuestionIndex]
    val answerLength = question.answer.length

    //Dữ liệu đưa vào màn hình kết quả
    val num_question = viewModelBatChu.questionList.size
    var num_question_correct by remember { mutableStateOf(0) }

    val selectedLetters = remember(question) {
        mutableStateListOf<Char?>(*Array(question.answer.length) { null })
    }

    val usedIndices = remember(question) { mutableStateListOf<Pair<Int, Char>>() }

    var hintUsedForCurrentQuestion by remember(question) { mutableStateOf(false) }

    /*    // Reset khi đổi câu
        LaunchedEffect(question) {
            hintUsedForCurrentQuestion = false
            selectedLetters.clear()
            usedIndices.clear()
            repeat(answerLength) {
                selectedLetters.add(null)
            }
        }*/

    // Nội dung UI
    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFF99BA),
                Color(0xFFFFB8C2),
                Color(0xFFFDCEC6),
                MaterialTheme.colorScheme.background
            )
        )
    )) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Booster(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    coins = coins.value,
                    onUseHint = {
                        if (!hintUsedForCurrentQuestion) {
                            viewModelBatChu.spendCoins(5)
                            hintUsedForCurrentQuestion = true
                        }
                        showHintDialog = true
                    },
                    onSkip = {
                        if (currentQuestionIndex < viewModelBatChu.questionList.lastIndex) {
                            currentQuestionIndex++
                        } else {
                            Toast.makeText(context, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
                            navController.navigate("result/$num_question_correct/$num_question/${Routes.LevelBatChu}/${Routes.IntroBatChu}")
                        }
                    },
                    onAutoSuggest = {
                        if (selectedLetters.contains(null)) {
                            viewModelBatChu.autoSuggestLetter(
                                selectedLetters = selectedLetters,
                                usedIndices = usedIndices,
                                question = question
                            )
                            viewModelBatChu.spendCoins(8)
                            hintUsedForCurrentQuestion = true
                        }
                    }
                )
            },
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Câu ${currentQuestionIndex + 1} / ${viewModelBatChu.questionList.size}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = question.question ?: "", // nếu bạn có title câu hỏi
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(modifier = Modifier.height(9.dp))
                ImageComponent(question)
                Spacer(modifier = Modifier.height(15.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedLetters.forEachIndexed { index, letter ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.LightGray)
                                .clickable {
                                    if (letter != null) {
                                        val indexInUsed = usedIndices.indexOfFirst {
                                            it.second == letter && question.shuffledLetters[it.first] == letter
                                        }
                                        if (indexInUsed != -1) {
                                            usedIndices.removeAt(indexInUsed)
                                            selectedLetters[index] = null
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = letter?.toString() ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ModernWordGrid(
                    grid = question.shuffledLetters.mapIndexed { index, char ->
                        Cell(0, index, char, false)
                    },
                    usedIndices = usedIndices.map { it.first },
                    onCellSelected = { cell ->
                        if (usedIndices.any { it.first == cell.col }) return@ModernWordGrid
                        val emptyIndex = selectedLetters.indexOfFirst { it == null }
                        if (emptyIndex != -1) {
                            selectedLetters[emptyIndex] = cell.char
                            usedIndices.add(cell.col to cell.char)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(7.dp))

                val userAnswer = selectedLetters.joinToString("") { it?.toString() ?: "" }
                if (userAnswer.length == answerLength) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (userAnswer == question.answer) "" else "❌ SAI RỒI!",
                                color = if (userAnswer == question.answer) Color(0xFF2E7D32) else Color.Red,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            var showCorrectDialog by remember { mutableStateOf(false) }

                            val userAnswer = selectedLetters.joinToString("") { it?.toString() ?: "" }

// Khi trả lời đúng, bật Dialog
                            if (userAnswer.length == answerLength && userAnswer == question.answer) {
                                LaunchedEffect(userAnswer) {
                                    showCorrectDialog = true
                                }
                            }

// Hiển thị Dialog
                            if (showCorrectDialog) {
                                AlertDialog(
                                    onDismissRequest = { /* Không cho tắt ngoài */ },
                                    title = {
                                        Text("🎉 Đúng rồi!")
                                    },
                                    text = {
                                        Text("Bạn đã trả lời đúng câu hỏi. Nhấn Next để tiếp tục.")
                                    },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            showCorrectDialog = false
                                            if (currentQuestionIndex < viewModelBatChu.questionList.lastIndex) {
                                                num_question_correct++
                                                currentQuestionIndex++
                                            } else {
                                                num_question_correct++
                                                Toast.makeText(context, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
                                                navController.navigate("result/$num_question_correct/$num_question/${Routes.LevelBatChu}/${Routes.IntroBatChu}")
                                            }
                                        }) {
                                            Text("Next")
                                        }
                                    }
                                )
                            }
                            /*                            if (userAnswer == question.answer) {
                                                            Spacer(modifier = Modifier.width(16.dp)) // tạo khoảng cách ngang giữa text và nút

                                                            Button(
                                                                onClick = {
                                                                    if (currentQuestionIndex < viewModelBatChu.questionList.lastIndex) {
                                                                        num_question_correct++
                                                                        currentQuestionIndex++
                                                                    } else {
                                                                        num_question_correct++
                                                                        Toast.makeText(context, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
                                                                        navController.navigate("result/$num_question_correct/$num_question/${Routes.LevelBatChu}/${Routes.IntroBatChu}")
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                                            ) {
                                                                Text("Next", color = Color.White)
                                                            }
                                                        }*/
                        }
                    }
                }

            }
        }
    }

    if (showHintDialog) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            title = { Text("Gợi ý") },
            text = { Text(question.suggestion ?: "Không có gợi ý.") },
            confirmButton = {
                TextButton(onClick = { showHintDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun ImageComponent(dataBatChu: DataBatChu) {
    val scrollState = rememberScrollState()
    val imageUrl = dataBatChu.imageUrl ?: ""

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Question Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ModernGridCell(cell: Cell, onCellSelected: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = CellBackground,
        animationSpec = tween(durationMillis = 300),
        label = "background_color_animation"
    )

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(4.dp)
            .size(36.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(onClick = {
                AudioManager.playClickSfx()
                onCellSelected()
            })
    ) {
        Text(text = cell.char.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ModernWordGrid(
    grid: List<Cell>,
    usedIndices: List<Int>,
    onCellSelected: (Cell) -> Unit
) {
    sqrt(grid.size.toFloat()).toInt()
    LazyVerticalGrid(
        //columns = GridCells.Fixed(gridSize),
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        items(grid.size) { index ->
            val cell = grid[index]
            if (usedIndices.contains(index)) {
                Box(modifier = Modifier
                    .padding(4.dp)
                    .size(36.dp))
            } else {
                ModernGridCell(
                    cell = cell,
                    onCellSelected = {
                        AudioManager.playClickSfx()
                        onCellSelected(cell)
                    }
                )
            }
        }
    }
}
@Composable
fun Booster(
    modifier: Modifier = Modifier,
    coins: Int,
    onUseHint: () -> Unit,
    onSkip: () -> Unit,
    onAutoSuggest: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.coinimg ),
                contentDescription = "Coin",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = coins.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Button(
                onClick = onUseHint,
                colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Xem gợi ý (5💰)")
            }

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.weight(1f)
            ) {
                Text("Bỏ qua")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onAutoSuggest,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Gợi ý 1 chữ đúng (8💰)")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun Intro_Preview() {
    QuizAppTheme {
        Main_BatChu(navController = rememberNavController())
    }
}