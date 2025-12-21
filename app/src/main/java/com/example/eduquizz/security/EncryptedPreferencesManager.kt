package com.example.eduquizz.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferencesManager {

    private const val ENCRYPTED_PREFS_FILE = "secure_user_prefs"
    
    fun getEncryptedPreferences(context: Context): SharedPreferences {
        // Tạo hoặc lấy MasterKey
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Tạo EncryptedSharedPreferences
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Helper methods để lưu/đọc dữ liệu

    fun saveString(context: Context, key: String, value: String) {
        getEncryptedPreferences(context).edit().putString(key, value).apply()
    }

    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getEncryptedPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    fun saveLong(context: Context, key: String, value: Long) {
        getEncryptedPreferences(context).edit().putLong(key, value).apply()
    }

    fun getLong(context: Context, key: String, defaultValue: Long = 0L): Long {
        return getEncryptedPreferences(context).getLong(key, defaultValue)
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getEncryptedPreferences(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getEncryptedPreferences(context).getBoolean(key, defaultValue)
    }

    fun saveInt(context: Context, key: String, value: Int) {
        getEncryptedPreferences(context).edit().putInt(key, value).apply()
    }

    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getEncryptedPreferences(context).getInt(key, defaultValue)
    }

    fun remove(context: Context, key: String) {
        getEncryptedPreferences(context).edit().remove(key).apply()
    }

    fun clear(context: Context) {
        getEncryptedPreferences(context).edit().clear().apply()
    }
}