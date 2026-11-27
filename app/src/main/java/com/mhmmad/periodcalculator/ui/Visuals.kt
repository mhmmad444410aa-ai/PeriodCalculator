package com.mhmmad.periodcalculator.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhmmad.periodcalculator.R

/** All illustration files are shipped with the app. No CDN, external fetch or tracking. */
@Composable
fun IllustratedHeader(
    title: String,
    description: String,
    @DrawableRes imageId: Int,
    modifier: Modifier = Modifier
) {
    val c = MaterialTheme.colorScheme
    val symbol = when(imageId) {
        R.drawable.art_calendar -> Icons.Outlined.CalendarMonth
        R.drawable.art_insights -> Icons.Outlined.Insights
        R.drawable.art_fertility -> Icons.Outlined.FavoriteBorder
        else -> Icons.Outlined.Spa
    }
    Surface(modifier.fillMaxWidth(), color = c.primaryContainer, shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium, color = c.onPrimaryContainer)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = c.onPrimaryContainer)
            }
            Box(Modifier.size(60.dp).background(c.surface, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Icon(symbol, null, Modifier.size(32.dp), tint = c.primary)
            }
        }
    }
}

@Composable
fun FloralEmptyState(title: String, message: String, action: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .1f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Button(onClick = onClick, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
fun CycleProgressRing(
    progress: Float,
    title: String,
    bigNumber: String,
    footer: String,
    modifier: Modifier = Modifier
) {
    Box(modifier.size(194.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val stroke = 14.dp.toPx()
            drawArc(color = Color.White.copy(alpha = .62f), startAngle = 135f, sweepAngle = 270f,
                useCenter = false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            drawArc(brush = Brush.sweepGradient(listOf(Color(0xFFFFA8B5), Rose, Color(0xFFA982D4), Color(0xFFFFA8B5))),
                startAngle = 135f, sweepAngle = 270f * progress.coerceIn(0f,1f),
                useCenter = false, style = Stroke(width = stroke, cap = StrokeCap.Round))
            drawCircle(Color.White.copy(alpha = .48f), radius = 5.dp.toPx(), center = Offset(size.width * .16f, size.height * .12f))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Plum)
            Text(bigNumber, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Plum)
            Text(footer, style = MaterialTheme.typography.labelSmall, color = Color(0xFF76546D))
        }
    }
}

@Composable
fun SymptomLevelRow(label: String, @DrawableRes iconId: Int, value: Float, max: Float, min: Float = 0f, change: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(42.dp).background(Brush.linearGradient(listOf(Blush.copy(alpha = .45f), Lavender.copy(alpha = .4f))), CircleShape), contentAlignment = Alignment.Center) {
                Icon(when(iconId) { R.drawable.symptom_cramps -> Icons.Outlined.FavoriteBorder; R.drawable.symptom_headache -> Icons.Outlined.Info; R.drawable.symptom_fatigue -> Icons.Outlined.Bolt; R.drawable.symptom_mood -> Icons.Outlined.SentimentSatisfied; else -> Icons.Outlined.Spa }, null, Modifier.size(24.dp), tint = Pine)
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("${value.toInt()} / ${max.toInt()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = { change(kotlin.math.round(it).toFloat()) }, valueRange = min..max,
            steps = (max - min).toInt() - 1, modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary, thumbColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = Lavender.copy(alpha = .35f)))
    }
}

@Composable
fun IllustratedInfoRow(@DrawableRes imageId: Int, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Outlined.Insights, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}
