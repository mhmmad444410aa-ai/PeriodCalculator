package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.PeriodEntity
import com.mhmmad.periodcalculator.data.DailyEntryEntity
import com.mhmmad.periodcalculator.domain.CycleReview
import com.mhmmad.periodcalculator.domain.ReviewSample
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.text.NumberFormat

@Composable
fun CycleReviewScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, onDay: (Long) -> Unit) {
    val today = LocalDate.now().toEpochDay()
    val cycles = remember(periods, entries, today) {
        CycleReview.build(periods.map { it.startEpochDay }, entries.map {
            ReviewSample(it.epochDay, listOf(it.cramps, it.headache, it.bloating, it.fatigue))
        }, today)
    }
    val locale = androidx.core.os.ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: java.util.Locale.UK
    val dates = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    val number = remember(locale) { NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 } }
    val metrics = listOf(R.string.cramps, R.string.headache, R.string.bloating, R.string.fatigue)
    var metric by remember { mutableIntStateOf(0) }
    var selected by remember(cycles.map { it.start }) { mutableIntStateOf(0) }
    val active = cycles.getOrNull(selected)
    val previous = cycles.getOrNull(selected + 1)
    var offset by remember(active?.start) { mutableLongStateOf(0L) }

    ScreenColumn {
        Text(stringResource(R.string.review_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.review_intro), style = MaterialTheme.typography.bodyMedium)
        if (active == null) {
            Panel { Text(stringResource(R.string.review_empty)) }
        } else {
            Panel {
                Text(stringResource(R.string.review_choose), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { selected++ }, enabled = selected < cycles.lastIndex) { Text(stringResource(R.string.review_older)) }
                    OutlinedButton(onClick = { selected-- }, enabled = selected > 0) { Text(stringResource(R.string.review_newer)) }
                }
                Text(dates.format(LocalDate.ofEpochDay(active.start)), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(if (active.complete) R.string.review_complete else R.string.review_open))
                Text(stringResource(R.string.review_coverage, number.format(active.samples.size), number.format(active.length)))
                LinearProgressIndicator(progress = { (active.samples.size.toDouble() / active.length.coerceAtLeast(1L)).toFloat().coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth())
            }
            Panel {
                Text(stringResource(R.string.review_compare), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.review_compare_hint), style = MaterialTheme.typography.bodySmall)
                if (previous != null) Text(stringResource(R.string.review_previous, dates.format(LocalDate.ofEpochDay(previous.start))))
                metrics.forEachIndexed { i, label ->
                    val a = active.average(i)
                    val b = previous?.average(i)
                    Text(stringResource(label), style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(R.string.review_values, a?.let { number.format(it) } ?: "—", b?.let { number.format(it) } ?: "—"))
                    if (a != null) LinearProgressIndicator(progress = { (a / 3).toFloat() }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                }
            }
            Panel {
                Text(stringResource(R.string.review_map), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.review_map_hint), style = MaterialTheme.typography.bodySmall)
                metrics.chunked(2).forEachIndexed { row, group ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        group.forEachIndexed { i, label ->
                            val index = row * 2 + i
                            FilterChip(selected = metric == index, onClick = { metric = index }, label = { Text(stringResource(label)) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
                val end = (offset + 28).coerceAtMost(active.length)
                Text(stringResource(R.string.review_range, number.format(offset + 1), number.format(end)))
                val days = (offset until end).toList()
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        week.forEach { index ->
                            val day = active.start + index
                            val level = active.level(day, metric)
                            val color = if (level == null) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
                            TextButton(onClick = { onDay(day) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp).background(color, RoundedCornerShape(8.dp)), contentPadding = PaddingValues(0.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(number.format(index+1), style = MaterialTheme.typography.labelSmall)
                                    Text(level?.let { number.format(it) } ?: "—", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                        repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { offset = (offset - 28).coerceAtLeast(0) }, enabled = offset > 0) { Text(stringResource(R.string.review_older)) }
                    OutlinedButton(onClick = { offset += 28 }, enabled = end < active.length) { Text(stringResource(R.string.review_newer)) }
                }
            }
            Text(stringResource(R.string.review_caution), style = MaterialTheme.typography.bodySmall)
        }
    }
}
