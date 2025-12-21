package com.example.eduquizz.data_save

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

/**
 * Manager để xử lý encrypted DataStore
 * Sử dụng AES-GCM để mã hóa dữ liệu nhạy cảm
 */
class SecureDataStoreManager(private val context: Context) {

    // DataStore thông thường cho dữ liệu không nhạy cảm
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_user_prefs")

    // Encryption utilities
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH = 128
    }

    // Keys cho các field nhạy cảm
    object SecureKeys {
        val PLAYER_NAME = stringPreferencesKey("enc_player_name")
        val PLAYER_AGE = stringPreferencesKey("enc_player_age")
        val AVATAR_URI = stringPreferencesKey("enc_avatar_uri")
        val BIRTHDAY = stringPreferencesKey("enc_birthday")
        val EMAIL = stringPreferencesKey("enc_email")

        // Keys không nhạy cảm - không cần mã hóa
        val GOLD = intPreferencesKey("gold")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val FIRST_TIME = booleanPreferencesKey("first_time")
        val MUSIC = booleanPreferencesKey("music")
        val SFX = booleanPreferencesKey("sfx")
    }

    /**
     * Mã hóa string
     */
    private fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        // Kết hợp IV + encrypted data
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Giải mã string
     */
    private fun decrypt(ciphertext: String): String {
        if (ciphertext.isEmpty()) return ""

        try {
            val combined = Base64.decode(ciphertext, Base64.NO_WRAP)

            // Tách IV và encrypted data
            val iv = combined.copyOfRange(0, 12) // GCM IV is 12 bytes
            val encrypted = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            // Nếu giải mã thất bại, trả về empty string
            return ""
        }
    }

    /**
     * Lấy secret key từ MasterKey
     */
    private fun getSecretKey(): SecretKeySpec {
        // Trong production, bạn nên lưu key này vào Android Keystore
        // Đây chỉ là ví dụ đơn giản
        val keyBytes = masterKey.toString().toByteArray().copyOf(32) // AES-256 needs 32 bytes
        return SecretKeySpec(keyBytes, "AES")
    }

    // === Encrypted Fields ===

    /**
     * Lưu player name (encrypted)
     */
    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.PLAYER_NAME] = encrypt(name)
        }
    }

    /**
     * Lấy player name (decrypted)
     */
    val playerNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.PLAYER_NAME]?.let { decrypt(it) } ?: ""
    }

    /**
     * Lưu player age (encrypted)
     */
    suspend fun savePlayerAge(age: Int) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.PLAYER_AGE] = encrypt(age.toString())
        }
    }

    /**
     * Lấy player age (decrypted)
     */
    val playerAgeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.PLAYER_AGE]?.let { decrypt(it).toIntOrNull() } ?: 0
    }

    /**
     * Lưu avatar URI (encrypted)
     */
    suspend fun saveAvatarUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.AVATAR_URI] = encrypt(uri)
        }
    }

    /**
     * Lấy avatar URI (decrypted)
     */
    val avatarUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.AVATAR_URI]?.let { decrypt(it) } ?: ""
    }

    /**
     * Lưu birthday (encrypted)
     */
    suspend fun saveBirthday(birthday: String) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.BIRTHDAY] = encrypt(birthday)
        }
    }

    /**
     * Lấy birthday (decrypted)
     */
    val birthdayFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.BIRTHDAY]?.let { decrypt(it) } ?: "01/01/2000"
    }

    // === Non-encrypted Fields (ví dụ cho gold, level, etc.) ===

    suspend fun saveGold(gold: Int) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.GOLD] = gold
        }
    }

    val goldFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.GOLD] ?: 0
    }

    suspend fun saveCurrentLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.CURRENT_LEVEL] = level
        }
    }

    val currentLevelFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.CURRENT_LEVEL] ?: 1
    }

    suspend fun saveFirstTime(firstTime: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.FIRST_TIME] = firstTime
        }
    }

    val firstTimeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.FIRST_TIME] ?: false
    }

    suspend fun saveMusic(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.MUSIC] = enabled
        }
    }

    val musicFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SecureKeys.MUSIC] ?: true
    }

    /**
     * Xóa tất cả dữ liệu
     */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

/**
 * Extension để inject vào ViewModel
 */
val Context.secureDataStore: SecureDataStoreManager
    get() = SecureDataStoreManager(this)