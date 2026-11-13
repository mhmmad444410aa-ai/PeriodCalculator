package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhmmad.periodcalculator.R

/**
 * A true in-app illustration, not a store mockup.
 * All art is bundled locally. Markers are explanatory only, never medical predictions.
 */
@Composable
fun CycleJourneyCard(cycleDay: Int?, averageDays: Int, modifier: Modifier = Modifier) {
    val stages = listOf(
        R.string.log_period to R.drawable.art_calendar,
        R.string.cycle_day to R.drawable.art_cycle,
        R.string.fertility to R.drawable.art_fertility,
        R.string.next_period to R.drawable.art_wellness
    )
    val value = if (cycleDay == null || averageDays <= 0) 0f
                else (cycleDay.toFloat() / averageDays.toFloat()).coerceIn(0f, 1f)
    val stageIndex = when {
        cycleDay == null -> -1
        value < .20f -> 0
        value < .60f -> 1
        value < .85f -> 2
        else -> 3
    }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp),
        color = Color.Transparent, tonalElevation = 0.dp, shadowElevation = 5.dp) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(Color(0xFFFFF0F3), Color(0xFFF7EBFF), Color(0xFFFFE6EE))))
                .border(1.dp, Color.White.copy(alpha = .8f), RoundedCornerShape(30.dp))
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.cycle_label), style = MaterialTheme.typography.titleMedium,
                        color = Plum, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.illustrated_edition), fontSize = 11.sp,
                        color = Color(0xFF805D81))
                }
                Image(painterResource(R.drawable.art_wellness), null, Modifier.size(66.dp),
                    contentScale = ContentScale.Fit)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                stages.forEachIndexed { index, pair ->
                    val active = index == stageIndex
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(if (active) 65.dp else 58.dp)
                            .clip(CircleShape)
                            .background(if (active) Color.White else Color.White.copy(alpha = .48f))
                            .border(if (active) 2.dp else 1.dp,
                                if (active) Rose else Color.White.copy(alpha = .7f), CircleShape),
                            contentAlignment = Alignment.Center) {
                            Image(painterResource(pair.second), null,
                                Modifier.size(if (active) 59.dp else 51.dp),
                                contentScale = ContentScale.Fit)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(pair.first), fontSize = 10.sp,
                            lineHeight = 12.sp, color = Plum, textAlign = TextAlign.Center,
                            maxLines = 2, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            LinearProgressIndicator(
                progress = { value },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Rose,
                trackColor = Color.White.copy(alpha = .8f)
            )
            Text(stringResource(R.string.prediction_note), style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF73566A), maxLines = 2)
        }
    }
}
