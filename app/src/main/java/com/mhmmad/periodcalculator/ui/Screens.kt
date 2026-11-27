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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch

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
    Surface(modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        content = { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content) })
}
@Composable fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}
@Composable fun Subheading(value: String) { Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
@Composable fun Stat(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .12f)), shadowElevation = 0.dp) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = 34.dp, height = 4.dp).background(Brush.horizontalGradient(listOf(Pine, Lavender)), RoundedCornerShape(8.dp)))
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
    var addInitial by remember { mutableStateOf(false) }
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
                       onPeriod: () -> Unit, onSymptom: () -> Unit, onHistory: () -> Unit, onFertility: () -> Unit, onTools: () -> Unit, onAtlas: () -> Unit, onLibrary: () -> Unit) {
    val prediction = vm.prediction()
    val today = LocalDate.now()
    val records = periods.map { PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), it.endEpochDay?.let(LocalDate::ofEpochDay)) }
    val currentDay = CycleEngine.currentCycleDay(records, today)
    val lang = wellnessLanguage()
    fun t(en: String, ar: String, pt: String) = when(lang) { "ar" -> ar; "pt" -> pt; else -> en }
    val c = MaterialTheme.colorScheme
    ScreenColumn {
        Text(DateText(today), style = MaterialTheme.typography.labelLarge, color = c.onSurfaceVariant)
        Text(t("Your cycle, your rhythm","دورتك، إيقاعك","Seu ciclo, seu ritmo"), style = MaterialTheme.typography.headlineLarge)
        Surface(shape = RoundedCornerShape(32.dp), color = c.surface, tonalElevation = 2.dp) {
            Column {
                Box(Modifier.fillMaxWidth().height(235.dp), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.art_wellness), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xCFFFF9F1), Color(0xEFFFF9F1)))))
                    CycleProgressRing(
                        progress = if(currentDay != null && prediction != null) currentDay.toFloat()/prediction.averageDays.coerceAtLeast(1) else 0f,
                        title = stringResource(R.string.cycle_day),
                        bigNumber = currentDay?.toString() ?: "—",
                        footer = if(currentDay == null) t("No cycle recorded","لم تسجّلي دورة","Sem registo") else t("Current cycle","الدورة الحالية","Ciclo atual"),
                        modifier = Modifier.size(210.dp)
                    )
                }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if(prediction == null) t("Begin your personal diary","ابدئي مذكرتك الشخصية","Comece seu diário pessoal") else stringResource(R.string.next_period), style = MaterialTheme.typography.titleLarge)
                    if(prediction != null) {
                        Text("${DateText(prediction.earliest)} – ${DateText(prediction.latest)}", style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.prediction_note), style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
                    }
                    Button(onClick = onPeriod, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = RoundedCornerShape(28.dp)) {
                        Icon(Icons.Outlined.AddCircleOutline, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.log_period))
                    }
                }
            }
        }
        val recent = entries.filter { it.epochDay in today.minusDays(6).toEpochDay()..today.toEpochDay() }
        Surface(shape = RoundedCornerShape(26.dp), color = c.secondaryContainer) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(t("Your last 7 days","آخر سبعة أيام","Seus últimos 7 dias"), style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (6 downTo 0).forEach { offset ->
                        val day = today.minusDays(offset.toLong())
                        val entry = recent.firstOrNull { it.epochDay == day.toEpochDay() }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(30.dp).background(if(entry != null) c.primary else c.surface, CircleShape), contentAlignment = Alignment.Center) {
                                Text(day.dayOfMonth.toString(), color = if(entry != null) c.onPrimary else c.onSurface, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Text(t("Days logged: ","أيام مسجلة: ","Dias registados: ") + recent.map { it.epochDay }.distinct().size + "/7", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onSymptom, modifier = Modifier.weight(1f)) { Text(t("Log today","سجلي اليوم","Registar hoje")) }
                    OutlinedButton(onClick = onAtlas, modifier = Modifier.weight(1f)) { Text(t("Explore","استكشفي","Explorar")) }
                }
            }
        }
        Subheading(stringResource(R.string.log_today))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardTile(stringResource(R.string.log_symptoms), Icons.Outlined.FavoriteBorder, Modifier.weight(1f), onSymptom)
            DashboardTile(stringResource(R.string.history), Icons.Outlined.Assignment, Modifier.weight(1f), onHistory)
        }
        Subheading(t("Explore your health","استكشفي صحتك","Explore sua saúde"))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardTile(t("Symptom atlas","أطلس الأعراض","Atlas de sintomas"), Icons.Outlined.Insights, Modifier.weight(1f), onAtlas)
            DashboardTile(t("Health library","المكتبة الصحية","Biblioteca de saúde"), Icons.Outlined.Spa, Modifier.weight(1f), onLibrary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardTile(stringResource(R.string.tools_title), Icons.Outlined.CalendarToday, Modifier.weight(1f), onTools)
            DashboardTile(stringResource(R.string.fertility), Icons.Outlined.DateRange, Modifier.weight(1f), onFertility)
        }
        Panel {
            Subheading(stringResource(R.string.recent_symptoms))
            if(entries.isEmpty()) Text(stringResource(R.string.no_data), color = c.onSurfaceVariant)
            entries.sortedByDescending { it.epochDay }.take(3).forEach { e ->
                Text("${DateText(LocalDate.ofEpochDay(e.epochDay))}  ·  ${stringResource(R.string.cramps)} ${e.cramps}/3", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Notice(stringResource(R.string.medical_notice))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable private fun DashboardTile(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, action: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val art = when(icon) {
        Icons.Outlined.FavoriteBorder -> R.drawable.art_wellness
        Icons.Outlined.CalendarToday, Icons.Outlined.Assignment -> R.drawable.art_calendar
        Icons.Outlined.Insights -> R.drawable.art_insights
        Icons.Outlined.DateRange -> R.drawable.art_fertility
        else -> R.drawable.art_wellness
    }
    Surface(onClick = action, modifier = modifier, shape = RoundedCornerShape(24.dp), color = c.surface, border = BorderStroke(1.dp,c.outlineVariant)) {
        Column {
            Image(painterResource(art), null, Modifier.fillMaxWidth().height(100.dp), contentScale = ContentScale.Crop)
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = c.primary, modifier = Modifier.size(20.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable fun CalendarScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, vm: PeriodViewModel, onDay: (Long) -> Unit) {
    val initialMonth = remember { YearMonth.now() }
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
    val c = MaterialTheme.colorScheme
    val lang = wellnessLanguage()
    fun t(en: String, ar: String, pt: String) = when(lang) { "ar" -> ar; "pt" -> pt; else -> en }
    val firstWeekday = WeekFields.of(locale).firstDayOfWeek
    val state = rememberCalendarState(startMonth = initialMonth.minusYears(8), endMonth = initialMonth.plusYears(3), firstVisibleMonth = initialMonth, firstDayOfWeek = firstWeekday)
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var onlyLogged by remember { mutableStateOf(false) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.calendar), t("Swipe to explore your timeline","اسحبي لاستكشاف سجلك","Deslize para explorar seu histórico"), R.drawable.art_calendar)
        Panel {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { scope.launch { val m = state.firstVisibleMonth.yearMonth.minusMonths(1); if(!m.isBefore(initialMonth.minusYears(8))) state.animateScrollToMonth(m) } }) { Icon(Icons.Outlined.ChevronLeft, t("Previous month","الشهر السابق","Mês anterior")) }
                Text(state.firstVisibleMonth.yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("LLLL yyyy",locale)), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { scope.launch { val m = state.firstVisibleMonth.yearMonth.plusMonths(1); if(!m.isAfter(initialMonth.plusYears(3))) state.animateScrollToMonth(m) } }) { Icon(Icons.Outlined.ChevronRight, t("Next month","الشهر التالي","Próximo mês")) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(selected = onlyLogged, onClick = { onlyLogged = !onlyLogged }, label = { Text(t("Records only","السجلات فقط","Só registos")) })
                TextButton(onClick = { selected = LocalDate.now(); scope.launch { state.animateScrollToMonth(initialMonth) } }) { Text(t("Today","اليوم","Hoje")) }
            }
            Row(Modifier.fillMaxWidth()) {
                (0..6).map { firstWeekday.plus(it.toLong()) }.forEach { day ->
                    Text(day.getDisplayName(java.time.format.TextStyle.SHORT,locale), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                }
            }
            HorizontalCalendar(state = state, modifier = Modifier.fillMaxWidth(), dayContent = { day ->
                val date = day.date
                val inMonth = day.position == DayPosition.MonthDate
                val isLogged = date.toEpochDay() in dailySet
                val isRecorded = date in recorded
                val isEstimate = !onlyLogged && prediction != null && !date.isBefore(prediction.earliest) && !date.isAfter(prediction.latest)
                val isFertile = !onlyLogged && fertility != null && !date.isBefore(fertility.start) && !date.isAfter(fertility.end)
                val bg = when {
                    isRecorded -> Rose
                    isEstimate -> Blush
                    isFertile -> Lavender
                    else -> Color.Transparent
                }
                Box(Modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape).background(if(inMonth) bg else Color.Transparent)
                    .border(if(inMonth && date == selected) 2.dp else 0.dp, if(inMonth && date == selected) c.primary else Color.Transparent, CircleShape)
                    .clickable(enabled = inMonth) { selected = date }, contentAlignment = Alignment.Center) {
                    if(inMonth) Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(date.dayOfMonth.toString(), color = if(isRecorded) Color.White else if(isEstimate || isFertile) Plum else c.onSurface, fontWeight = if(date == selected) FontWeight.Bold else FontWeight.Normal)
                        if(isLogged) Box(Modifier.size(5.dp).background(if(isRecorded) Color.White else c.primary,CircleShape))
                    }
                }
            })
        }
        Surface(color = c.secondaryContainer, shape = RoundedCornerShape(26.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(DateText(selected), style = MaterialTheme.typography.titleLarge)
                val entry = entries.firstOrNull { it.epochDay == selected.toEpochDay() }
                Text(if(entry == null) t("No symptoms recorded","لا توجد أعراض مسجلة","Sem sintomas registados") else t("Cramps: ","التقلصات: ","Cólicas: ") + entry.cramps + "/3")
                if(selected in recorded) Text(stringResource(R.string.calendar_legend_period))
                Button(onClick = { onDay(selected.toEpochDay()) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.log_symptoms)) }
            }
        }
        LegendDot(Rose, stringResource(R.string.calendar_legend_period))
        if(!onlyLogged) {
            LegendDot(Blush, stringResource(R.string.calendar_legend_estimate))
            LegendDot(Lavender, stringResource(R.string.calendar_legend_fertile))
        }
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

@Composable fun InsightsScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, vm: PeriodViewModel, onHistory: () -> Unit, onFertility: () -> Unit, onReview: () -> Unit, onTools: () -> Unit) {
    val cycle = vm.prediction()
    val measurements = CycleEngine.intervals(periods.map { PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), null) })
    var selected by remember(measurements) { mutableIntStateOf(-1) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.insights), stringResource(R.string.data_quality), R.drawable.art_insights)
        CycleToolsEntry(onTools)
        Panel {
            Subheading(stringResource(R.string.review_title))
            Text(stringResource(R.string.review_intro))
            Button(onClick = onReview, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.review_title)) }
        }
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
