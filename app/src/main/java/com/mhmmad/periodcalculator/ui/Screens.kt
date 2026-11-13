package com.mhmmad.periodcalculator.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.*
import com.mhmmad.periodcalculator.domain.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

@Composable fun DateText(date: LocalDate): String {
    val config = LocalContext.current.resources.configuration
    val locale = androidx.core.os.ConfigurationCompat.getLocales(config).get(0) ?: Locale.UK
    return remember(date, locale) { date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)) }
}
@Composable fun DateInput(label: String, value: LocalDate, onChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        DatePickerDialog(context, { _, year, month, day -> onChange(LocalDate.of(year, month+1, day)) },
            value.year, value.monthValue-1, value.dayOfMonth).show()
    }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Outlined.CalendarToday, null, Modifier.size(18.dp)); Spacer(Modifier.width(12.dp))
        Text("$label: ${DateText(value)}")
    }
}
@Composable fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .10f)),
        color = MaterialTheme.colorScheme.surface,
        content = { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp), content = content) })
}
@Composable fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .14f), MaterialTheme.colorScheme.background)))
        .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}
@Composable fun Subheading(value: String) { Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
@Composable fun Stat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .12f)), shadowElevation = 2.dp) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = 34.dp, height = 4.dp).background(Brush.horizontalGradient(listOf(Blush, Lavender)), RoundedCornerShape(8.dp)))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
    }
}
@Composable fun Notice(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Info, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable fun OnboardingScreen(vm: PeriodViewModel, done: () -> Unit) {
    var date by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var cycle by remember { mutableFloatStateOf(28f) }
    var bleeding by remember { mutableFloatStateOf(5f) }
    var addInitial by remember { mutableStateOf(true) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.welcome), stringResource(R.string.onboarding_message), R.drawable.art_wellness)
        Notice(stringResource(R.string.local_data))
        Panel {
            Subheading(stringResource(R.string.cycle_length))
            Text("${cycle.roundToInt()}", style = MaterialTheme.typography.headlineSmall)
            Slider(value = cycle, onValueChange = { cycle = it }, valueRange = 21f..45f, steps = 23)
            Subheading(stringResource(R.string.period_length))
            Text("${bleeding.roundToInt()}", style = MaterialTheme.typography.titleLarge)
            Slider(value = bleeding, onValueChange = { bleeding = it }, valueRange = 2f..10f, steps = 7)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(addInitial, onCheckedChange = { addInitial = it })
                Text(stringResource(R.string.last_period))
            }
            if (addInitial) DateInput(stringResource(R.string.last_period), date) { date = it }
        }
        Button(onClick = {
            if (addInitial) vm.savePeriod(PeriodEntity(startEpochDay = date.toEpochDay(), endEpochDay = null, flowIntensity = 2, note = ""))
            vm.finishSetup(cycle.roundToInt(), bleeding.roundToInt()); done()
        }, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(stringResource(R.string.continue_button)) }
        TextButton(onClick = { vm.finishSetup(cycle.roundToInt(), bleeding.roundToInt()); done() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.skip))
        }
        Notice(stringResource(R.string.medical_notice))
    }
}

