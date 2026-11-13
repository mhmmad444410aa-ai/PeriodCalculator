package com.mhmmad.periodcalculator.ads

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.adsPreferences by preferencesDataStore(name = "advertising_consent")
/** A user preference gate, not a substitute for a lawful CMP/consent SDK decision. */
class AdConsentManager(context: Context) {
    private val appContext = context.applicationContext
    private val key = booleanPreferencesKey("nonpersonalised_ads_optin")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val allowed = appContext.adsPreferences.data.map { it[key] ?: false }
    private val decided = booleanPreferencesKey("advertising_decision_made")
    val decisionMade = appContext.adsPreferences.data.map { it[decided] ?: false }
    fun setAllowed(value: Boolean) { scope.launch { reset(value) } }
    suspend fun reset(value: Boolean = false) {
        appContext.adsPreferences.edit { it[key] = value; it[decided] = true }
        AdConsentBridge.update(appContext, value)
    }
}
