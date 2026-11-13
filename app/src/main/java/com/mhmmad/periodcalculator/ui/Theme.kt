package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Rose = Color(0xFFB84A75)
val Blush = Color(0xFFF7B2C4)
val Lavender = Color(0xFFCDBAF5)
val Ivory = Color(0xFFFFF9F7)
val Plum = Color(0xFF38253F)
val DarkBackground = Color(0xFF38253F)
private val lightPalette = lightColorScheme(
    primary = Rose, onPrimary = Color.White, primaryContainer = Color(0xFFFFDDE8), onPrimaryContainer = Plum, secondary = Color(0xFF77549A),
    tertiary = Color(0xFF9B6D65), background = Ivory,
    surface = Color.White, surfaceVariant = Color(0xFFFFE8EE),
    onBackground = Plum, onSurface = Plum
)
private val darkPalette = darkColorScheme(
    primary = Blush, onPrimary = DarkBackground, primaryContainer = Color(0xFF61334E), onPrimaryContainer = Color.White, secondary = Lavender,
    background = DarkBackground, surface = Color(0xFF4D3558),
    surfaceVariant = Color(0xFF64466A), onBackground = Color(0xFFFFF4F6),
    onSurface = Color(0xFFFFF4F6)
)
@Composable
fun PeriodTheme(setting: String, content: @Composable () -> Unit) {
    val dark = when (setting) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) darkPalette else lightPalette,
        shapes = Shapes(
            small = androidx.compose.foundation.shape.RoundedCornerShape(12),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(20),
            large = androidx.compose.foundation.shape.RoundedCornerShape(28)
        ), content = content)
}
