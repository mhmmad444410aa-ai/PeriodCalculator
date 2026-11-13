package com.mhmmad.periodcalculator.ads

import android.content.Context
import java.lang.reflect.Proxy

/**
 * Query Huawei's regional consent state on each launch.
 * Never equate a missing SDK or callback failure with legal permission.
 *
 * A normal app advertising switch does not exist. For sensitive jurisdictions
 * where consent is required but not already present, this build shows NO ads.
 */
object AdConsentBridge {
    fun refresh(context: Context, result: (Boolean) -> Unit) {
        try {
            val consentType = Class.forName("com.huawei.hms.ads.consent.Consent")
            val listenerType = Class.forName("com.huawei.hms.ads.consent.ConsentUpdateListener")
            val instance = consentType.getMethod("getInstance", Context::class.java)
                .invoke(null, context.applicationContext)
            if (instance == null) { result(false); return }

            val listener = Proxy.newProxyInstance(
                listenerType.classLoader,
                arrayOf(listenerType)
            ) { _, method, args ->
                when (method.name) {
                    "onSuccess" -> {
                        // Huawei ConsentUpdateListener.onSuccess(status, needConsent, providers)
                        val needConsent = args?.getOrNull(1) as? Boolean ?: true
                        val status = args?.getOrNull(0)?.toString()?.uppercase() ?: "UNKNOWN"
                        val permissionExists = status == "PERSONALIZED" || status == "NON_PERSONALIZED"
                        result(!needConsent || permissionExists)
                    }
                    "onFail" -> result(false)
                }
                null
            }
            consentType.getMethod("requestConsentUpdate", listenerType).invoke(instance, listener)
        } catch (_: Throwable) {
            // Consent SDK unavailable or changed. Don't make a guess from locale/SIM.
            result(false)
        }
    }
}
