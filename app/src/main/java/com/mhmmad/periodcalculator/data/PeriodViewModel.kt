package com.mhmmad.periodcalculator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mhmmad.periodcalculator.domain.*
import com.mhmmad.periodcalculator.ads.AdConsentManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class PeriodViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = PeriodRepository(application)
    private val preferences = PreferenceRepository(application)
    val periods = repo.periods.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val entries = repo.entries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val prefs = preferences.settings.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())
    // Distinguish cold start from never-onboarded to avoid showing the setup transiently.
    val ready = preferences.settings.map { true }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    fun prediction(today: LocalDate = LocalDate.now()) = CycleEngine.predict(
        periods.value.map { PeriodRecord(it.id, LocalDate.ofEpochDay(it.startEpochDay), it.endEpochDay?.let(LocalDate::ofEpochDay)) },
        prefs.value.cycleLength, today
    )
    fun savePeriod(entity: PeriodEntity) = viewModelScope.launch { repo.savePeriod(entity) }
    fun deletePeriod(id: Long) = viewModelScope.launch { repo.deletePeriod(id) }
    fun saveDay(entity: DailyEntryEntity) = viewModelScope.launch { repo.saveDay(entity) }
    fun deleteDay(day: Long) = viewModelScope.launch { repo.deleteDay(day) }
    fun finishSetup(cycle: Int, bleeding: Int) = viewModelScope.launch { preferences.finishSetup(cycle, bleeding) }
    fun theme(value: String) = viewModelScope.launch { preferences.setTheme(value) }
    fun reminder(value: Boolean) = viewModelScope.launch { preferences.setReminder(value) }
    fun periodReminder(value: Boolean) = viewModelScope.launch { preferences.setPeriodReminder(value) }
    fun lock(value: Boolean) = viewModelScope.launch { preferences.setLock(value) }
    fun clearAll(onComplete: () -> Unit) = viewModelScope.launch {
        repo.clear(); preferences.clear(); AdConsentManager(getApplication()).reset(); onComplete()
    }
}