@Composable fun HomeScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, vm: PeriodViewModel,
                       onPeriod: () -> Unit, onSymptom: () -> Unit, onHistory: () -> Unit, onFertility: () -> Unit) {
    val prediction = vm.prediction()
    val today = LocalDate.now()
    val records = periods.map { PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), it.endEpochDay?.let(LocalDate::ofEpochDay)) }
    val currentDay = CycleEngine.currentCycleDay(records, today)
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.recent_note), stringResource(R.string.prediction_note), R.drawable.art_wellness)
        CycleJourneyCard(currentDay, prediction?.averageDays ?: 28)
        if (prediction == null) {
            FloralEmptyState(stringResource(R.string.no_data), stringResource(R.string.add_first), stringResource(R.string.log_period), onPeriod)
        } else {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFFDDE9), Color(0xFFF4DFFD), Color(0xFFFFEBF0))))) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.next_period), style = MaterialTheme.typography.titleMedium, color = Plum)
                    if (prediction.confidence != Confidence.LOW && !prediction.isExtrapolated) {
                        val until = CycleEngine.numberOfDaysUntil(prediction.nextStart, today)
                        CycleProgressRing((currentDay ?: 1).toFloat() / prediction.averageDays.coerceAtLeast(1),
                            stringResource(R.string.days_left), until.toString(), stringResource(R.string.cycle_label))
                        Text(DateText(prediction.nextStart), style = MaterialTheme.typography.titleLarge, color = Plum, fontWeight = FontWeight.SemiBold)
                    } else {
                        Image(painter = androidx.compose.ui.res.painterResource(R.drawable.art_cycle), contentDescription = null, modifier = Modifier.size(110.dp))
                        Text(stringResource(R.string.next_range), style = MaterialTheme.typography.titleLarge, color = Plum)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("${DateText(prediction.earliest)} — ${DateText(prediction.latest)}",
                        style = MaterialTheme.typography.bodyMedium, color = Plum, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(stringResource(R.string.cycle_day), currentDay?.toString() ?: "—", Modifier.weight(1f))
                Stat(stringResource(R.string.avg_cycle), "${prediction.averageDays}", Modifier.weight(1f))
            }
            if (prediction.confidence == Confidence.LOW || prediction.isExtrapolated) Notice(stringResource(R.string.irregular_warning))
        }
        Subheading(stringResource(R.string.log_today))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onPeriod, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.AddCircleOutline, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.log_period))
            }
            FilledTonalButton(onClick = onSymptom, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.FavoriteBorder, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.log_symptoms))
            }
        }
        Panel {
            IllustratedInfoRow(R.drawable.art_insights, stringResource(R.string.recent_symptoms))
            if (entries.isEmpty()) Text(stringResource(R.string.no_data))
            entries.take(3).forEach { e ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("${DateText(LocalDate.ofEpochDay(e.epochDay))}  •  ${stringResource(R.string.mood)} ${e.mood}/5  •  ${stringResource(R.string.cramps)} ${e.cramps}/3",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onHistory, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.history)) }
            OutlinedButton(onClick = onFertility, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.fertility)) }
        }
        Notice(stringResource(R.string.prediction_note))
        Spacer(Modifier.height(65.dp))
    }
}

