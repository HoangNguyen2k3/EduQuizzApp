package com.example.eduquizz.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import org.json.JSONObject
import java.security.SecureRandom

object PlayIntegrityHelper {

    private const val PROJECT_NUMBER = 177486006662L

    fun checkIntegrity(context: Context, onResult: (Boolean, JSONObject?) -> Unit) {

        val manager = IntegrityManagerFactory.create(context)

        val nonceBytes = ByteArray(32)
        SecureRandom().nextBytes(nonceBytes)

        val nonceBase64 = Base64.encodeToString(
            nonceBytes,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(PROJECT_NUMBER)
            .setNonce(nonceBase64)
            .build()

        manager.requestIntegrityToken(request)
            .addOnSuccessListener { response ->
                val token = response.token()
                val payload = decodeJwsPayload(token)

                val passed = verifyPayload(payload)
                onResult(passed, payload)
            }
            .addOnFailureListener { e ->
                Log.e("Integrity", "FAILED: ${e.message}")
                onResult(false, null)
            }

    }

    /** Giải mã JSON từ JWS dạng header.payload.signature */
    private fun decodeJwsPayload(jws: String?): JSONObject? {
        return try {
            if (jws == null) return null

            val parts = jws.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payload)

        } catch (e: Exception) {
            Log.e("Integrity", "Decode error: ${e.message}")
            null
        }
    }

    /** Kiểm tra Integrity */
    private fun verifyPayload(json: JSONObject?): Boolean {
        if (json == null) return false

        return try {
            val appIntegrity = json.getJSONObject("appIntegrity")
            val deviceIntegrity = json.getJSONObject("deviceIntegrity")

            val verdict = appIntegrity.getString("appRecognitionVerdict")
            val deviceVerdicts = deviceIntegrity.getJSONArray("deviceRecognitionVerdict")

            val isAppOk = verdict == "PLAY_RECOGNIZED"
            val isDeviceOk = (0 until deviceVerdicts.length())
                .map { deviceVerdicts.getString(it) }
                .contains("MEETS_DEVICE_INTEGRITY")

            isAppOk && isDeviceOk

        } catch (e: Exception) {
            Log.e("Integrity", "Verify error: ${e.message}")
            false
        }
    }
}
