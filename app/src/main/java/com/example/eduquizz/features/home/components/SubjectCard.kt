package com.example.quizapp.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eduquizz.R
import com.example.eduquizz.data.models.Subject
import com.example.eduquizz.features.ContestOnline.ContestPrefs
import com.example.eduquizz.navigation.Routes
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onJoinContest: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.subject_card_height))
            .clickable {
                if (subject.id == "contest") {
                    val state = calculateContestState(21, 60)
                    val hasJoined = ContestPrefs.hasJoinedToday(context)

                    when {
                        hasJoined -> {
                            Toast.makeText(context, "Bạn đã tham gia hôm nay rồi!", Toast.LENGTH_SHORT).show()
                            onClick()
                        }
                        state is ContestState.Running -> {
                            onClick()
                            //ContestPrefs.saveJoinDate(context)
                        }
                        state is ContestState.Waiting -> Toast.makeText(context, "Cuộc thi chưa bắt đầu!", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(context, "Cuộc thi đã kết thúc, hẹn bạn ngày mai!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    onClick()
                }
            },
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.subject_card_corner)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimensionResource(id = R.dimen.subject_card_elevation)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = subject.gradientColors,
                        startX = 0f,
                        endX = 1000f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.spacing_xxl)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.icon_subject))
                        .background(
                            colorResource(id = R.color.white_20),
                            RoundedCornerShape(dimensionResource(id = R.dimen.corner_medium))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = subject.iconRes),
                        contentDescription = stringResource(id = R.string.subject_icon_desc),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.icon_large))
                    )
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_xxl)))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        color = Color.White,
                        fontSize = dimensionResource(id = R.dimen.text_large).value.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_normal)))

                    if (subject.id == "contest") {
                        ContestCountdown(
                            targetHour = 21,
                            durationMinutes = 60,
                            onJoinContest = onJoinContest
                        )
                    } else {
                        DefaultSubjectInfo(subject)
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultSubjectInfo(subject: Subject) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        InfoBadge(Icons.Default.Star, "${subject.progress}/${subject.totalQuestions}")
        Spacer(modifier = Modifier.width(8.dp))
        InfoBadge(Icons.Default.Edit, "${subject.completedQuestions}/${subject.totalLessons}")
    }
}

@Composable
fun InfoBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------- CONTEST ----------------

@Composable
fun ContestCountdown(
    targetHour: Int,
    durationMinutes: Int,
    onJoinContest: () -> Unit
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(calculateContestState(targetHour, durationMinutes)) }
    var hasJoined by remember { mutableStateOf(ContestPrefs.hasJoinedToday(context)) }

    // Cập nhật trạng thái liên tục
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            state = calculateContestState(targetHour, durationMinutes)

            // 🔁 Reset lại nếu qua ngày mới
            val stillJoined = ContestPrefs.hasJoinedToday(context)
            if (hasJoined && !stillJoined) {
                hasJoined = false
            }
        }
    }

    when (state) {
        is ContestState.Waiting -> {
            val remaining = (state as ContestState.Waiting).remaining
            InfoText("⏳ Bắt đầu sau: $remaining")
        }

        is ContestState.Running -> {
            val remaining = (state as ContestState.Running).remaining
            Column {
                Text("🎯 Cuộc thi đang diễn ra!", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Còn lại: $remaining", color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))
                if (hasJoined) {
                    DisabledButton("✅ Bạn đã tham gia hôm nay")
                } else {
                    ActiveButton("Tham gia ngay") {
                        onJoinContest()
                        //ContestPrefs.saveJoinDate(context)
                        hasJoined = true
                    }
                }
            }
        }

        is ContestState.Ended -> {
            InfoText("🏁 Cuộc thi đã kết thúc. Hẹn bạn ngày mai!")
        }
    }
}

@Composable
fun InfoText(text: String) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun ActiveButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DisabledButton(text: String) {
    Button(
        onClick = {},
        enabled = false,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.4f))
    ) {
        Text(text, color = Color.White)
    }
}

// ---------------- STATE + LOGIC ----------------

sealed class ContestState {
    data class Waiting(val remaining: String) : ContestState()
    data class Running(val remaining: String) : ContestState()
    object Ended : ContestState()
}

fun calculateContestState(targetHour: Int, durationMinutes: Int): ContestState {
    val now = Calendar.getInstance()
    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, targetHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        timeInMillis = start.timeInMillis + durationMinutes * 60 * 1000
    }

    return when {
        now.before(start) -> {
            ContestState.Waiting(formatMillis(start.timeInMillis - now.timeInMillis))
        }
        now.after(start) && now.before(end) -> {
            ContestState.Running(formatMillis(end.timeInMillis - now.timeInMillis))
        }
        else -> ContestState.Ended
    }
}

fun formatMillis(ms: Long): String {
    val totalSeconds = ms / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}

// ---------------- PREFS ----------------

/*object ContestPrefs {
    private const val PREF_NAME = "contest_prefs"
    private const val KEY_LAST_JOIN_DATE = "last_join_date"

    private fun todayString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun saveJoinDate(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_JOIN_DATE, todayString()).apply()
    }

    fun hasJoinedToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_JOIN_DATE, null)
        return lastDate == todayString()
    }
}*/
