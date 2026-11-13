package com.mhmmad.periodcalculator.ads

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.banner.BannerView
import com.mhmmad.periodcalculator.R

/** Uses Huawei's layout-defined banner size; no health data is supplied to the ad. */
@Composable
fun BannerAdContainer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val consent = remember(context) { AdConsentManager(context) }
    val allowed by consent.allowed.collectAsState(initial = false)
    if (allowed && AdConfiguration.bannerId.isNotBlank() && AdManager.canDisplay(context, allowed)) {
        AndroidView(
            factory = { ctx ->
                (LayoutInflater.from(ctx).inflate(R.layout.huawei_banner, null, false) as BannerView).apply {
                    adId = AdConfiguration.bannerId
                    loadAd(AdParam.Builder().setRequestLocation(false).build())
                }
            },
            modifier = modifier.fillMaxWidth(),
            onRelease = { it.destroy() }
        )
    }
}
