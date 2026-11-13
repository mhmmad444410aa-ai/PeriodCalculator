package com.mhmmad.periodcalculator.ads

import android.content.Context
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.RequestOptions
import com.huawei.hms.ads.NonPersonalizedAd

/** Do not use cycle phase, symptoms, fertility, age, or periods in ad requests. */
object AdManager {
    private var initialised = false
    @Synchronized fun canDisplay(context: Context, hasConsent: Boolean): Boolean {
        if (!hasConsent || !AdConfiguration.enabled) return false
        if (!initialised) {
            runCatching {
                HwAds.init(context.applicationContext)
                val opts = HwAds.getRequestOptions().toBuilder()
                    .setNonPersonalizedAd(NonPersonalizedAd.ALLOW_NON_PERSONALIZED)
                    .setRequestLocation(false).build()
                HwAds.setRequestOptions(opts)
                initialised = true
            }
        }
        return initialised
    }
}
