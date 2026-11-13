package com.mhmmad.periodcalculator.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.*
import com.mhmmad.periodcalculator.reminders.ReminderScheduler
import com.mhmmad.periodcalculator.export.ExportCommands
import java.time.LocalDate

@Composable fun SettingsScreen(vm: PeriodViewModel, prefs: UserPreferences, canLock: () -> Boolean,
                               onPrivacy: () -> Unit, onReminders: () -> Unit, onHistory: () -> Unit) {
    val context = LocalContext.current
    var languages by remember { mutableStateOf(false) }
    var themes by remember { mutableStateOf(false) }
    var confirmErase by remember { mutableStateOf(false) }
    var lockError by remember { mutableStateOf(false) }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.settings), stringResource(R.string.local_data), R.drawable.art_privacy)
        Panel {
            Subheading(stringResource(R.string.language))
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { languages = true }, modifier = Modifier.fillMaxWidth()) {
                Text(when(AppCompatDelegate.getApplicationLocales().toLanguageTags()) {
                    "ar" -> stringResource(R.string.arabic); "en-GB" -> stringResource(R.string.english)
                    "pt-BR" -> stringResource(R.string.portuguese); else -> stringResource(R.string.follow_system)
                })
            }
            Subheading(stringResource(R.string.theme)); Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { themes = true }, modifier = Modifier.fillMaxWidth()) {
                Text(when (prefs.theme) { "dark" -> stringResource(R.string.dark); "light" -> stringResource(R.string.light_theme)
                    else -> stringResource(R.string.system) })
            }
        }
        Panel {
            SettingRow(stringResource(R.string.reminders), Icons.Outlined.Notifications, onReminders)
            SettingRow(stringResource(R.string.history), Icons.Outlined.FolderOpen, onHistory)
            SettingRow(stringResource(R.string.privacy), Icons.Outlined.Policy, onPrivacy)
        }
        Panel {
            Subheading(stringResource(R.string.app_lock))
            Text(stringResource(R.string.app_lock_desc), style = MaterialTheme.typography.bodySmall)
            Switch(checked = prefs.lock, onCheckedChange = { requested ->
                if (!requested || canLock()) { lockError = false; vm.lock(requested) }
                else lockError = true
            })
            if (lockError) Text(stringResource(R.string.lock_unavailable), color = MaterialTheme.colorScheme.error)
        }
        Panel {
            Subheading(stringResource(R.string.erase))
            Text(stringResource(R.string.erase_warn), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { confirmErase = true }) { Text(stringResource(R.string.erase), color = MaterialTheme.colorScheme.error) }
        }
        Notice(stringResource(R.string.medical_notice))
    }
    if (languages) AlertDialog(onDismissRequest = { languages = false }, title = { Text(stringResource(R.string.language)) },
        text = { Column {
            listOf("" to R.string.follow_system, "ar" to R.string.arabic, "en-GB" to R.string.english, "pt-BR" to R.string.portuguese)
                .forEach { (tag, label) -> TextButton(onClick = {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag)); languages = false
                }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) } }
        } }, confirmButton = { TextButton(onClick = { languages = false }) { Text(stringResource(R.string.cancel)) } })
    if (themes) AlertDialog(onDismissRequest = { themes = false }, title = { Text(stringResource(R.string.theme)) },
        text = { Column {
            listOf("system" to R.string.system, "light" to R.string.light_theme, "dark" to R.string.dark).forEach { (tag, label) ->
                TextButton(onClick = { vm.theme(tag); themes = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) }
            }
        } }, confirmButton = { TextButton(onClick = { themes = false }) { Text(stringResource(R.string.cancel)) } })
    if (confirmErase) AlertDialog(onDismissRequest = { confirmErase = false },
        title = { Text(stringResource(R.string.erase)) }, text = { Text(stringResource(R.string.erase_warn)) },
        confirmButton = { TextButton(onClick = { confirmErase = false; ReminderScheduler.cancel(context); vm.clearAll {} }) { Text(stringResource(R.string.erase)) } },
        dismissButton = { TextButton(onClick = { confirmErase = false }) { Text(stringResource(R.string.cancel)) } })
}

@Composable private fun SettingRow(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) {
    TextButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, null); Spacer(Modifier.width(12.dp)); Text(text, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, null)
    }
}

@Composable fun ReminderScreen(vm: PeriodViewModel, prefs: UserPreferences) {
    val context = LocalContext.current
    var requestedKind by remember { mutableStateOf("daily") }
    fun update(kind: String, enabled: Boolean) {
        if (kind == "daily") vm.reminder(enabled) else vm.periodReminder(enabled)
        if (enabled || (if (kind == "daily") prefs.periodReminder else prefs.reminder)) ReminderScheduler.schedule(context)
        else ReminderScheduler.cancel(context)
    }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) update(requestedKind, true) else update(requestedKind, false)
    }
    fun request(kind: String, enabled: Boolean) {
        if (!enabled) update(kind, false)
        else if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestedKind = kind; ask.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else update(kind, true)
    }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.reminders), stringResource(R.string.reminder_desc), R.drawable.art_privacy)
        Panel {
            Subheading(stringResource(R.string.reminders))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.reminder_desc))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.notifications))
                Switch(checked = prefs.reminder, onCheckedChange = { request("daily", it) })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.period_reminder))
                Switch(checked = prefs.periodReminder, onCheckedChange = { request("period", it) })
            }
        }
        Notice(stringResource(R.string.notification_permission))
    }
}

@Composable fun HistoryScreen(periods: List<PeriodEntity>, entries: List<DailyEntryEntity>, commands: ExportCommands, onEdit: (Long) -> Unit) {
    var filter by remember { mutableStateOf("") }
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.history), stringResource(R.string.history_hint), R.drawable.art_calendar)
        Notice(stringResource(R.string.export_warning))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = commands.csv, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.export_csv)) }
            OutlinedButton(onClick = commands.pdf, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.export_pdf)) }
        }
        OutlinedTextField(value = filter, onValueChange = { filter = it.take(7) }, modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.date_from) + " YYYY-MM") }, singleLine = true)
        Subheading(stringResource(R.string.period_history))
        val visible = periods.filter { filter.isBlank() || LocalDate.ofEpochDay(it.startEpochDay).toString().startsWith(filter) }
        if (visible.isEmpty()) Notice(stringResource(R.string.no_data))
        visible.forEach { period ->
            Surface(onClick = { onEdit(period.id) }, color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(DateText(LocalDate.ofEpochDay(period.startEpochDay)), style = MaterialTheme.typography.titleMedium)
                        Text(period.endEpochDay?.let { DateText(LocalDate.ofEpochDay(it)) } ?: stringResource(R.string.not_recorded))
                    }
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit))
                }
            }
        }
        Subheading(stringResource(R.string.count_symptoms) + ": ${entries.size}")
    }
}

@Composable fun PrivacyScreen() {
    ScreenColumn {
        IllustratedHeader(stringResource(R.string.privacy), stringResource(R.string.local_data), R.drawable.art_privacy)
        Panel { Subheading(stringResource(R.string.privacy)); Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.privacy_text)) }
        Notice(stringResource(R.string.export_warning))
        Notice(stringResource(R.string.medical_notice))
    }
}

