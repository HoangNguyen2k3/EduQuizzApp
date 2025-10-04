package com.example.eduquizz.data_save

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eduquizz.features.widget.StreakManager
import com.example.eduquizz.features.widget.WidgetUpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Integration example cho DataViewModel với Widget system
 * Thêm các methods này vào DataViewModel hiện tại của bạn
 */
@HiltViewModel
class DataViewModelWithWidget @Inject constructor(
    @ApplicationContext private val context: Context
    // ... existing dependencies
) : ViewModel() {

    // Existing properties
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    init {
        // Load streak khi khởi tạo
        loadStreak()
    }

    /**
     * Load streak từ SharedPreferences
     */
    private fun loadStreak() {
        viewModelScope.launch {
            val currentStreak = StreakManager.getCurrentStreak(context)
            _streak.value = currentStreak
        }
    }

    /**
     * Cập nhật streak khi user sử dụng app
     * Gọi method này trong MainActivity.onCreate()
     */
    fun updateDailyStreak() {
        viewModelScope.launch {
            StreakManager.updateStreak(context)
            val newStreak = StreakManager.getCurrentStreak(context)
            _streak.value = newStreak
        }
    }

    /**
     * Thêm bonus streak (ví dụ: hoàn thành challenges)
     */
    fun addStreakBonus(bonus: Int) {
        viewModelScope.launch {
            StreakManager.addStreakBonus(context, bonus)
            val newStreak = StreakManager.getCurrentStreak(context)
            _streak.value = newStreak
        }
    }

    /**
     * Gọi khi user hoàn thành một game/activity
     * Đảm bảo widget được update
     */
    fun onActivityCompleted(activityType: String) {
        viewModelScope.launch {
            // Update streak nếu cần
            if (StreakManager.getStreakInfo(context).needsUpdate) {
                updateDailyStreak()
            }

            // Update tất cả widgets
            WidgetUpdateManager.updateAllWidgets(context)

            // Log activity completion
            println("Activity completed: $activityType, Current streak: ${_streak.value}")
        }
    }

    /**
     * Reset streak (dùng cho testing hoặc user request)
     */
    fun resetStreak() {
        viewModelScope.launch {
            StreakManager.resetStreak(context)
            _streak.value = 0
        }
    }

    /**
     * Lấy thông tin chi tiết về streak
     */
    fun getStreakInfo() = StreakManager.getStreakInfo(context)
}

/**
 * Extension functions để dễ sử dụng trong Composables
 */

// Sử dụng trong MainActivity
fun DataViewModel.initializeWidgetSystem(context: Context) {
    // Update streak khi app được mở
    StreakManager.updateStreak(context)

    // Schedule periodic updates
    WidgetUpdateManager.scheduleWidgetUpdates(context)
}

// Sử dụng khi hoàn thành game
fun DataViewModel.notifyGameCompletion(context: Context, gameType: String) {
    // Update streak nếu cần
    val info = StreakManager.getStreakInfo(context)
    if (info.needsUpdate) {
        StreakManager.updateStreak(context)
    }

    // Update widgets
    WidgetUpdateManager.updateAllWidgets(context)
}

/**
 * Composable để hiển thị streak trong app
 */
@Composable
fun StreakDisplay(viewModel: DataViewModel) {
    val context = LocalContext.current
    val streakInfo = remember { StreakManager.getStreakInfo(context) }

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Current Streak",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (streakInfo.isActive) "Keep it up!" else "Start your streak today!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${streakInfo.currentStreak}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " 🔥",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}