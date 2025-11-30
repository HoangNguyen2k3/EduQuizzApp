package com.example.eduquizz.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

object SignatureUtils {

    fun verifyAppSignature(context: Context): Boolean {

        val expected = "EC:0E:5E:7D:C8:F3:B2:9B:F1:4F:CA:E4:D1:E9:01:05:4A:23:CD:F2:C5:69:D1:FE:20:E9:9E:C3:40:D4:50:57"

        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures == null || signatures.isEmpty()) return false

            val md = MessageDigest.getInstance("SHA-256")
            val hex = md.digest(signatures[0].toByteArray()).joinToString(":") {
                "%02X".format(it)
            }
            Log.d("SIGNATURE", hex)
            hex == expected

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
