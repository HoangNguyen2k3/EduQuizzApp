package com.example.eduquizz.features.ThongKe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.R
import com.example.eduquizz.data_save.DataViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt

import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThongKe(
    dataViewModel: DataViewModel = hiltViewModel()
) {
    val totalQuestions by dataViewModel.numTotalQuestions.observeAsState(0)
    val correctAnswers by dataViewModel.numCorrectAnsweredQuestions.observeAsState(0)
    val winAbove50 by dataViewModel.numCorrectAbove50Percent.observeAsState(0)
    val winBelow50 by dataViewModel.numCorrectBelow50Percent.observeAsState(0)
    val winPerfect by dataViewModel.numCorrectAllQuestions.observeAsState(0)

    remember(totalQuestions, correctAnswers) {
        if (totalQuestions == 0) 0f else correctAnswers.toFloat() / totalQuestions
    }
    val pieChartData = listOf(
        PieData(value = winAbove50, label = "Thắng >50%", color = Color(0xFFE53935)),
        PieData(value = winPerfect, label = "Thắng 100%", color = Color(0xFF43A047)),
        PieData(value = winBelow50, label = "Thắng <50%", color = Color(0xFF8E24AA))
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📊 Thống kê người chơi",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0D47A1),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        //AccuracyCircularChart(successRate)
        AccuracyPieChart(
            data = pieChartData,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            size = 220.dp
        )
        Spacer(modifier = Modifier.height(24.dp))

        StatCard("Tổng số câu đã chơi", "$totalQuestions câu", R.drawable.musicnote)
        StatCard("Tổng số câu đúng", "$correctAnswers câu", R.drawable.musicnote)
        StatCard("Lượt thắng > 50%", "$winAbove50 lần", R.drawable.musicnote)
        StatCard("Lượt thắng < 50%", "$winBelow50 lần", R.drawable.musicnote)
        StatCard("Lượt thắng hoàn hảo (100%)", "$winPerfect lần", R.drawable.musicnote)
    }
}
@Composable
fun AccuracyCircularChart(successRate: Float) {
    val percent = (successRate * 100).roundToInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .padding(8.dp)
    ) {
        CircularProgressIndicator(
            progress = successRate,
            strokeWidth = 12.dp,
            color = Color(0xFF43A047),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "$percent%",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
    }
}
@Composable
fun StatCard(title: String, value: String, iconResId: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = value, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
data class PieData(
    val value: Int,
    val label: String,
    val color: Color
)

@Composable
fun AccuracyPieChart(
    data: List<PieData>,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    textSize: TextUnit = 12.sp
) {
    val total = data.sumOf { it.value }
    val density = LocalDensity.current

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (total == 0) {
            // Vẽ vòng tròn xám khi không có dữ liệu
            Canvas(modifier = Modifier.size(size)) {
                drawArc(
                    color = Color.Gray,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.toPx(), size.toPx())
                )
            }

            Text(
                text = "Không có dữ liệu",
                color = Color.DarkGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            val sweepAngles = data.map { 360f * it.value / total }
            Canvas(modifier = Modifier.size(size)) {
                val centerX = size.toPx() / 2
                val centerY = size.toPx() / 2
                val radius = size.toPx() / 2
                var startAngle = -90f

                data.forEachIndexed { index, item ->
                    if (item.value == 0) return@forEachIndexed // Bỏ qua nếu 0%

                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngles[index],
                        useCenter = true,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.toPx(), size.toPx())
                    )

                    // Vị trí nhãn
                    val angle = startAngle + sweepAngles[index] / 2
                    val radians = Math.toRadians(angle.toDouble())
                    val labelX = centerX + radius * 0.6f * cos(radians).toFloat()
                    val labelY = centerY + radius * 0.6f * sin(radians).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                        with(density) {
                            paint.textSize = textSize.toPx()
                        }
                        drawText(item.label, labelX, labelY, paint)
                    }

                    startAngle += sweepAngles[index]
                }
            }
        }
    }
}


