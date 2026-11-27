package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Rose = Color(0xFFB74F43)
val Blush = Color(0xFFF1C6B6)
val Lavender = Color(0xFFBAD9C8)
val Ivory = Color(0xFFF6F4EC)
val Plum = Color(0xFF163A35)
val DarkBackground = Color(0xFF132B28)
val Pine = Color(0xFF14594E)
val Sage = Color(0xFFD9E9D7)
private val lightPalette = lightColorScheme(
    primary = Pine, onPrimary = Color.White, primaryContainer = Sage, onPrimaryContainer = Plum,
    secondary = Color(0xFF526F60), onSecondary = Color.White, secondaryContainer = Color(0xFFE5E9D9), onSecondaryContainer = Plum,
    tertiary = Rose, onTertiary = Color.White, tertiaryContainer = Color(0xFFFFDCD0), onTertiaryContainer = Color(0xFF57251E),
    background = Ivory, onBackground = Plum, surface = Color(0xFFFFFDF8), onSurface = Plum,
    surfaceVariant = Color(0xFFE7EBE2), onSurfaceVariant = Color(0xFF405A52),
    outline = Color(0xFF788B80), outlineVariant = Color(0xFFCAD5C8)
)
private val darkPalette = darkColorScheme(
    primary = Color(0xFFA8DDC4), onPrimary = Color(0xFF10382F), primaryContainer = Color(0xFF285249), onPrimaryContainer = Color(0xFFE0F3E6),
    secondary = Color(0xFFB5CDB9), secondaryContainer = Color(0xFF354A40), onSecondaryContainer = Color(0xFFE0F3E6),
    tertiary = Color(0xFFFFB4A1), background = DarkBackground, surface = Color(0xFF203C37),
    surfaceVariant = Color(0xFF314D45), onBackground = Color(0xFFE7F0E6), onSurface = Color(0xFFE7F0E6), onSurfaceVariant = Color(0xFFBBD0C4),
    outline = Color(0xFF8EA99A), outlineVariant = Color(0xFF476257)
)
private val base = Typography()
private val editorial = base.copy(
    headlineLarge = base.headlineLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold, lineHeight = 39.sp),
    headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 34.sp),
    titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold, lineHeight = 29.sp),
    titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyMedium = base.bodyMedium.copy(fontSize = 15.sp, lineHeight = 23.sp),
    bodySmall = base.bodySmall.copy(fontSize = 13.sp, lineHeight = 20.sp),
    labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp)
)
@Composable fun PeriodTheme(setting: String, content: @Composable () -> Unit) {
    val dark = when(setting) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if(dark) darkPalette else lightPalette, typography = editorial,
        shapes = Shapes(small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(14.dp), large = RoundedCornerShape(22.dp)), content = content)
}
