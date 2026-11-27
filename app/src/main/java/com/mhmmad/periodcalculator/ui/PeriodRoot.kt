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
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import com.mhmmad.periodcalculator.export.ExportActions

private val tabs = listOf("home", "calendar", "studio", "visit", "settings")

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
        "calendar" -> R.string.calendar; "studio" -> R.string.insights; "insights" -> R.string.insights
        "settings" -> R.string.settings; "period" -> R.string.log_period
        "symptoms" -> R.string.log_symptoms; "fertility" -> R.string.fertility
        "history" -> R.string.history; "reminders" -> R.string.reminders
        "privacy" -> R.string.privacy
        "review" -> R.string.review_title
        "visit" -> R.string.visit_title
        "tools" -> R.string.tools_title
        "planner" -> R.string.tools_planner
        "accuracy" -> R.string.tools_accuracy
        "kit" -> R.string.tools_kit
        else -> R.string.app_name
    }
    Scaffold(
        topBar = {
            if (screen != "onboarding") {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (screen == "home") Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
                            Text(if (screen == "library") { when(wellnessLanguage()) { "ar" -> "المكتبة الصحية"; "pt" -> "Biblioteca de saúde"; else -> "Health library" } } else stringResource(titleId), style = MaterialTheme.typography.titleLarge,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        if (screen !in tabs) IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    },
                    actions = {
                        if (screen == "home" || screen == "insights" || screen == "studio") {
                            IconButton(onClick = { go("review") }) {
                                Icon(Icons.Outlined.DateRange, contentDescription = stringResource(R.string.review_title))
                            }
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
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    tabs.forEach { route ->
                        val icon = when(route) {
                            "home" -> Icons.Outlined.Home; "calendar" -> Icons.Outlined.CalendarMonth
                            "studio" -> Icons.Outlined.Insights; "visit" -> Icons.Outlined.Assignment; else -> Icons.Outlined.Settings
                        }
                        val label = when(route) {
                            "home" -> R.string.home; "calendar" -> R.string.calendar
                            "studio" -> R.string.insights; "visit" -> R.string.visit_tab; else -> R.string.settings
                        }
                        NavigationBarItem(
                            selected = screen == route, onClick = { go(route) },
                            icon = { Icon(icon, null) }, label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                    }
                }
            }
        },
        floatingActionButton = {
            if (screen == "calendar") {
                FloatingActionButton(onClick = { editingId = 0; go("period") }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Outlined.Add, stringResource(R.string.log_period))
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = nav, startDestination = if (prefs.onboarded) "home" else "onboarding", enterTransition = { androidx.compose.animation.EnterTransition.None }, exitTransition = { androidx.compose.animation.ExitTransition.None }, popEnterTransition = { androidx.compose.animation.EnterTransition.None }, popExitTransition = { androidx.compose.animation.ExitTransition.None }) {
                composable("onboarding") { OnboardingScreen(vm) { go("home") } }
                composable("home") { HomeScreen(periods, entries, vm, onPeriod = { editingId = 0; go("period") },
                    onSymptom = { editingDay = java.time.LocalDate.now().toEpochDay(); go("symptoms") },
                    onHistory = { go("history") }, onFertility = { go("fertility") }, onTools = { go("tools") }, onAtlas = { go("studio") }, onLibrary = { go("library") }) }
                composable("calendar") { CalendarScreen(periods, entries, vm) { day -> editingDay = day; go("symptoms") } }
                composable("library") { WellnessLibrary() }
                composable("studio") { Column { FilledTonalButton(onClick = { go("insights") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) { Text(stringResource(R.string.insights)) }; Box(Modifier.weight(1f)) { WellnessStudio(entries) } } }
                composable("insights") { InsightsScreen(periods, entries, vm, onHistory = { go("history") },
                    onFertility = { go("fertility") }, onReview = { go("review") }, onTools = { go("tools") }) }
                composable("tools") { CycleToolsHome { go(it) } }
                composable("planner") { EventPlannerScreen(periods, prefs.cycleLength, prefs.bleedingLength, onPeriod = { editingId = 0; go("period") }, onKit = { go("kit") }) }
                composable("accuracy") { ForecastAuditScreen(periods) }
                composable("kit") { PreparationKitScreen(prefs.kitItems, vm::toggleKit, vm::resetKit) }
                composable("visit") { VisitPrepScreen(periods, entries, onLog = { editingDay = java.time.LocalDate.now().toEpochDay(); go("symptoms") }) }
                composable("review") { CycleReviewScreen(periods, entries) { day -> editingDay = day; go("symptoms") } }
                composable("settings") { SettingsScreen(vm, prefs, canLock, onPrivacy = { go("privacy") },
                    onReminders = { go("reminders") }, onHistory = { go("history") }) }
                composable("period") { PeriodEditor(periods.firstOrNull { it.id == editingId }, vm, done) }
                composable("symptoms") { SymptomEditor(entries.firstOrNull { it.epochDay == editingDay }, editingDay, vm, done) }
                composable("fertility") { FertilityScreen(vm) }
                composable("reminders") { ReminderScreen(vm, prefs) }
                composable("history") { ExportActions(periods, entries) { actions ->
                    HistoryScreen(periods, entries, actions, onEdit = { id -> editingId = id; go("period") })
                } }
                composable("privacy") { PrivacyScreen() }
            }

        }
    }
}
