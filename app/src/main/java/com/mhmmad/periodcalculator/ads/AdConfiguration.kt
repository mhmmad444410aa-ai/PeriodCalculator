package com.mhmmad.periodcalculator.ads

import com.mhmmad.periodcalculator.BuildConfig

/** These flags must not become true until ad SDK / jurisdiction / disclosure review passes. */
object AdConfiguration {
    val bannerId: String get() = BuildConfig.BANNER_AD_ID
    val interstitialId: String get() = BuildConfig.INTERSTITIAL_AD_ID
    val enabled: Boolean get() = BuildConfig.ADS_ENABLED && BuildConfig.ADS_RELEASE_APPROVED
    // Ads must never receive PeriodEntity, DailyEntryEntity or LocalDate.
}
