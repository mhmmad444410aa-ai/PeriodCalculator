package com.mhmmad.periodcalculator.ads

import android.app.Activity
import android.os.SystemClock
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.InterstitialAd

/**
 * Infrequent ads on a safe, non-health journal route only.
 * Never delay entry saving or expose the cycle/health history in an ad request.
 */
class InterstitialAdController {
    private var lastShownAt = SystemClock.elapsedRealtime()
    private var safeVisits = 0
    private var loading = false

    fun maybeShow(activity: Activity, route: String, consent: Boolean, isStillSafe: () -> Boolean) {
        if (route != "settings" || !consent) return
        if (!AdManager.canDisplay(activity, consent) || AdConfiguration.interstitialId.isBlank()) return
        safeVisits++
        if (safeVisits < 3 || loading) return
        if (SystemClock.elapsedRealtime() - lastShownAt < 30 * 60_000L) return

        val ad = InterstitialAd(activity)
        ad.adId = AdConfiguration.interstitialId
        loading = true
        ad.adListener = object : AdListener() {
            override fun onAdLoaded() {
                loading = false
                if (!activity.isFinishing && !activity.isDestroyed &&
                    isStillSafe() && ad.isLoaded &&
                    SystemClock.elapsedRealtime() - lastShownAt >= 30 * 60_000L) {
                    lastShownAt = SystemClock.elapsedRealtime()
                    safeVisits = 0
                    ad.show(activity)
                }
            }
            override fun onAdFailed(errorCode: Int) { loading = false }
        }
        ad.loadAd(AdParam.Builder().setRequestLocation(false).build())
    }
}
