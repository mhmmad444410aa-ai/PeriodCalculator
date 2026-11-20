package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.PeriodEntity
import com.mhmmad.periodcalculator.domain.*
import java.time.LocalDate
import java.text.NumberFormat
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import kotlin.math.roundToInt

private fun List<PeriodEntity>.planningRecords() = map {
    PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), it.endEpochDay?.let(LocalDate::ofEpochDay))
}

@Composable fun CycleToolsEntry(onTools: () -> Unit) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.EventAvailable, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
            Column(Modifier.weight(1f)) {
                Subheading(stringResource(R.string.tools_title))
                Text(stringResource(R.string.tools_intro), style = MaterialTheme.typography.bodySmall)
            }
        }
        Button(onClick = onTools, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tools_open)) }
    }
}

@Composable fun CycleToolsHome(open: (String) -> Unit) {
    ScreenColumn {
        Text(stringResource(R.string.tools_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.tools_intro))
        val options = listOf(
            Triple("planner", R.string.tools_planner, R.string.tools_planner_desc),
            Triple("accuracy", R.string.tools_accuracy, R.string.tools_accuracy_desc),
            Triple("kit", R.string.tools_kit, R.string.tools_kit_desc)
        )
        options.forEachIndexed { i, item ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(listOf(Icons.Outlined.EventAvailable, Icons.Outlined.Insights, Icons.Outlined.Checklist)[i], null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Column(Modifier.weight(1f)) {
                        Subheading(stringResource(item.second))
                        Text(stringResource(item.third), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                FilledTonalButton(onClick = { open(item.first) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tools_open)) }
            }
        }
    }
}

@Composable fun EventPlannerScreen(periods: List<PeriodEntity>, cycle: Int, bleeding: Int, onPeriod: () -> Unit, onKit: () -> Unit) {
    val today = LocalDate.now()
    var offset by remember { mutableIntStateOf(7) }
    var duration by remember { mutableIntStateOf(3) }
    val result = remember(periods, today, offset, duration, cycle, bleeding) { PlanningTools.event(today, offset, duration, periods.planningRecords(), cycle, bleeding) }
    ScreenColumn {
        Text(stringResource(R.string.tools_planner), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.tools_planner_desc))
        Panel {
            Subheading(stringResource(R.string.tools_event_date))
            Text(DateText(today.plusDays(offset.toLong())), style = MaterialTheme.typography.headlineSmall)
            Slider(value = offset.toFloat(), onValueChange = { offset = it.roundToInt() }, valueRange = 0f..90f, steps = 89)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { offset-- }, enabled = offset > 0) { Text("−") }
                Text(stringResource(R.string.tools_offset, offset), modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedButton(onClick = { offset++ }, enabled = offset < 90) { Text("+") }
            }
            Subheading(stringResource(R.string.tools_event_duration, duration))
            Slider(value = duration.toFloat(), onValueChange = { duration = it.roundToInt() }, valueRange = 1f..14f, steps = 12)
            Text(DateText(today.plusDays(offset.toLong()+duration-1)), style = MaterialTheme.typography.titleMedium)
        }
        if (result == null) Panel {
            Notice(stringResource(R.string.tools_need_record))
            OutlinedButton(onClick = onPeriod, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.log_period)) }
        } else Panel {
            Subheading(stringResource(R.string.tools_overlap))
            Text(stringResource(R.string.tools_overlap_days, result.overlapDays, result.eventDays), style = MaterialTheme.typography.headlineSmall)
            LinearProgressIndicator(progress = { result.overlapDays.toFloat()/result.eventDays }, modifier = Modifier.fillMaxWidth())
            if (result.overlapDays == 0) Text(stringResource(R.string.tools_no_overlap))
            result.windows.forEach { window ->
                Text(DateText(window.first) + " — " + DateText(window.last), style = MaterialTheme.typography.titleMedium)
            }
        }
        Notice(stringResource(R.string.tools_planner_warning))
        Button(onClick = onKit, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tools_kit)) }
    }
}

@Composable fun ForecastAuditScreen(periods: List<PeriodEntity>) {
    val today = LocalDate.now()
    val trials = remember(periods, today) { PlanningTools.audit(periods.planningRecords(),today) }
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: java.util.Locale.UK
    val number = remember(locale) { NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 1 } }
    ScreenColumn {
        Text(stringResource(R.string.tools_accuracy), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.tools_accuracy_desc))
        Notice(stringResource(R.string.tools_audit_method))
        if (trials.isEmpty()) Panel { Text(stringResource(R.string.tools_audit_empty)) }
        else {
            Panel {
                val matches = trials.count { it.inWindow }
                Subheading(stringResource(R.string.tools_error_average))
                Text(number.format(PlanningTools.meanAbsoluteError(trials) ?: 0.0), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.tools_window_hits, matches, trials.size))
                LinearProgressIndicator(progress = { matches.toFloat()/trials.size }, modifier = Modifier.fillMaxWidth())
            }
            trials.asReversed().forEach { trial ->
                Panel {
                    Text(stringResource(R.string.tools_actual, DateText(trial.actual)), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.tools_estimated, DateText(trial.estimated)))
                    Text(DateText(trial.lower)+" — "+DateText(trial.upper))
                    Text(if (trial.signedError == 0L) stringResource(R.string.tools_exact) else stringResource(if (trial.signedError > 0) R.string.tools_late else R.string.tools_early, kotlin.math.abs(trial.signedError)))
                    LinearProgressIndicator(progress = { (kotlin.math.abs(trial.signedError).toFloat()/14).coerceIn(0f,1f) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable fun PreparationKitScreen(checked: Set<String>, onToggle: (String) -> Unit, onReset: () -> Unit) {
    val items = listOf("products" to R.string.tools_kit_products, "spares" to R.string.tools_kit_spares, "bag" to R.string.tools_kit_bag, "hygiene" to R.string.tools_kit_hygiene, "water" to R.string.tools_kit_water, "comfort" to R.string.tools_kit_comfort, "contact" to R.string.tools_kit_contact)
    val count = items.count { it.first in checked }
    var reset by remember { mutableStateOf(false) }
    ScreenColumn {
        Text(stringResource(R.string.tools_kit), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.tools_kit_desc))
        Panel {
            Subheading(stringResource(R.string.tools_kit_progress, count, items.size))
            LinearProgressIndicator(progress = { count.toFloat()/items.size }, modifier = Modifier.fillMaxWidth())
            items.forEach { (key, label) ->
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).toggleable(value = key in checked, role = Role.Checkbox, onValueChange = { onToggle(key) }), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(key in checked, onCheckedChange = null)
                    Text(stringResource(label), modifier = Modifier.weight(1f).padding(start = 12.dp))
                }
            }
            if (count == items.size) Text(stringResource(R.string.tools_kit_done), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = { reset = true }, enabled = count > 0, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.tools_kit_reset)) }
        }
        Notice(stringResource(R.string.tools_kit_local))
    }
    if (reset) AlertDialog(onDismissRequest = { reset = false }, title = { Text(stringResource(R.string.tools_kit_reset)) },
        text = { Text(stringResource(R.string.tools_kit_confirm)) },
        confirmButton = { TextButton(onClick = { onReset(); reset = false }) { Text(stringResource(R.string.tools_kit_reset)) } },
        dismissButton = { TextButton(onClick = { reset = false }) { Text(stringResource(android.R.string.cancel)) } })
}
