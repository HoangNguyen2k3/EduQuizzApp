package com.example.eduquizz.features.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import android.content.Context
import android.content.SharedPreferences

enum class MoodState {
    SAD, NORMAL, HAPPY
}

data class DayMoodRecord(
    val date: Date,
    val mood: MoodState,
    val timeSpent: Long
)

class MoodDataManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mood_tracker", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun saveSessionData(mood: MoodState, timeSpent: Long) {
        val today = dateFormat.format(Date())
        val existingTime = prefs.getLong("time_$today", 0)

        prefs.edit().apply {
            putLong("time_$today", existingTime + timeSpent)
            putString("mood_$today", mood.name)
            apply()
        }
    }

    fun getLast7DaysData(): List<DayMoodRecord> {
        val calendar = Calendar.getInstance()
        val records = mutableListOf<DayMoodRecord>()

        // Bắt đầu từ hôm qua (không tính hôm nay)
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        for (i in 0 until 7) {
            val dateStr = dateFormat.format(calendar.time)
            val timeSpent = prefs.getLong("time_$dateStr", 0)
            val moodStr = prefs.getString("mood_$dateStr", null)

            // Chỉ thêm nếu có dữ liệu
            if (timeSpent > 0 && moodStr != null) {
                records.add(
                    DayMoodRecord(
                        date = calendar.time.clone() as Date,
                        mood = MoodState.valueOf(moodStr),
                        timeSpent = timeSpent
                    )
                )
            }

            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return records
    }

    fun updateTodayTime(timeSpent: Long) {
        val today = dateFormat.format(Date())
        prefs.edit().apply {
            putLong("time_$today", timeSpent)
            apply()
        }
    }

    fun updateTodayMood(mood: MoodState) {
        val today = dateFormat.format(Date())
        prefs.edit().apply {
            putString("mood_$today", mood.name)
            apply()
        }
    }

    fun getTodayData(): Pair<MoodState, Long> {
        val today = dateFormat.format(Date())
        val timeSpent = prefs.getLong("time_$today", 0)
        val moodStr = prefs.getString("mood_$today", MoodState.SAD.name)
        return Pair(MoodState.valueOf(moodStr ?: MoodState.SAD.name), timeSpent)
    }
}

