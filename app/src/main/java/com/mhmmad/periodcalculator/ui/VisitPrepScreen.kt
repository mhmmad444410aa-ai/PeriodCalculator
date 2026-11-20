package com.mhmmad.periodcalculator.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.DailyEntryEntity
import com.mhmmad.periodcalculator.data.PeriodEntity
import com.mhmmad.periodcalculator.domain.VisitDay
import com.mhmmad.periodcalculator.domain.VisitPeriod
import com.mhmmad.periodcalculator.domain.VisitSummaries
import java.time.LocalDate

@Composable
fun VisitPrepScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, onLog: () -> Unit) {
    val context = LocalContext.current
    val today = LocalDate.now().toEpochDay()
    var days by remember { mutableIntStateOf(30) }
    var picks by remember { mutableStateOf(setOf<Int>()) }
    var note by remember { mutableStateOf("") }
    var includeJournal by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<String?>(null) }
    val questions = listOf(R.string.visit_q1, R.string.visit_q2, R.string.visit_q3, R.string.visit_q4, R.string.visit_q5)
    val labels = listOf(R.string.cramps, R.string.headache, R.string.bloating, R.string.fatigue)
    val report = remember(periods, entries, today, days) {
        VisitSummaries.build(today, days, periods.map { VisitPeriod(it.startEpochDay, it.endEpochDay) },
            entries.map { VisitDay(it.epochDay, listOf(it.cramps, it.headache, it.bloating, it.fatigue)) })
    }
    val coverage = stringResource(R.string.visit_coverage, report.logged, days)
    val starts = stringResource(R.string.visit_starts, report.periodStarts)
    val duration = if (report.completedLengths.isEmpty()) stringResource(R.string.visit_missing)
        else stringResource(R.string.visit_length, report.completedLengths.minOrNull()!!, report.completedLengths.maxOrNull()!!)
    val results = labels.mapIndexed { index, label -> 
        val name = stringResource(label)
        if (report.logged == 0) "$name: —" else "$name: ${report.markedDays[index]} / ${report.logged}"
    }
    val questionText = questions.map { stringResource(it) }
    val title = stringResource(R.string.visit_title)
    val warning = stringResource(R.string.visit_notice)
    val range = stringResource(R.string.visit_range_label, days)
    val metricHint = stringResource(R.string.visit_metric)
    val qHeading = stringResource(R.string.visit_questions)
    val noteHeading = stringResource(R.string.visit_note)
    val errorMessage = stringResource(R.string.visit_error)
    val saved = stringResource(R.string.visit_saved)
    val empty = stringResource(R.string.visit_missing)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val snapshot = pending
        pending = null
        if (uri != null && snapshot != null) {
            val ok = runCatching {
                val stream = context.contentResolver.openOutputStream(uri) ?: error("No output stream")
                stream.use { it.write(snapshot.toByteArray(Charsets.UTF_8)) }
            }.isSuccess
            Toast.makeText(context, if (ok) saved else errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
    ScreenColumn {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.visit_intro))
        Panel {
            Subheading(stringResource(R.string.visit_step1))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90).forEach { value ->
                    FilterChip(selected = days == value, onClick = { days = value }, label = { Text(stringResource(R.string.visit_days, value)) }, modifier = Modifier.weight(1f))
                }
            }
            Text(coverage, style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(progress = { report.logged.toFloat() / days }, modifier = Modifier.fillMaxWidth())
            Text(starts)
            Text(duration)
            Text(metricHint, style = MaterialTheme.typography.bodySmall)
            results.forEachIndexed { index, line ->
                Text(line, style = MaterialTheme.typography.labelLarge)
                if (report.logged > 0) LinearProgressIndicator(progress = { report.markedDays[index].toFloat() / report.logged }, modifier = Modifier.fillMaxWidth())
            }
            if (report.logged == 0) Notice(stringResource(R.string.visit_empty))
            OutlinedButton(onClick = onLog, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.log_symptoms)) }
        }
        Panel {
            Subheading(stringResource(R.string.visit_step2))
            Text(stringResource(R.string.visit_pick_hint))
            questionText.forEachIndexed { index, question ->
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = index in picks, role = Role.Checkbox, onValueChange = { checked -> picks = if (checked) picks + index else picks - index }), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = index in picks, onCheckedChange = null)
                    Text(question, modifier = Modifier.weight(1f).padding(start = 10.dp))
                }
            }
            OutlinedTextField(value = note, onValueChange = { note = it.take(750) }, label = { Text(noteHeading) }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Text(stringResource(R.string.visit_session), style = MaterialTheme.typography.bodySmall)
        }
        Panel {
            Subheading(stringResource(R.string.visit_step3))
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = includeJournal, role = Role.Checkbox, onValueChange = { includeJournal = it }), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = includeJournal, onCheckedChange = null)
                Text(stringResource(R.string.visit_include), modifier = Modifier.weight(1f).padding(start = 10.dp))
            }
            if (includeJournal) {
                Text(range); Text(coverage); Text(starts); Text(duration)
                Text(metricHint, style = MaterialTheme.typography.bodySmall)
                results.forEach { Text(it) }
            }
            Text(qHeading, style = MaterialTheme.typography.titleSmall)
            if (picks.isEmpty()) Text(empty) else picks.sorted().forEach { Text("• " + questionText[it]) }
            if (note.isNotBlank()) Text(note)
            Notice(stringResource(R.string.visit_export_warning))
            Button(onClick = {
                pending = buildString {
                    appendLine(title); appendLine(LocalDate.ofEpochDay(today)); appendLine(warning)
                    if (includeJournal) {
                        appendLine(range); appendLine(coverage); appendLine(starts); appendLine(duration); appendLine(metricHint)
                        results.forEach { appendLine(it) }
                    }
                    appendLine(qHeading); picks.sorted().forEach { appendLine("• " + questionText[it]) }
                    if (note.isNotBlank()) { appendLine(noteHeading); appendLine(note.trim()) }
                }
                launcher.launch("visit-preparation.txt")
            }, enabled = pending == null && (picks.isNotEmpty() || note.isNotBlank() || includeJournal), modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.visit_export)) }
            Notice(warning)
        }
    }
}
