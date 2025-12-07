package com.example.eduquizz.features.dailyLogin.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduquizz.R
import com.example.eduquizz.features.dailyLogin.model.DailyLoginReward
import com.example.eduquizz.features.dailyLogin.viewmodel.DailyLoginViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyLoginScreen(
    onBackClick: () -> Unit,
    viewModel: DailyLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Get user ID - dùng helper function từ ViewModel
    val userId = remember {
        viewModel.getCurrentUserId()
    }

    // Load data on enter
    LaunchedEffect(userId) {
        android.util.Log.d("DailyLoginScreen", "🚀 [SCREEN] Screen opened, userId: $userId")
        android.util.Log.d("DailyLoginScreen", "🚀 [SCREEN] Loading daily login data...")
        viewModel.loadDailyLoginData(userId)
    }
    
    // Debug: Log UI state changes
    LaunchedEffect(uiState.canClaimToday, uiState.lastClaimedDay) {
        android.util.Log.d("DailyLoginScreen", "🔄 [SCREEN] UI State changed:")
        android.util.Log.d("DailyLoginScreen", "   - canClaimToday: ${uiState.canClaimToday}")
        android.util.Log.d("DailyLoginScreen", "   - lastClaimedDay: ${uiState.lastClaimedDay}")
        android.util.Log.d("DailyLoginScreen", "   - currentDay: ${uiState.currentDay}")
        android.util.Log.d("DailyLoginScreen", "   - isLoading: ${uiState.isLoading}")
        android.util.Log.d("DailyLoginScreen", "   - isClaiming: ${uiState.isClaiming}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đăng nhập hàng ngày",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.bg_very_light_gray),
                            colorResource(R.color.bg_light_gray),
                            colorResource(R.color.bg_darker_gray)
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "🎁 Phần thưởng đăng nhập",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.text_primary_dark)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Đăng nhập mỗi ngày để nhận vàng!",
                            fontSize = 14.sp,
                            color = colorResource(R.color.text_secondary_gray),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        val progress = uiState.lastClaimedDay / 7f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colorResource(R.color.english_red),
                            trackColor = colorResource(R.color.bg_light_gray)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Ngày ${uiState.lastClaimedDay}/7",
                            fontSize = 12.sp,
                            color = colorResource(R.color.text_secondary_gray)
                        )
                    }
                }

                // Rewards row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(uiState.rewards) { index, reward ->
                        val dayNumber = index + 1
                        val isCurrentDay = dayNumber == uiState.lastClaimedDay + 1 && uiState.canClaimToday
                        val isClaimed = reward.isClaimed
                        val isLocked = dayNumber > uiState.lastClaimedDay + 1 && !uiState.canClaimToday
                        val isClaiming = uiState.isClaiming && dayNumber == uiState.lastClaimedDay + 1
                        
                        android.util.Log.d("DailyLoginScreen", "🎴 [CARD] Day $dayNumber: isCurrentDay=$isCurrentDay, isClaimed=$isClaimed, isLocked=$isLocked, lastClaimedDay=${uiState.lastClaimedDay}, canClaim=${uiState.canClaimToday}")

                        RewardDayCard(
                            reward = reward,
                            isCurrentDay = isCurrentDay,
                            isClaimed = isClaimed,
                            isLocked = isLocked,
                            isClaiming = isClaiming,
                            onClick = {
                                android.util.Log.d("DailyLoginScreen", "👆 [CARD] User clicked on day $dayNumber card")
                                if (uiState.canClaimToday && !uiState.isClaiming) {
                                    android.util.Log.d("DailyLoginScreen", "✅ [CARD] Conditions met, calling claimTodayReward")
                                    viewModel.claimTodayReward(userId)
                                } else {
                                    android.util.Log.d("DailyLoginScreen", "❌ [CARD] Cannot claim: canClaimToday=${uiState.canClaimToday}, isClaiming=${uiState.isClaiming}")
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Nút Điểm danh
                when {
                    uiState.isClaiming -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = colorResource(R.color.english_red)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Đang xử lý...",
                                fontSize = 14.sp,
                                color = colorResource(R.color.text_secondary_gray)
                            )
                        }
                    }
                    uiState.canClaimToday -> {
                        // Nút Điểm danh nổi bật
                        Button(
                            onClick = {
                                android.util.Log.d("DailyLoginScreen", "🎯 User clicked Điểm danh button")
                                viewModel.claimTodayReward(userId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.english_red)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "ĐIỂM DANH",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = "Nhận ${(uiState.lastClaimedDay + 1) * 100} vàng ngay hôm nay!",
                            fontSize = 14.sp,
                            color = colorResource(R.color.text_secondary_gray),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(colorResource(R.color.bg_light_gray)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (uiState.lastClaimedDay == 7)
                                            "✅ Đã hoàn thành chu kỳ 7 ngày!"
                                        else
                                            "✅ Đã điểm danh hôm nay",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colorResource(R.color.text_secondary_gray)
                                    )
                                }
                            }
                        }
                    }
                }

                // Success Popup Dialog
                if (uiState.successMessage != null && uiState.claimedGold > 0) {
                    LaunchedEffect(uiState.claimedGold) {
                        delay(5000) // Tự động đóng sau 5 giây
                        viewModel.clearSuccess()
                    }
                    SuccessDialog(
                        message = uiState.successMessage!!,
                        goldAmount = uiState.claimedGold,
                        onDismiss = {
                            viewModel.clearSuccess()
                        }
                    )
                }

                // Error message
                uiState.errorMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        delay(3000)
                        viewModel.clearError()
                    }
                    ErrorSnackbar(msg)
                }
            }
        }
    }
}

