package com.example.eduquizz.features.widget

import android.content.Context
import java.util.Calendar

object StreakManager {
    private const val PREFS_NAME = "app_data"
    private const val KEY_STREAK = "user_streak"
    private const val KEY_LAST_ACTIVE = "last_active_date"

    /**
     * Cập nhật streak khi user mở app hoặc hoàn thành activity
     */
    fun updateStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0)

        val today = getTodayTimestamp()
        val yesterday = today - (24 * 60 * 60 * 1000)

        val newStreak = when {
            lastActive == 0L -> 1 // Lần đầu tiên sử dụng
            lastActive >= today -> currentStreak // Đã update hôm nay rồi
            lastActive >= yesterday -> currentStreak + 1 // Ngày liên tiếp
            else -> 1 // Streak bị broken, restart
        }

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .putLong(KEY_LAST_ACTIVE, today)
            .apply()

        // Cập nhật widget
        WidgetUpdateManager.updateStreak(context, newStreak)
    }

    /**
     * Lấy streak hiện tại (đã validate)
     */
    fun getCurrentStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = prefs.getInt(KEY_STREAK, 0)
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0)

        val today = getTodayTimestamp()
        val yesterday = today - (24 * 60 * 60 * 1000)

        // Validate: nếu lastActive quá xa thì reset streak
        return if (lastActive >= yesterday) streak else 0
    }

    /**
     * Thêm streak manually (dùng cho testing hoặc rewards)
     */
    fun addStreakBonus(context: Context, bonus: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentStreak = getCurrentStreak(context)
        val newStreak = currentStreak + bonus

        prefs.edit()
            .putInt(KEY_STREAK, newStreak)
            .apply()

        WidgetUpdateManager.updateStreak(context, newStreak)
    }

    /**
     * Reset streak về 0
     */
    fun resetStreak(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_STREAK, 0)
            .putLong(KEY_LAST_ACTIVE, 0)
            .apply()

        WidgetUpdateManager.updateStreak(context, 0)
    }

    /**
     * Lấy timestamp của ngày hôm nay (00:00:00)
     */
    private fun getTodayTimestamp(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /**
     * Check xem user có maintain streak hay không
     */
    fun hasActiveStreak(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0)

        val today = getTodayTimestamp()
        val yesterday = today - (24 * 60 * 60 * 1000)

        return lastActive >= yesterday
    }

    /**
     * Lấy thông tin chi tiết về streak
     */
    fun getStreakInfo(context: Context): StreakInfo {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = getCurrentStreak(context)
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0)
        val hasActiveStreak = hasActiveStreak(context)

        return StreakInfo(
            currentStreak = streak,
            lastActiveDate = lastActive,
            isActive = hasActiveStreak,
            needsUpdate = lastActive < getTodayTimestamp()
        )
    }
}

data class StreakInfo(
    val currentStreak: Int,
    val lastActiveDate: Long,
    val isActive: Boolean,
    val needsUpdate: Boolean
)