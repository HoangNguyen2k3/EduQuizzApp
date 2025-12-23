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

    // Keys cho các field nhạy cảm - TẤT CẢ đều được mã hóa
    object SecureKeys {
        val PLAYER_NAME = stringPreferencesKey("enc_player_name")
        val PLAYER_AGE = stringPreferencesKey("enc_player_age")
        val AVATAR_URI = stringPreferencesKey("enc_avatar_uri")
        val BIRTHDAY = stringPreferencesKey("enc_birthday")
        val EMAIL = stringPreferencesKey("enc_email")

        // GOLD - MÃ HÓA + CHECKSUM để chống hack
        val GOLD = stringPreferencesKey("enc_gold")
        val GOLD_CHECKSUM = stringPreferencesKey("gold_checksum")
        
        // Keys không nhạy cảm
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val FIRST_TIME = booleanPreferencesKey("first_time")
        val MUSIC = booleanPreferencesKey("music")
        val SFX = booleanPreferencesKey("sfx")
    }
    
    // Secret key cho HMAC checksum (trong production nên lưu ở server hoặc Keystore)
    private val checksumSecret = "EduQuizz_Gold_Secret_2024"

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

    // === GOLD - Encrypted + Checksum để chống hack ===
    
    /**
     * Tạo HMAC checksum cho gold value
     */
    private fun createGoldChecksum(gold: Int): String {
        try {
            val mac = javax.crypto.Mac.getInstance("HmacSHA256")
            val secretKeySpec = javax.crypto.spec.SecretKeySpec(
                checksumSecret.toByteArray(Charsets.UTF_8), 
                "HmacSHA256"
            )
            mac.init(secretKeySpec)
            val data = "$gold:EduQuizz:${context.packageName}".toByteArray(Charsets.UTF_8)
            val hash = mac.doFinal(data)
            return Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            return ""
        }
    }
    
    /**
     * Verify HMAC checksum cho gold
     */
    private fun verifyGoldChecksum(gold: Int, checksum: String): Boolean {
        if (checksum.isEmpty()) return false
        val expectedChecksum = createGoldChecksum(gold)
        return checksum == expectedChecksum
    }
    
    /**
     * Lưu gold (encrypted + checksum)
     * @param gold Số gold phải >= 0
     * @return true nếu lưu thành công
     */
    suspend fun saveGold(gold: Int): Boolean {
        // Validation: Gold không được âm
        if (gold < 0) {
            android.util.Log.w("SecureDataStore", "⚠️ Attempted to save negative gold: $gold")
            return false
        }
        
        // Validation: Gold không được quá lớn (chống overflow attack)
        if (gold > 999_999_999) {
            android.util.Log.w("SecureDataStore", "⚠️ Attempted to save excessive gold: $gold")
            return false
        }
        
        context.dataStore.edit { prefs ->
            prefs[SecureKeys.GOLD] = encrypt(gold.toString())
            prefs[SecureKeys.GOLD_CHECKSUM] = createGoldChecksum(gold)
        }
        return true
    }
    
    /**
     * Lấy gold (decrypted + verified)
     * Nếu checksum không hợp lệ → trả về 0 (reset gold)
     */
    val goldFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        val encryptedGold = prefs[SecureKeys.GOLD] ?: return@map 0
        val checksum = prefs[SecureKeys.GOLD_CHECKSUM] ?: return@map 0
        
        val decryptedGold = decrypt(encryptedGold).toIntOrNull() ?: 0
        
        // Verify checksum - nếu bị hack sẽ fail
        if (!verifyGoldChecksum(decryptedGold, checksum)) {
            android.util.Log.e("SecureDataStore", "🚨 GOLD TAMPERING DETECTED! Resetting to 0")
            return@map 0
        }
        
        decryptedGold
    }
    
    /**
     * Thêm gold an toàn
     * @param amount Số gold cần thêm (có thể âm để trừ)
     * @return Số gold mới sau khi thêm, hoặc -1 nếu thất bại
     */
    suspend fun addGold(amount: Int): Int {
        val currentGold = goldFlow.first()
        val newGold = currentGold + amount
        
        // Không cho phép gold âm
        if (newGold < 0) {
            android.util.Log.w("SecureDataStore", "⚠️ Insufficient gold: $currentGold + $amount = $newGold")
            return -1
        }
        
        return if (saveGold(newGold)) newGold else -1
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