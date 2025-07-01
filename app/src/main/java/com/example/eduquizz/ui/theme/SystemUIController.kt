package com.example.eduquizz.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
fun SystemUIController(
    statusBarColor: Color = Color.Transparent,
    navigationBarColor: Color = Color.Transparent,
    isLightStatusBar: Boolean = true,
    isLightNavigationBar: Boolean = true
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as androidx.activity.ComponentActivity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        // Thiết lập màu status bar và navigation bar
        window.statusBarColor = statusBarColor.hashCode()
        window.navigationBarColor = navigationBarColor.hashCode()

        // Thiết lập màu icon
        insetsController.apply {
            isAppearanceLightStatusBars = isLightStatusBar
            isAppearanceLightNavigationBars = isLightNavigationBar
        }
    }
}

// Extension function để dễ sử dụng
@Composable
fun SetSystemBarsTheme(
    isDarkTheme: Boolean = false
) {
    SystemUIController(
        isLightStatusBar = !isDarkTheme,
        isLightNavigationBar = !isDarkTheme
    )
}