@Composable fun CalendarScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, vm: PeriodViewModel, onDay: (Long) -> Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    val prediction = vm.prediction()
    val fertility = prediction?.let(CycleEngine::fertility)
    val recorded = remember(periods) { periods.flatMap { p ->
        val first = LocalDate.ofEpochDay(p.startEpochDay)
        val end = p.endEpochDay?.let(LocalDate::ofEpochDay) ?: first.plusDays((vm.prefs.value.bleedingLength - 1).toLong())
        if (end.isBefore(first) || ChronoUnit.DAYS.between(first, end) > 15) emptyList()
        else (0..ChronoUnit.DAYS.between(first, end).toInt()).map { first.plusDays(it.toLong()) }
    }.toSet() }
    val dailySet = entries.map { it.epochDay }.toSet()
    val config = LocalContext.current.resources.configuration
    val locale = androidx.core.os.ConfigurationCompat.getLocales(config).get(0) ?: Locale.UK
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.calendar), stringResource(R.string.prediction_note), R.drawable.art_calendar)
        Panel {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Outlined.ChevronLeft, null) }
                Text(month.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy", locale)), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Outlined.ChevronRight, null) }
            }
            val weekStart = WeekFields.of(locale).firstDayOfWeek
            val days = (0..6).map { weekStart.plus(it.toLong()) }
            Row(Modifier.fillMaxWidth()) { days.forEach { day ->
                Text(day.getDisplayName(java.time.format.TextStyle.SHORT, locale), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            } }
            Spacer(Modifier.height(8.dp))
            val offset = (month.atDay(1).dayOfWeek.value - weekStart.value + 7) % 7
            val cells = (0 until ((offset + month.lengthOfMonth() + 6) / 7) * 7).map { it - offset + 1 }
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    week.forEach { day ->
                        val current = if (day in 1..month.lengthOfMonth()) month.atDay(day) else null
                        val recordedDay = current != null && current in recorded
                        val estimated = current != null && prediction != null && !current.isBefore(prediction.earliest) && !current.isAfter(prediction.latest)
                        val fertile = current != null && fertility != null && !current.isBefore(fertility.start) && !current.isAfter(fertility.end)
                        val bg = when {
                            recordedDay -> Rose
                            fertile -> Lavender
                            estimated -> Blush.copy(alpha = .7f)
                            else -> Color.Transparent
                        }
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape)
                            .background(bg)
                            .border(if (current == LocalDate.now()) 2.dp else 0.dp,
                                if (current == LocalDate.now()) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                            .clickable(enabled = current != null) { current?.let { onDay(it.toEpochDay()) } }, contentAlignment = Alignment.Center) {
                            if (current != null) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day.toString(), color = if (recordedDay) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (current == LocalDate.now()) FontWeight.Bold else FontWeight.Normal)
                                if (current.toEpochDay() in dailySet) Box(Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                            }
                        }
                    }
                }
            }
        }
        LegendDot(Rose, stringResource(R.string.calendar_legend_period))
        LegendDot(Blush, stringResource(R.string.calendar_legend_estimate))
        LegendDot(Lavender, stringResource(R.string.calendar_legend_fertile))
        Notice(stringResource(R.string.fertility_warning))
    }
}
@Composable fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(12.dp).background(color, CircleShape)); Spacer(Modifier.width(8.dp)); Text(text) }
}

