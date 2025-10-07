package com.example.eduquizz.features.ContestOnline

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object ContestPrefs {
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
}
