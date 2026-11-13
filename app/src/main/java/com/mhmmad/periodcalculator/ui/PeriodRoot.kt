package com.mhmmad.periodcalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.mhmmad.periodcalculator.R
import com.mhmmad.periodcalculator.data.PeriodViewModel
import com.mhmmad.periodcalculator.ads.BannerAdContainer
import com.mhmmad.periodcalculator.ads.InterstitialAdController
import com.mhmmad.periodcalculator.ads.AdConsentManager
import com.mhmmad.periodcalculator.BuildConfig
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.mhmmad.periodcalculator.export.ExportActions

private val tabs = listOf("home", "calendar", "insights", "settings")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodRoot(vm: PeriodViewModel, onUnlock: () -> Unit, canLock: () -> Boolean) {
    val nav = rememberNavController()
    val currentEntry by nav.currentBackStackEntryAsState()
    val screen = currentEntry?.destination?.route ?: "home"
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val adConsent = remember(context) { AdConsentManager(context) }
    val adAllowed by adConsent.allowed.collectAsState(initial = false)
    val adChoiceMade by adConsent.decisionMade.collectAsState(initial = true)
    val interstitial = remember { InterstitialAdController() }
    val periods by vm.periods.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    var editingId by remember { mutableLongStateOf(0L) }
    var editingDay by remember { mutableLongStateOf(java.time.LocalDate.now().toEpochDay()) }
    val go: (String) -> Unit = { route ->
        if (route in tabs) nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        } else nav.navigate(route)
    }
    val done: () -> Unit = {
        if (!nav.popBackStack()) go("home")
    }
    val currentScreen by rememberUpdatedState(screen)
    LaunchedEffect(screen, adAllowed) {
        if (screen == "settings" && adAllowed) {
            (context as? Activity)?.let { activity ->
                interstitial.maybeShow(activity, screen, adAllowed) {
                    currentScreen == "settings" && adAllowed
                }
            }
        }
    }
    val titleId = when (screen) {
        "calendar" -> R.string.calendar; "insights" -> R.string.insights
        "settings" -> R.string.settings; "period" -> R.string.log_period
        "symptoms" -> R.string.log_symptoms; "fertility" -> R.string.fertility
        "history" -> R.string.history; "reminders" -> R.string.reminders
        "privacy" -> R.string.privacy; "consent" -> R.string.privacy_ads
        else -> R.string.app_name
    }
    Scaffold(
        topBar = {
            if (screen != "onboarding") {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (screen == "home") Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                            Text(stringResource(titleId), style = MaterialTheme.typography.titleLarge,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        if (screen !in tabs) IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        bottomBar = {
            if (screen in tabs) {
                Column {
                    if (screen == "home") BannerAdContainer(modifier = Modifier.fillMaxWidth())
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { route ->
                        val icon = when(route) {
                            "home" -> Icons.Outlined.Home; "calendar" -> Icons.Outlined.CalendarMonth
                            "insights" -> Icons.Outlined.Insights; else -> Icons.Outlined.Settings
                        }
                        val label = when(route) {
                            "home" -> R.string.home; "calendar" -> R.string.calendar
                            "insights" -> R.string.insights; else -> R.string.settings
                        }
                        NavigationBarItem(
                            selected = screen == route, onClick = { go(route) },
                            icon = { Icon(icon, null) }, label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = .14f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    }
                }
            }
        },
        floatingActionButton = {
            if (screen == "home" || screen == "calendar") {
                FloatingActionButton(onClick = { editingId = 0; go("period") }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Outlined.Add, stringResource(R.string.log_period))
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = nav, startDestination = if (prefs.onboarded) "home" else "onboarding") {
                composable("onboarding") { OnboardingScreen(vm) { go("home") } }
                composable("home") { HomeScreen(periods, entries, vm, onPeriod = { editingId = 0; go("period") },
                    onSymptom = { editingDay = java.time.LocalDate.now().toEpochDay(); go("symptoms") },
                    onHistory = { go("history") }, onFertility = { go("fertility") }) }
                composable("calendar") { CalendarScreen(periods, entries, vm) { day -> editingDay = day; go("symptoms") } }
                composable("insights") { InsightsScreen(periods, entries, vm, onHistory = { go("history") },
                    onFertility = { go("fertility") }) }
                composable("settings") { SettingsScreen(vm, prefs, canLock, onPrivacy = { go("privacy") },
                    onReminders = { go("reminders") }, onHistory = { go("history") }, onConsent = { go("consent") }) }
                composable("period") { PeriodEditor(periods.firstOrNull { it.id == editingId }, vm, done) }
                composable("symptoms") { SymptomEditor(entries.firstOrNull { it.epochDay == editingDay }, editingDay, vm, done) }
                composable("fertility") { FertilityScreen(vm) }
                composable("reminders") { ReminderScreen(vm, prefs) }
                composable("history") { ExportActions(periods, entries) { actions ->
                    HistoryScreen(periods, entries, actions, onEdit = { id -> editingId = id; go("period") })
                } }
                composable("privacy") { PrivacyScreen() }
                composable("consent") { AdvertisingPrivacyScreen() }
            }

        }
    }
    if (BuildConfig.ADS_ENABLED && prefs.onboarded && !adChoiceMade) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.privacy_ads)) },
            text = { Text(stringResource(R.string.consent_message)) },
            confirmButton = {
                TextButton(onClick = { adConsent.setAllowed(true) }) {
                    Text(stringResource(R.string.allow_nonpersonalised))
                }
            },
            dismissButton = {
                TextButton(onClick = { adConsent.setAllowed(false) }) {
                    Text(stringResource(R.string.not_now))
                }
            }
        )
    }
}
