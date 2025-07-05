package com.example.eduquizz.data_save

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val PLAYER_NAME = stringPreferencesKey("player_name")
    val GOLD = intPreferencesKey("gold")
    val CURRENT_LEVEL = intPreferencesKey("current_level")
    val FIRST_TIME = booleanPreferencesKey("firts_time")
}