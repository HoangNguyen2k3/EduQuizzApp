package com.example.eduquizz.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import org.json.JSONObject

object PlayIntegrityHelper {

    private const val API_KEY = "AIzaSyC54a169pl8U-L7TopgN3pnmgWGpPudm7c"
    private const val PROJECT_NUMBER = 177486006662

    fun checkIntegrity(context: Context, onResult: (Boolean, JSONObject?) -> Unit) {

        val manager = IntegrityManagerFactory.create(context)

        val nonce = generateNonce()

        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(PROJECT_NUMBER)
            .setNonce(nonce)
            .build()

        manager.requestIntegrityToken(request)
            .addOnSuccessListener { response ->
                val token = response.token()
                val payload = decodeJwsPayload(token)

                val passed = verifyPayload(payload)
                onResult(passed, payload)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onResult(false, null)
            }
    }

    private fun generateNonce(): String {
        return "nonce_" + System.currentTimeMillis().toString()
    }

    /** Giải mã JSON từ JWS dạng header.payload.signature */
    private fun decodeJwsPayload(jws: String): JSONObject? {
        return try {
            val parts = jws.split(".")
            if (parts.size != 3) return null

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            JSONObject(payload)

        } catch (e: Exception) {
            Log.e("Integrity", "Decode error: ${e.message}")
            null
        }
    }

    /** Kiểm tra các trường integrity */
    private fun verifyPayload(json: JSONObject?): Boolean {
        if (json == null) return false

        return try {
            val appIntegrity = json.getJSONObject("appIntegrity")
            val verdict = appIntegrity.getString("appRecognitionVerdict")

            val deviceIntegrity = json.getJSONObject("deviceIntegrity")
            val deviceVerdicts = deviceIntegrity.getJSONArray("deviceRecognitionVerdict")

            val appOk = verdict == "UNEVALUATED" || verdict == "PLAY_RECOGNIZED"
            val deviceOk = deviceVerdicts.toString().contains("MEETS_DEVICE_INTEGRITY")

            appOk && deviceOk

        } catch (e: Exception) {
            false
        }
    }
}
