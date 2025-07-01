package com.example.eduquizz.DataSave

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val PLAYER_NAME = stringPreferencesKey("player_name")
    val GOLD = intPreferencesKey("gold")
    val CURRENT_LEVEL = intPreferencesKey("current_level")
}