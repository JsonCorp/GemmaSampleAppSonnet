package com.example.gemmasample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Gemma 브랜드 색상 기반 팔레트
private val GemmaBlue = Color(0xFF1A73E8)
private val GemmaLightBlue = Color(0xFF4285F4)
private val GemmaGreen = Color(0xFF34A853)
private val GemmaDarkSurface = Color(0xFF1C1B1F)

private val DarkColorScheme = darkColorScheme(
    primary = GemmaLightBlue,
    secondary = GemmaGreen,
    background = GemmaDarkSurface,
    surface = Color(0xFF2C2B2F),
    onPrimary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

private val LightColorScheme = lightColorScheme(
    primary = GemmaBlue,
    secondary = GemmaGreen,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFF2F2F7),
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun GemmaSampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