@Composable
fun FloatingMoodButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dataManager = remember { MoodDataManager(context) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showDialog by remember { mutableStateOf(false) }
    var sessionStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentMood by remember { mutableStateOf(MoodState.SAD) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Tải dữ liệu hôm nay khi khởi động
    LaunchedEffect(Unit) {
        val (savedMood, savedTime) = dataManager.getTodayData()
        currentMood = savedMood
        elapsedSeconds = savedTime
        sessionStartTime = System.currentTimeMillis() - (savedTime * 1000)
    }

    // Theo dõi thời gian sử dụng app
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Cập nhật mỗi giây
            elapsedSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000
            val elapsedMinutes = elapsedSeconds / 60

            val newMood = when {
                elapsedMinutes < 2 -> MoodState.SAD
                elapsedMinutes < 5 -> MoodState.NORMAL
                else -> MoodState.HAPPY
            }

            if (newMood != currentMood) {
                currentMood = newMood
                dataManager.updateTodayMood(newMood)
            }

            // Lưu thời gian mỗi 10 giây
            if (elapsedSeconds % 10 == 0L) {
                dataManager.updateTodayTime(elapsedSeconds)
            }
        }
    }

    // Lưu dữ liệu khi component bị dispose
    DisposableEffect(Unit) {
        onDispose {
            dataManager.updateTodayTime(elapsedSeconds)
            dataManager.updateTodayMood(currentMood)
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val buttonSize = with(density) { 60.dp.toPx() }

    // Khởi tạo vị trí ban đầu ở góc phải dưới
    LaunchedEffect(Unit) {
        offsetX = screenWidth - buttonSize - 16f
        offsetY = screenHeight - buttonSize - 100f
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Nút floating
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                }
                .size(60.dp)
                .zIndex(10f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // Snap to nearest edge
                            val centerX = offsetX + buttonSize / 2
                            val centerY = offsetY + buttonSize / 2

                            // Xác định cạnh gần nhất
                            val distanceToLeft = centerX
                            val distanceToRight = screenWidth - centerX
                            val distanceToTop = centerY
                            val distanceToBottom = screenHeight - centerY

                            val minDistance = minOf(
                                distanceToLeft,
                                distanceToRight,
                                distanceToTop,
                                distanceToBottom
                            )

                            when (minDistance) {
                                distanceToLeft -> offsetX = 16f
                                distanceToRight -> offsetX = screenWidth - buttonSize - 16f
                                distanceToTop -> offsetY = 16f
                                distanceToBottom -> offsetY = screenHeight - buttonSize - 16f
                            }

                            // Giới hạn trong màn hình
                            offsetX = offsetX.coerceIn(16f, screenWidth - buttonSize - 16f)
                            offsetY = offsetY.coerceIn(16f, screenHeight - buttonSize - 16f)
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxSize(),
                containerColor = when (currentMood) {
                    MoodState.SAD -> Color(0xFFFF6B6B)
                    MoodState.NORMAL -> Color(0xFFFFD93D)
                    MoodState.HAPPY -> Color(0xFF6BCF7F)
                },
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Text(
                    text = when (currentMood) {
                        MoodState.SAD -> "😢"
                        MoodState.NORMAL -> "😐"
                        MoodState.HAPPY -> "😊"
                    },
                    fontSize = 32.sp
                )
            }
        }

        // Dialog hiển thị lịch sử
        if (showDialog) {
            MoodHistoryDialog(
                currentMood = currentMood,
                currentTimeSpent = elapsedSeconds,
                dataManager = dataManager,
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
private fun MoodHistoryDialog(
    currentMood: MoodState,
    currentTimeSpent: Long,
    dataManager: MoodDataManager,
    onDismiss: () -> Unit
) {
    // Lấy dữ liệu thực từ SharedPreferences
    val moodHistory = remember { dataManager.getLast7DaysData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .zIndex(20f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trạng thái 7 ngày",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Current mood (Hôm nay)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (currentMood) {
                            MoodState.SAD -> Color(0xFFFFEBEE)
                            MoodState.NORMAL -> Color(0xFFFFF9E6)
                            MoodState.HAPPY -> Color(0xFFE8F5E9)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentMood) {
                                MoodState.SAD -> "😢"
                                MoodState.NORMAL -> "😐"
                                MoodState.HAPPY -> "😊"
                            },
                            fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Hôm nay",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = when (currentMood) {
                                    MoodState.SAD -> "Cần cố gắng thêm"
                                    MoodState.NORMAL -> "Đang tiến bộ"
                                    MoodState.HAPPY -> "Xuất sắc!"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                text = "${currentTimeSpent / 60} phút ${currentTimeSpent % 60} giây",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // History list header
                Text(
                    text = if (moodHistory.isNotEmpty()) "Lịch sử (${moodHistory.size} ngày)" else "Chưa có lịch sử",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable history
                if (moodHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        moodHistory.forEach { record ->
                            MoodHistoryItem(record)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sử dụng app thêm để ghi nhận\nlịch sử trạng thái của bạn! 💪",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodHistoryItem(record: DayMoodRecord) {
    val dateFormat = SimpleDateFormat("dd/MM (EEE)", Locale("vi", "VN"))
    val calendar = Calendar.getInstance()
    calendar.time = record.date

    val today = Calendar.getInstance()
    today.add(Calendar.DAY_OF_YEAR, 0)

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    val displayDate = when {
        calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
            "Hôm qua (${dateFormat.format(record.date)})"
        else -> dateFormat.format(record.date)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (record.mood) {
                        MoodState.SAD -> "😢"
                        MoodState.NORMAL -> "😐"
                        MoodState.HAPPY -> "😊"
                    },
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = displayDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    Text(
                        text = "${record.timeSpent / 60} phút ${record.timeSpent % 60} giây",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        when (record.mood) {
                            MoodState.SAD -> Color(0xFFFF6B6B)
                            MoodState.NORMAL -> Color(0xFFFFD93D)
                            MoodState.HAPPY -> Color(0xFF6BCF7F)
                        }
                    )
            )
        }
    }
}