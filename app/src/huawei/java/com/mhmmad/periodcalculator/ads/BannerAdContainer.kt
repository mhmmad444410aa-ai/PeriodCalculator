package com.mhmmad.periodcalculator.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.banner.BannerView

/** Render only in a permitted, non-entry screen. No health context is passed. */
@Composable fun BannerAdContainer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val manager = remember { AdConsentManager(context) }
    val allowed by manager.allowed.collectAsState(initial = false)
    if (allowed && AdConfiguration.bannerId.isNotBlank() && AdManager.canDisplay(context, allowed)) {
        AndroidView(factory = { ctx ->
            BannerView(ctx).apply {
                adId = AdConfiguration.bannerId
                bannerAdSize = BannerAdSize.BANNER_SIZE_320_50
                loadAd(AdParam.Builder().setRequestLocation(false).build())
            }
        }, modifier = modifier.fillMaxWidth(), onRelease = { it.destroy() })
    }
}
