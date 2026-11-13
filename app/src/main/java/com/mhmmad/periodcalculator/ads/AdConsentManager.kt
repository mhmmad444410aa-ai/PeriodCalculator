package com.mhmmad.periodcalculator.ads

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mhmmad.periodcalculator.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

private val Context.adsPreferences by preferencesDataStore(name = "advertising_consent")

/**
 * No "show ads" preference is offered.
 *
 * Only non-personalised ads can load, and only when the Huawei consent
 * provider confirms no additional consent is required or a valid prior
 * permission exists. If status cannot be confirmed, do NOT request ads.
 * Respect explicit refusals stored by earlier app versions.
 */
class AdConsentManager(context: Context) {
    private val appContext = context.applicationContext
    private val optInKey = booleanPreferencesKey("nonpersonalised_ads_optin")
    private val decisionKey = booleanPreferencesKey("advertising_decision_made")

    companion object {
        private val sdkPermitsAds = MutableStateFlow(false)
    }

    val allowed = combine(appContext.adsPreferences.data, sdkPermitsAds) { prefs, sdkAllowed ->
        val earlierRefusal = prefs[decisionKey] == true && prefs[optInKey] == false
        BuildConfig.ADS_ENABLED && sdkAllowed && !earlierRefusal
    }

    fun refresh() {
        sdkPermitsAds.value = false
        if (!BuildConfig.ADS_ENABLED) return
        AdConsentBridge.refresh(appContext) { legallyPermitted ->
            sdkPermitsAds.value = legallyPermitted
        }
    }
}
