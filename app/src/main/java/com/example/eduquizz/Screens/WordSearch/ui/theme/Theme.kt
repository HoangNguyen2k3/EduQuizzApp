package com.example.wordsearch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.wordsearchgame.ui.theme.Shapes
import com.example.wordsearchgame.ui.theme.Typography

private val DarkColorPalette = darkColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    secondary = Secondary,
    onSecondary = TextOnPrimary,
    tertiary = Teal200,
    background = Color(0xFF121927),
    onBackground = Color(0xFFE1E2E6),
    surface = Color(0xFF1E2533),
    onSurface = Color(0xFFE1E2E6)
)

private val LightColorPalette = lightColorScheme(
    primary = Primary,
    onPrimary = TextOnPrimary,
    secondary = Secondary,
    onSecondary = TextOnPrimary,
    tertiary = Teal200,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary
)

@Composable
fun WordSearchGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if(darkTheme){
        DarkColorPalette
    }else{
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}