@Composable fun PeriodEditor(existing: PeriodEntity?, vm: PeriodViewModel, done: () -> Unit) {
    var start by remember(existing?.id) { mutableStateOf(existing?.startEpochDay?.let(LocalDate::ofEpochDay) ?: LocalDate.now()) }
    var end by remember(existing?.id) { mutableStateOf(existing?.endEpochDay?.let(LocalDate::ofEpochDay) ?: start.plusDays(4)) }
    var hasEnd by remember(existing?.id) { mutableStateOf(existing?.endEpochDay != null) }
    var flow by remember(existing?.id) { mutableIntStateOf(existing?.flowIntensity ?: 2) }
    var note by remember(existing?.id) { mutableStateOf(existing?.note ?: "") }
    var error by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf(false) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.log_period), stringResource(R.string.period_details), R.drawable.art_calendar)
        Panel {
            Subheading(stringResource(R.string.period_details))
            Spacer(Modifier.height(12.dp))
            DateInput(stringResource(R.string.start), start) { start = it }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(hasEnd, onCheckedChange = { hasEnd = it }); Text(stringResource(R.string.range_end))
            }
            if (hasEnd) DateInput(stringResource(R.string.end), end) { end = it }
            Spacer(Modifier.height(14.dp))
            Subheading(stringResource(R.string.flow))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(R.string.light, R.string.medium, R.string.heavy).forEachIndexed { idx, title ->
                    FilterChip(selected = flow == idx+1, onClick = { flow = idx+1 }, label = { Text(stringResource(title)) })
                }
            }
            OutlinedTextField(value = note, onValueChange = { note = it.take(500) }, label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(), minLines = 3)
        }
        if (error) Notice(stringResource(R.string.invalid_dates))
        Button(onClick = {
            if (hasEnd && end.isBefore(start)) error = true
            else { vm.savePeriod(PeriodEntity(existing?.id ?: 0, start.toEpochDay(), if (hasEnd) end.toEpochDay() else null, flow, note.trim())); done() }
        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
        if (existing != null) TextButton(onClick = { deleteConfirm = true }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }
    }
    if (deleteConfirm) AlertDialog(onDismissRequest = { deleteConfirm = false }, title = { Text(stringResource(R.string.delete)) },
        text = { Text(stringResource(R.string.history_hint)) },
        confirmButton = { TextButton(onClick = { vm.deletePeriod(existing!!.id); deleteConfirm = false; done() }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { deleteConfirm = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable fun SymptomEditor(existing: DailyEntryEntity?, initially: Long, vm: PeriodViewModel, done: () -> Unit) {
    var day by remember(initially) { mutableStateOf(LocalDate.ofEpochDay(initially)) }
    var cramps by remember(existing) { mutableFloatStateOf((existing?.cramps ?: 0).toFloat()) }
    var headache by remember(existing) { mutableFloatStateOf((existing?.headache ?: 0).toFloat()) }
    var bloating by remember(existing) { mutableFloatStateOf((existing?.bloating ?: 0).toFloat()) }
    var fatigue by remember(existing) { mutableFloatStateOf((existing?.fatigue ?: 0).toFloat()) }
    var mood by remember(existing) { mutableFloatStateOf((existing?.mood ?: 3).toFloat()) }
    var sleep by remember(existing) { mutableStateOf(existing?.sleepHours?.toString() ?: "") }
    var notes by remember(existing) { mutableStateOf(existing?.notes ?: "") }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.log_symptoms), stringResource(R.string.symptom_levels), R.drawable.art_wellness)
        DateInput(stringResource(R.string.symptoms_for_day), day) { day = it }
        Panel {
            Text(stringResource(R.string.symptom_levels), style = MaterialTheme.typography.bodySmall)
            SymptomLevelRow(stringResource(R.string.cramps), R.drawable.symptom_cramps, cramps, 3f) { cramps = it }
            SymptomLevelRow(stringResource(R.string.headache), R.drawable.symptom_headache, headache, 3f) { headache = it }
            SymptomLevelRow(stringResource(R.string.bloating), R.drawable.symptom_bloating, bloating, 3f) { bloating = it }
            SymptomLevelRow(stringResource(R.string.fatigue), R.drawable.symptom_fatigue, fatigue, 3f) { fatigue = it }
            SymptomLevelRow(stringResource(R.string.mood), R.drawable.symptom_mood, mood, 5f, 1f) { mood = it }
            OutlinedTextField(sleep, { sleep = it.take(4) }, label = { Text(stringResource(R.string.sleep_hours)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(notes, { notes = it.take(500) }, label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
        Button(onClick = {
            vm.saveDay(DailyEntryEntity(day.toEpochDay(), cramps.roundToInt(), headache.roundToInt(), bloating.roundToInt(),
                fatigue.roundToInt(), mood.roundToInt(), (sleep.toFloatOrNull() ?: 0f).coerceIn(0f, 24f), notes.trim()))
            done()
        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save_day)) }
        if (existing != null) TextButton(onClick = { vm.deleteDay(existing.epochDay); done() }) { Text(stringResource(R.string.delete)) }
    }
}
@Composable fun SymptomSlider(label: String, value: Float, max: Float, min: Float = 0f, change: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text("${value.roundToInt()}/${max.roundToInt()}") }
    Slider(value = value, onValueChange = { change(it.roundToInt().toFloat()) }, valueRange = min..max, steps = (max-min).toInt()-1)
}

@Composable fun InsightsScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, vm: PeriodViewModel, onHistory: () -> Unit, onFertility: () -> Unit) {
    val cycle = vm.prediction()
    val measurements = CycleEngine.intervals(periods.map { PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), null) })
    var selected by remember(measurements) { mutableIntStateOf(-1) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.insights), stringResource(R.string.data_quality), R.drawable.art_insights)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(stringResource(R.string.count_cycles), periods.size.toString(), Modifier.weight(1f))
            Stat(stringResource(R.string.avg_cycle), cycle?.averageDays?.toString() ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat(stringResource(R.string.variation), if (measurements.isEmpty()) "—" else CycleEngine.variation(measurements).toString(), Modifier.weight(1f))
            Stat(stringResource(R.string.count_symptoms), entries.size.toString(), Modifier.weight(1f))
        }
        Panel {
            Subheading(stringResource(R.string.varying_lengths)); Spacer(Modifier.height(10.dp))
            if (measurements.isEmpty()) Text(stringResource(R.string.insufficient_cycles))
            else {
                Row(Modifier.fillMaxWidth().height(156.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                    measurements.forEachIndexed { idx, n ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selected = idx }) {
                            Text(n.toString(), style = MaterialTheme.typography.labelSmall)
                            Spacer(Modifier.height(5.dp))
                            Box(Modifier.width(28.dp).height((n.coerceIn(15,60) * 2.0f).dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(Brush.verticalGradient(if (selected == idx)
                                    listOf(Lavender, MaterialTheme.colorScheme.secondary) else listOf(Blush, Rose))))
                        }
                    }
                }
            }
        }
        Panel {
            Subheading(stringResource(R.string.symptom_comparison))
            if (entries.isEmpty()) Text(stringResource(R.string.no_data))
            else listOf(R.string.cramps to entries.map { it.cramps }, R.string.headache to entries.map { it.headache },
                R.string.bloating to entries.map { it.bloating }, R.string.fatigue to entries.map { it.fatigue }).forEach { (name, data) ->
                val avg = data.average().toFloat()
                Text(stringResource(name) + "  ${"%.1f".format(Locale.US, avg)}/3")
                LinearProgressIndicator(progress = { (avg/3).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
        }
        Panel {
            Subheading(stringResource(R.string.symptoms_by_cycle))
            val starts = periods.map { LocalDate.ofEpochDay(it.startEpochDay) }.distinct().sorted()
            if (starts.isEmpty() || entries.isEmpty()) Text(stringResource(R.string.insufficient_cycles))
            else starts.takeLast(4).forEachIndexed { index, start ->
                val next = starts.getOrNull(starts.indexOf(start) + 1)
                val days = entries.filter { d ->
                    val date = LocalDate.ofEpochDay(d.epochDay)
                    !date.isBefore(start) && (next == null || date.isBefore(next))
                }
                val crampsAvg = if (days.isEmpty()) 0f else days.map { it.cramps }.average().toFloat()
                Text("${DateText(start)} · ${days.size} ${stringResource(R.string.count_symptoms)}", style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { crampsAvg / 3f }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
            }
        }
        val confidence = when(cycle?.confidence) { Confidence.HIGH -> R.string.high; Confidence.MODERATE -> R.string.moderate; else -> R.string.low }
        Notice(stringResource(R.string.confidence) + ": " + stringResource(confidence) + ". " + stringResource(R.string.data_quality))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onHistory, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.history)) }
            OutlinedButton(onClick = onFertility, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.fertility)) }
        }
        Notice(stringResource(R.string.medical_notice))
    }
}

@Composable fun FertilityScreen(vm: PeriodViewModel) {
    val prediction = vm.prediction()
    val window = prediction?.let(CycleEngine::fertility)
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.fertility), stringResource(R.string.fertility_warning), R.drawable.art_fertility)
        Notice(stringResource(R.string.fertility_warning))
        if (window == null) Panel { Subheading(stringResource(R.string.insufficient_fertility)); Text(stringResource(R.string.data_quality)) }
        else {
            Panel { Subheading(stringResource(R.string.fertile_range))
                Spacer(Modifier.height(8.dp))
                Text("${DateText(window.start)} — ${DateText(window.end)}", style = MaterialTheme.typography.titleLarge)
            }
            Panel { Subheading(stringResource(R.string.ovulation))
                Text(DateText(window.ovulation), style = MaterialTheme.typography.titleLarge)
            }
        }
        Notice(stringResource(R.string.medical_notice))
    }
}
