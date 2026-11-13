package com.mhmmad.periodcalculator.ads

import android.content.Context

/** The offline product flavour never requests advertisements. */
object AdConsentBridge {
    fun refresh(context: Context, result: (Boolean) -> Unit) {
        result(false)
    }
}
