package com.example.eduquizz.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object SignatureUtils {

    fun verifyAppSignature(context: Context): Boolean {

        val expected = "3C:97:D0:FB:AD:D0:ED:92:78:DA:80:6B:6A:F7:FC:F0:F8:13:D1:3C:DC:58:28:B5:CE:0F:67:94:F4:00:2B:51"

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

            hex == expected

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
