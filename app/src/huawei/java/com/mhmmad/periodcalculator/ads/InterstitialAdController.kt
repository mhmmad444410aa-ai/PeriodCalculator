package com.mhmmad.periodcalculator.ads

import android.app.Activity
import android.os.SystemClock
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.InterstitialAd

/** Disabled by default. May only activate after opt-in & compliance sign-off. */
class InterstitialAdController {
    private var lastShown = 0L
    private var transitions = 0
    fun maybeShow(activity: Activity, route: String, consent: Boolean) {
        // Explicit allowlist never includes private entry, symptoms, history or fertility screens.
        if (route !in setOf("settings", "about")) return
        if (!consent || !AdManager.canDisplay(activity, consent) || AdConfiguration.interstitialId.isBlank()) return
        transitions++
        if (transitions < 5 || SystemClock.elapsedRealtime() - lastShown < 30 * 60_000L) return
        val ad = InterstitialAd(activity)
        ad.adId = AdConfiguration.interstitialId
        ad.loadAd(AdParam.Builder().setRequestLocation(false).build())
        // No automatic show: require a loaded callback and an additional safe transition.
        // This first release intentionally has no interstitial presentation.
        transitions = 0
    }
}
