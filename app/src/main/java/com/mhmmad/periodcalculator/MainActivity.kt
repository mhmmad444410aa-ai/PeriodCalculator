package com.mhmmad.periodcalculator

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val Rose = Color(0xFFB84A75)
private val Pink = Color(0xFFF7B2C4)
private val Ivory = Color(0xFFFFF9F7)
private val Plum = Color(0xFF38253F)
private val Lilac = Color(0xFFCDBAF5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = CycleDatabase.get(applicationContext).dao()
        val prefs = getSharedPreferences("app-settings", Context.MODE_PRIVATE)
        setContent {
            var language by remember { mutableStateOf(prefs.getString("language", "system") ?: "system") }
            val localized = remember(language) {
                if (language == "system") this@MainActivity else {
                    val configuration = Configuration(resources.configuration)
                    configuration.setLocale(Locale.forLanguageTag(language))
                    createConfigurationContext(configuration)
                }
            }
            val rtl = if (language == "ar") true
                else if (language == "system") java.text.Bidi.requiresBidi(
                    Locale.getDefault().displayName.toCharArray(), 0, Locale.getDefault().displayName.length
                ) else false
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                CycleTheme {
                    val periods by dao.observePeriods().collectAsState(initial = emptyList())
                    val symptoms by dao.observeSymptoms().collectAsState(initial = emptyList())
                    val scope = rememberCoroutineScope()
                    var page by rememberSaveable { mutableIntStateOf(0) }
                    AppShell(
                        page = page,
                        periods = periods,
                        symptoms = symptoms,
                        onPage = { page = it },
                        onSavePeriod = { entry ->
                            scope.launch { dao.addPeriod(entry) }
                            page = 0
                        },
                        onSaveSymptoms = { entry ->
                            scope.launch { dao.addSymptom(entry) }
                            page = 0
                        },
                        onRemovePeriod = { entry -> scope.launch { dao.deletePeriod(entry) } },
                        onLanguage = {
                            language = it
                            prefs.edit().putString("language", it).apply()
                        },
                        language = language,
                        onClear = { scope.launch { dao.clearPeriods(); dao.clearSymptoms() } }
                    )
                }
            }
        }
    }
}

@Composable private fun CycleTheme(content: @Composable () -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val palette = if (dark) darkColorScheme(
        primary = Pink, onPrimary = Plum, background = Color(0xFF221925),
        surface = Color(0xFF2A2030), onSurface = Color(0xFFF9EDF5),
        secondary = Lilac
    ) else lightColorScheme(
        primary = Rose, onPrimary = Color.White, background = Ivory,
        surface = Color.White, onSurface = Plum,
        primaryContainer = Color(0xFFFCE0E9), onPrimaryContainer = Plum,
        secondary = Color(0xFF8E65A3)
    )
    MaterialTheme(colorScheme = palette, content = content)
}

@Composable private fun label(@StringRes res: Int): String = LocalContext.current.getString(res)

@Composable private fun prettyDate(date: LocalDate): String {
    val locale = LocalContext.current.resources.configuration.locales[0]
    return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
}