@Composable
fun RewardDayCard(
    reward: DailyLoginReward,
    isCurrentDay: Boolean,
    isClaimed: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    isClaiming: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isCurrentDay && !isClaimed) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val borderColor = when {
        isClaimed -> Color(0xFF4CAF50)
        isCurrentDay -> colorResource(R.color.english_red)
        isLocked -> colorResource(R.color.bg_light_gray)
        else -> colorResource(R.color.bg_light_gray)
    }

    Column(
        modifier = Modifier
            .width(100.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isClaimed) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF4CAF50),
                            Color.White
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.White, Color.White))
                }
            )
            .border(
                width = if (isCurrentDay) 3.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isCurrentDay && !isClaimed && !isClaiming) { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Ngày ${reward.day}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLocked)
                colorResource(R.color.text_secondary_gray)
            else
                colorResource(R.color.text_primary_dark)
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isClaimed -> Color(0xFF4CAF50)
                        isCurrentDay -> colorResource(R.color.english_red).copy(alpha = 0.2f)
                        isLocked -> colorResource(R.color.bg_light_gray)
                        else -> colorResource(R.color.bg_very_light_gray)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLocked -> Icon(Icons.Default.Lock, null, tint = colorResource(R.color.text_secondary_gray), modifier = Modifier.size(24.dp))
                isClaimed -> Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                isClaiming -> CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorResource(R.color.english_red), strokeWidth = 2.dp)
                else -> Icon(painterResource(R.drawable.coinimg), null, tint = colorResource(R.color.english_red), modifier = Modifier.size(32.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${reward.goldReward}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isLocked) colorResource(R.color.text_secondary_gray) else colorResource(R.color.english_red)
        )

        Text(
            text = "vàng",
            fontSize = 10.sp,
            color = colorResource(R.color.text_secondary_gray)
        )
    }
}

@Composable
fun SuccessDialog(
    message: String,
    goldAmount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "🎉 Thành công!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Gold reward card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.coinimg),
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "+$goldAmount",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "vàng",
                                fontSize = 14.sp,
                                color = colorResource(R.color.text_secondary_gray)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Tuyệt vời!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun SuccessSnackbar(message: String, gold: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50) // Green
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (gold > 0) {
                    Text(
                        text = "+$gold vàng",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorSnackbar(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF44336) // Red
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

