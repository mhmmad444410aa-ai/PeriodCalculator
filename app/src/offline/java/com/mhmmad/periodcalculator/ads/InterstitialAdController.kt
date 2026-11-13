package com.mhmmad.periodcalculator.ads

import android.app.Activity
class InterstitialAdController {
    fun maybeShow(activity: Activity, route: String, consent: Boolean, isStillSafe: () -> Boolean) = Unit
}
