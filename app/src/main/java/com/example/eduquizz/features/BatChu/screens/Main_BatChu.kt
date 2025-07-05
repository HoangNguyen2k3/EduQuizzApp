package com.example.eduquizz.features.BatChu.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eduquizz.features.BatChu.model.DataBatChu
import com.example.eduquizz.features.BatChu.viewmodel.ViewModelBatChu
import com.example.quizapp.ui.theme.QuizAppTheme
import com.example.wordsearch.model.Cell
import kotlin.math.sqrt

val CardBackground = Color(0xFFE3F2FD)
val ButtonPrimary = Color(0xFF1976D2)
val CellBackground = Color.White
val SelectedCell = Color(0xFFBBDEFB)

@Composable
fun Main_BatChu(viewModelBatChu: ViewModelBatChu = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val question = viewModelBatChu.sampleQuestions[0]
    val answerLength = question.answer.length

    val selectedLetters = remember { mutableStateListOf<Char?>(*Array(answerLength) { null }) } //Chứa những từ được chọn đã đc đưa lên
    val usedIndices = remember { mutableStateListOf<Pair<Int, Char>>() } //chứa những từ được chọn để link giữa bên trên và bên dưới

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        ImageComponent(question)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            selectedLetters.forEachIndexed { index, letter ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
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

        Spacer(modifier = Modifier.height(20.dp))

        val userAnswer = selectedLetters.joinToString("") { it?.toString() ?: "" }
        if (userAnswer.length == answerLength) {
            Text(
                text = if (userAnswer == question.answer) "✅ ĐÚNG RỒI!" else "❌ SAI RỒI!",
                color = if (userAnswer == question.answer) Color(0xFF2E7D32) else Color.Red,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
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
            .clickable(onClick = onCellSelected)
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
    val gridSize = sqrt(grid.size.toFloat()).toInt()
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
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
                    onCellSelected = { onCellSelected(cell) }
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun Intro_Preview() {
    QuizAppTheme {
        Main_BatChu()
    }
}