@Composable
private fun AppShell(
    page: Int,
    periods: List<PeriodEntry>,
    symptoms: List<SymptomEntry>,
    onPage: (Int) -> Unit,
    onSavePeriod: (PeriodEntry) -> Unit,
    onSaveSymptoms: (SymptomEntry) -> Unit,
    onRemovePeriod: (PeriodEntry) -> Unit,
    onLanguage: (String) -> Unit,
    language: String,
    onClear: () -> Unit
) {
    val tabNames = listOf(R.string.home, R.string.calendar, R.string.log_period, R.string.insights, R.string.settings)
    val tabIcons = listOf("⌂", "▦", "+", "◫", "⚙")
    Scaffold(
        topBar = {
            Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 22.dp, vertical = 16.dp)) {
                Text(label(R.string.app_name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(label(R.string.subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabNames.forEachIndexed { index, id ->
                    NavigationBarItem(
                        selected = page == index,
                        onClick = { onPage(index) },
                        icon = { Text(tabIcons[index], style = MaterialTheme.typography.titleLarge) },
                        label = { Text(label(id), maxLines = 1) }
                    )
                }
            }
        }
    ) { inner ->
        Box(Modifier.padding(inner).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (page) {
                0 -> HomeScreen(periods, symptoms, { onPage(2) }, { onPage(5) }, { onPage(6) })
                1 -> CalendarScreen(periods)
                2 -> AddPeriodScreen(onSavePeriod)
                3 -> InsightsScreen(periods, symptoms)
                4 -> SettingsScreen(language, onLanguage, onClear)
                5 -> SymptomsScreen(onSaveSymptoms)
                else -> HistoryScreen(periods, onRemovePeriod)
            }
        }
    }
}

@Composable private fun Panel(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable private fun Heading(title: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable private fun HomeScreen(
    periods: List<PeriodEntry>,
    symptoms: List<SymptomEntry>,
    addPeriod: () -> Unit,
    addSymptom: () -> Unit,
    history: () -> Unit
) {
    val today = LocalDate.now()
    val starts = periods.map { LocalDate.parse(it.startDate) }
    val next = CycleMath.nextExpected(starts, today)
    val length = CycleMath.averageCycle(starts)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.welcome))
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label(R.string.next_period), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(10.dp))
                Text(next?.let { prettyDate(it) } ?: label(R.string.no_prediction),
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
                if (next != null) {
                    Text(label(R.string.days_remaining) + ": " + CycleMath.daysUntil(next, today),
                        style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        if (periods.isEmpty()) {
            Panel(label(R.string.get_started)) {
                Text(label(R.string.onboarding_prompt))
                Button(onClick = addPeriod, modifier = Modifier.fillMaxWidth()) { Text(label(R.string.log_period)) }
            }
        } else {
            Panel(label(R.string.your_cycle)) {
                Text(label(R.string.average_cycle) + ": " + length + " " + label(R.string.days))
                Text(label(R.string.last_start) + ": " + prettyDate(starts.maxOrNull() ?: today))
                Text(label(R.string.estimate_warning), style = MaterialTheme.typography.bodySmall)
            }
        }
        Panel(label(R.string.quick_actions)) {
            Button(onClick = addPeriod, modifier = Modifier.fillMaxWidth()) { Text(label(R.string.add_period)) }
            OutlinedButton(onClick = addSymptom, modifier = Modifier.fillMaxWidth()) { Text(label(R.string.record_symptoms)) }
            OutlinedButton(onClick = history, modifier = Modifier.fillMaxWidth()) { Text(label(R.string.view_history)) }
        }
        if (symptoms.isNotEmpty()) {
            Panel(label(R.string.recent_symptoms)) {
                Text(label(R.string.last_entry) + ": " + prettyDate(LocalDate.parse(symptoms.first().date)))
                Text(label(R.string.mood) + ": " + symptoms.first().mood + "/5")
            }
        }
        Text(label(R.string.medical_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun CalendarScreen(periods: List<PeriodEntry>) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val first = month.atDay(1)
    val prefix = first.dayOfWeek.value - 1 // Monday-first grid
    val total = ((prefix + month.lengthOfMonth() + 6) / 7) * 7
    val predicted = CycleMath.nextExpected(periods.map { LocalDate.parse(it.startDate) }, today)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Heading(label(R.string.calendar_title))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = month.minusMonths(1) }) { Text("‹") }
            Text(month.month.getDisplayName(java.time.format.TextStyle.FULL, LocalContext.current.resources.configuration.locales[0]) + " " + month.year,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = { month = month.plusMonths(1) }) { Text("›") }
        }
        val weekdayNames = listOf(R.string.mon, R.string.tue, R.string.wed, R.string.thu, R.string.fri, R.string.sat, R.string.sun)
        Row(Modifier.fillMaxWidth()) {
            weekdayNames.forEach { name ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(label(name), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        repeat(total / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { dayIndex ->
                    val n = week * 7 + dayIndex - prefix + 1
                    if (n !in 1..month.lengthOfMonth()) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = month.atDay(n)
                        val recorded = periods.any {
                            val from = LocalDate.parse(it.startDate)
                            val to = LocalDate.parse(it.endDate)
                            !day.isBefore(from) && !day.isAfter(to)
                        }
                        val future = predicted != null && !day.isBefore(predicted) && day.isBefore(predicted.plusDays(5))
                        val container = if (recorded) Rose else if (future) Pink.copy(alpha = 0.55f)
                            else if (day == today) Lilac.copy(alpha = 0.6f) else Color.Transparent
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp).background(container, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center) {
                            Text(n.toString(), color = if (recorded) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Panel(label(R.string.legend)) {
            Text("● " + label(R.string.recorded_days), color = Rose)
            Text("● " + label(R.string.predicted_days), color = MaterialTheme.colorScheme.primary)
            Text(label(R.string.estimate_warning), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable private fun DateField(title: String, value: LocalDate, update: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(onClick = {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day -> update(LocalDate.of(year, month + 1, day)) },
            value.year, value.monthValue - 1, value.dayOfMonth
        ).show()
    }, modifier = Modifier.fillMaxWidth()) {
        Text(title + ": " + prettyDate(value))
    }
}

@Composable private fun AddPeriodScreen(save: (PeriodEntry) -> Unit) {
    var start by remember { mutableStateOf(LocalDate.now()) }
    var end by remember { mutableStateOf(LocalDate.now()) }
    var flow by remember { mutableFloatStateOf(2f) }
    var notes by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.log_period))
        Panel(label(R.string.period_details)) {
            DateField(label(R.string.start_date), start) { start = it; if (end.isBefore(start)) end = start }
            DateField(label(R.string.end_date), end) { end = it }
            Text(label(R.string.flow_level) + ": " + flow.toInt() + "/3")
            Slider(value = flow, onValueChange = { flow = it }, valueRange = 1f..3f, steps = 1)
            OutlinedTextField(value = notes, onValueChange = { notes = it.take(500) },
                label = { Text(label(R.string.notes)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            if (end.isBefore(start)) Text(label(R.string.invalid_dates), color = MaterialTheme.colorScheme.error)
            Button(
                enabled = !end.isBefore(start),
                onClick = { save(PeriodEntry(startDate = start.toString(), endDate = end.toString(), flowLevel = flow.toInt(), notes = notes)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(label(R.string.save)) }
        }
        Text(label(R.string.private_storage), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun SymptomsScreen(save: (SymptomEntry) -> Unit) {
    var day by remember { mutableStateOf(LocalDate.now()) }
    var mood by remember { mutableFloatStateOf(3f) }
    var cramps by remember { mutableStateOf(false) }
    var headache by remember { mutableStateOf(false) }
    var fatigue by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.record_symptoms))
        Panel(label(R.string.daily_checkin)) {
            DateField(label(R.string.date), day) { day = it }
            Text(label(R.string.mood) + ": " + mood.toInt() + "/5")
            Slider(value = mood, onValueChange = { mood = it }, valueRange = 1f..5f, steps = 3)
            listOf(
                Triple(R.string.cramps, cramps, { value: Boolean -> cramps = value }),
                Triple(R.string.headache, headache, { value: Boolean -> headache = value }),
                Triple(R.string.fatigue, fatigue, { value: Boolean -> fatigue = value })
            ).forEach { (res, checked, setter) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = setter)
                    Text(label(res), modifier = Modifier.clickable { setter(!checked) })
                }
            }
            OutlinedTextField(value = notes, onValueChange = { notes = it.take(500) },
                label = { Text(label(R.string.notes)) }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                save(SymptomEntry(date = day.toString(), mood = mood.toInt(), cramps = cramps,
                    headache = headache, fatigue = fatigue, notes = notes))
            }, modifier = Modifier.fillMaxWidth()) { Text(label(R.string.save)) }
        }
    }
}

@Composable private fun InsightsScreen(periods: List<PeriodEntry>, symptoms: List<SymptomEntry>) {
    val starts = periods.map { LocalDate.parse(it.startDate) }
    val lengths = CycleMath.cycleLengths(starts)
    val average = CycleMath.averageCycle(starts)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.insights))
        Panel(label(R.string.cycle_summary)) {
            StatLine(label(R.string.recorded_cycles), periods.size.toString())
            StatLine(label(R.string.average_cycle), average.toString() + " " + label(R.string.days))
            val duration = periods.map {
                CycleMath.bleedingDays(LocalDate.parse(it.startDate), LocalDate.parse(it.endDate))
            }
            if (duration.isNotEmpty()) StatLine(label(R.string.average_bleeding), duration.average().toInt().toString() + " " + label(R.string.days))
            StatLine(label(R.string.symptom_entries), symptoms.size.toString())
        }
        Panel(label(R.string.regularity)) {
            if (lengths.size < 2) Text(label(R.string.more_data))
            else {
                StatLine(label(R.string.shortest_cycle), lengths.min().toString() + " " + label(R.string.days))
                StatLine(label(R.string.longest_cycle), lengths.max().toString() + " " + label(R.string.days))
                Text(label(R.string.estimate_warning), style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(label(R.string.medical_note), style = MaterialTheme.typography.bodySmall)
    }
}
@Composable private fun StatLine(title: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable private fun HistoryScreen(periods: List<PeriodEntry>, delete: (PeriodEntry) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.history))
        if (periods.isEmpty()) Text(label(R.string.no_records))
        periods.forEach { item ->
            Panel(prettyDate(LocalDate.parse(item.startDate))) {
                Text(label(R.string.end_date) + ": " + prettyDate(LocalDate.parse(item.endDate)))
                Text(label(R.string.flow_level) + ": " + item.flowLevel + "/3")
                if (item.notes.isNotBlank()) Text(item.notes)
                TextButton(onClick = { delete(item) }) { Text(label(R.string.delete)) }
            }
        }
    }
}

@Composable private fun SettingsScreen(
    language: String,
    onLanguage: (String) -> Unit,
    clear: () -> Unit
) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Heading(label(R.string.settings))
        Panel(label(R.string.language)) {
            listOf(
                "system" to label(R.string.follow_system),
                "ar" to "العربية",
                "en-GB" to "English (UK)",
                "pt-BR" to "Português (Brasil)"
            ).forEach { (code, name) ->
                Row(
                    Modifier.fillMaxWidth().clickable { onLanguage(code) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = language == code, onClick = { onLanguage(code) })
                    Text(name)
                }
            }
        }
        Panel(label(R.string.privacy)) {
            Text(label(R.string.private_storage))
            Text(label(R.string.no_ads_note))
            Text(label(R.string.medical_note), style = MaterialTheme.typography.bodySmall)
        }
        Panel(label(R.string.data_management)) {
            OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text(label(R.string.erase_data))
            }
        }
    }
    if (confirm) AlertDialog(
        onDismissRequest = { confirm = false },
        title = { Text(label(R.string.erase_data)) },
        text = { Text(label(R.string.erase_confirmation)) },
        confirmButton = {
            TextButton(onClick = { clear(); confirm = false }) { Text(label(R.string.erase_data)) }
        },
        dismissButton = {
            TextButton(onClick = { confirm = false }) { Text(label(R.string.cancel)) }
        }
    )
}
