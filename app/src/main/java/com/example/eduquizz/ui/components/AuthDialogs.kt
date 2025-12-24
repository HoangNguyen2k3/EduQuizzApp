package com.example.eduquizz.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Dialog hiển thị khi phiên đăng nhập hết hạn
 * Yêu cầu user đăng nhập lại
 */
@Composable
fun SessionExpiredDialog(
    message: String,
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("⏰", fontSize = 40.sp)
        },
        title = {
            Text(
                text = "Phiên đăng nhập hết hạn",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Đăng nhập lại")
            }
        },
        dismissButton = null // Không cho phép dismiss, bắt buộc login
    )
}

/**
 * Dialog hiển thị khi user bị force logout
 */
@Composable
fun ForceLogoutDialog(
    reason: String,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Text("🔒", fontSize = 40.sp)
        },
        title = {
            Text(
                text = "Đã đăng xuất",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = reason)
        },
        confirmButton = {
            Button(onClick = onLoginClick) {
                Text("Đăng nhập")
            }
        }
    )
}

/**
 * Dialog thông báo lỗi xác thực
 */
@Composable
fun AuthErrorDialog(
    title: String = "Lỗi xác thực",
    message: String,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("⚠️", fontSize = 40.sp)
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            if (onRetry != null) {
                Button(onClick = onRetry) {
                    Text("Thử lại")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
