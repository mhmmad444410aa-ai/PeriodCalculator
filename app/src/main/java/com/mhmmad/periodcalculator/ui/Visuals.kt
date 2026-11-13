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
    val dark = MaterialTheme.colorScheme.background == DarkBackground
    val bannerColors = if (dark) listOf(Color(0xFF432C49), Color(0xFF523653), Color(0xFF42314C))
        else listOf(Color(0xFFFFEBF1), Color(0xFFF6E7FE), Color(0xFFFFF3EA))
    Box(
        modifier.fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(28.dp), ambientColor = Rose.copy(alpha = .09f))
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.horizontalGradient(bannerColors))
            .border(1.dp, Color.White.copy(alpha = if (dark) .14f else .75f), RoundedCornerShape(28.dp))
            .heightIn(min = 146.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = .42f), radius = size.minDimension * .33f, center = Offset(size.width * .94f, size.height * .12f))
            drawCircle(Color(0xFFF5C0DA).copy(alpha = .38f), radius = size.minDimension * .34f, center = Offset(size.width * .06f, size.height * 1.03f))
            drawCircle(Color.White.copy(alpha = .4f), radius = 10.dp.toPx(), center = Offset(size.width * .63f, size.height * .23f))
        }
        Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (dark) Color.White else Plum, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Text(description, style = MaterialTheme.typography.bodySmall, color = if (dark) Color(0xFFFFDBE9) else Color(0xFF795C72), maxLines = 4)
            }
            Image(painter = painterResource(imageId), contentDescription = null,
                modifier = Modifier.size(width = 126.dp, height = 132.dp), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun FloralEmptyState(title: String, message: String, action: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .1f))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painter = painterResource(R.drawable.art_cycle), contentDescription = null, modifier = Modifier.size(150.dp))
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
                Image(painter = painterResource(iconId), contentDescription = null, modifier = Modifier.size(42.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text("${value.toInt()} / ${max.toInt()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = { change(kotlin.math.round(it).toFloat()) }, valueRange = min..max,
            steps = (max - min).toInt() - 1, modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(activeTrackColor = Rose, thumbColor = Rose, inactiveTrackColor = Lavender.copy(alpha = .35f)))
    }
}

@Composable
fun IllustratedInfoRow(@DrawableRes imageId: Int, text: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Image(painter = painterResource(imageId), contentDescription = null, modifier = Modifier.size(66.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
    }
}
