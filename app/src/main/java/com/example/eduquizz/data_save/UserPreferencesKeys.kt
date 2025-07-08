package com.example.eduquizz.data_save

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    //Thông tin người chơi
    val PLAYER_NAME = stringPreferencesKey("player_name")
    val PLAYER_AGE = intPreferencesKey("player_age")
    val PLAYER_HOBBIES_SUBJECT = stringPreferencesKey("player_hobbies_subject")
    val GOLD = intPreferencesKey("gold")
    val CURRENT_LEVEL = intPreferencesKey("current_level")
    val FIRST_TIME = booleanPreferencesKey("firts_time")
    //Thống kê
    val NUM_TOTAL_QUESTION = intPreferencesKey("total_questionPlay")
    val NUM_CORRECT_ANS_QUESTION = intPreferencesKey("correct_ans_question")
    val NUM_CORRECT_ALL_QUESTION = intPreferencesKey("correcrt_all_question")
    val NUM_CORRECT_ABOVE_50_PERCENT_QUES = intPreferencesKey("correct_above_50_percent_question")
    val NUM_CORRECT_BELOW_50_PERCENT_QUES = intPreferencesKey("correct_below_50_percent_question")
    val BIRTHDAY = stringPreferencesKey("player_birthday")

    val music = booleanPreferencesKey("music")
    val sfx = booleanPreferencesKey("sfx")
}