package com.pdffox.adv.compose

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdView

@Composable
fun BannerAd(adView: ViewGroup, modifier: Modifier = Modifier) {
	if (LocalInspectionMode.current) {
		return
	}

	AndroidView(
		modifier = modifier,
		factory = { adView },
	)

	DisposableEffect(adView) {
		onDispose {
			(adView as? AdView)?.destroy()
		}
	}
}
