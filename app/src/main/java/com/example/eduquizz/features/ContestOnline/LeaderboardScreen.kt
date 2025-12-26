package com.example.eduquizz.features.ContestOnline

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class LeaderboardEntry(
    val name: String = "",
    val score: Int = 0,
    val date: String = "",
    val timestamp: Long = 0
)

/**
 * Premium Leaderboard Screen với UI hiện đại
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit = {}
) {
    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Load data
    fun loadData() {
        loading = true
        error = null
    }

    LaunchedEffect(Unit) {
        try {
            val ref = FirebaseDatabase.getInstance().getReference("Contest/Leaderboard")
            val snapshot = ref.get().await()

            val allEntries = snapshot.children.mapNotNull { snap ->
                snap.getValue(LeaderboardEntry::class.java)
            }

            leaderboard = allEntries
                .filter { it.date == today }
                .sortedByDescending { it.score }
                .take(10)

        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFF6B73FF)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Custom Top Bar
            LeaderboardTopBar(
                onBackClick = onBackClick,
                onRefresh = { loadData() }
            )

            // Header Section
            LeaderboardHeader()

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    loading -> {
                        LoadingState()
                    }

                    error != null -> {
                        ErrorState(error = error!!)
                    }

                    leaderboard.isEmpty() -> {
                        EmptyState()
                    }

                    else -> {
                        LeaderboardList(entries = leaderboard)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardTopBar(
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun LeaderboardHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Trophy icon with animation
        Text(
            "🏆",
            fontSize = 64.sp,
            modifier = Modifier.scale(scale)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Bảng Xếp Hạng",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Date badge
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "Hôm nay",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF667eea),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    "Đang tải bảng xếp hạng...",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ErrorState(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("❌", fontSize = 48.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Lỗi tải dữ liệu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    error,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎮", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Chưa có ai chơi hôm nay!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Hãy là người đầu tiên chinh phục\nbảng xếp hạng!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LeaderboardList(entries: List<LeaderboardEntry>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top 3 Podium (if available)
        if (entries.size >= 3) {
            item {
                TopThreePodium(entries.take(3))
                Spacer(Modifier.height(16.dp))
            }
        }

        // Rest of the list
        val startIndex = if (entries.size >= 3) 3 else 0
        itemsIndexed(entries.drop(startIndex)) { index, entry ->
            val actualRank = startIndex + index + 1
            AnimatedLeaderboardItem(
                rank = actualRank,
                entry = entry,
                animationDelay = index * 100
            )
        }
    }
}

@Composable
fun TopThreePodium(topThree: List<LeaderboardEntry>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🏅 TOP 3 🏅",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667eea)
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd place
                if (topThree.size > 1) {
                    PodiumItem(
                        rank = 2,
                        entry = topThree[1],
                        height = 100.dp,
                        color = Color(0xFFC0C0C0)
                    )
                }

                // 1st place (highest)
                PodiumItem(
                    rank = 1,
                    entry = topThree[0],
                    height = 130.dp,
                    color = Color(0xFFFFD700)
                )

                // 3rd place
                if (topThree.size > 2) {
                    PodiumItem(
                        rank = 3,
                        entry = topThree[2],
                        height = 80.dp,
                        color = Color(0xFFCD7F32)
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumItem(
    rank: Int,
    entry: LeaderboardEntry,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "podium$rank")
    val crownScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (rank == 1) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crownScale$rank"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Crown for 1st place
        if (rank == 1) {
            Text(
                "👑",
                fontSize = 28.sp,
                modifier = Modifier.scale(crownScale)
            )
        }

        // Avatar circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.name.take(1).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(8.dp))

        // Name
        Text(
            entry.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E),
            maxLines = 1
        )

        // Score
        Text(
            "${entry.score} điểm",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        // Podium block
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    else -> "🥉"
                },
                fontSize = 32.sp
            )
        }
    }
}

@Composable
fun AnimatedLeaderboardItem(
    rank: Int,
    entry: LeaderboardEntry,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { it }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFF667eea).copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "#$rank",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF667eea)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        entry.name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        entry.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        "Tham gia hôm nay",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Score
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${entry.score}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}
