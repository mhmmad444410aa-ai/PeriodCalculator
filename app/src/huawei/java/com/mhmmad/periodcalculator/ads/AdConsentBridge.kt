package com.mhmmad.periodcalculator.ads

import android.content.Context
import com.huawei.hms.ads.consent.Consent
import com.huawei.hms.ads.consent.ConsentStatus

/** Mirrors an explicit non-personalised opt-in into the Huawei consent SDK. */
object AdConsentBridge {
    fun update(context: Context, allowed: Boolean) {
        val status = if (allowed) ConsentStatus.NON_PERSONALIZED else ConsentStatus.UNKNOWN
        Consent.getInstance(context.applicationContext).setConsentStatus(status)
    }
}
