package com.mhmmad.periodcalculator.ads

import android.content.Context

/**
 * Keep explicit first-run permission and revocation in the app-private DataStore.
 * Huawei ad requests are configured for NON_PERSONALIZED only in AdManager.
 *
 * Some Ads Prime distributions do not expose the standalone Consent classes
 * on the app's compilation classpath. Use the optional bridge if available,
 * otherwise do not make up a consent state in the vendor SDK.
 */
object AdConsentBridge {
    fun update(context: Context, allowed: Boolean) {
        runCatching {
            val type = Class.forName("com.huawei.hms.ads.consent.Consent")
            val statusType = Class.forName("com.huawei.hms.ads.consent.ConsentStatus")
            val consent = type.getMethod("getInstance", Context::class.java)
                .invoke(null, context.applicationContext)
            val selected = statusType.enumConstants?.firstOrNull {
                (it as? Enum<*>)?.name == if (allowed) "NON_PERSONALIZED" else "UNKNOWN"
            }
            if (consent != null && selected != null) {
                type.getMethod("setConsentStatus", statusType).invoke(consent, selected)
            }
        }
    }
}
