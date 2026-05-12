package com.pdffox.adv.compose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.nativead.NativeAd
import com.pdffox.adv.Ads
import com.pdffox.adv.NativeAdContent
import kotlinx.coroutines.delay

private class NativeAdState(
	private val context: Context,
	private val areaKey: String,
) {
	private val nativeAdState = mutableStateOf<NativeAd?>(null)
	private var nativeAdContent: NativeAdContent? = null
	private var disposed = false

	val nativeAd: State<NativeAd?>
		get() = nativeAdState

	fun refresh() {
		if (disposed) {
			return
		}
		val adGroup = Ads.getNativeAd(context, areaKey) {
			refreshFromPool()
		}
		if (adGroup != null) {
			setAdGroup(adGroup)
		}
	}

	fun dispose() {
		disposed = true
		destroyCurrentAd()
	}

	private fun refreshFromPool() {
		if (disposed) {
			return
		}
		val adGroup = Ads.getNativeAd(context, areaKey) {}
		if (adGroup != null) {
			setAdGroup(adGroup)
		}
	}

	private fun setAdGroup(adGroup: NativeAdContent) {
		destroyCurrentAd()
		nativeAdContent = adGroup
		nativeAdState.value = adGroup.hAd ?: adGroup.mAd ?: adGroup.lAd
	}

	private fun destroyCurrentAd() {
		nativeAdState.value?.destroy()
		nativeAdContent?.hAd?.destroy()
		nativeAdContent?.mAd?.destroy()
		nativeAdContent?.lAd?.destroy()
		nativeAdState.value = null
		nativeAdContent = null
	}
}

@Composable
fun rememberNativeAd(
	areaKey: String,
	refreshImmediately: Boolean = true,
	shouldRefreshImmediately: () -> Boolean = { true },
	shouldAutoRefresh: () -> Boolean = shouldRefreshImmediately,
): State<NativeAd?> {
	val context = LocalContext.current
	val nativeAdState = remember(context, areaKey) {
		NativeAdState(context, areaKey)
	}
	val currentShouldRefreshImmediately = rememberUpdatedState(shouldRefreshImmediately)
	val currentShouldAutoRefresh = rememberUpdatedState(shouldAutoRefresh)

	LaunchedEffect(nativeAdState, refreshImmediately) {
		if (refreshImmediately && currentShouldRefreshImmediately.value()) {
			nativeAdState.refresh()
		}
		while (true) {
			delay(Ads.nativeRefreshTime)
			if (currentShouldAutoRefresh.value()) {
				nativeAdState.refresh()
			}
		}
	}

	DisposableEffect(nativeAdState) {
		onDispose {
			nativeAdState.dispose()
		}
	}

	return nativeAdState.nativeAd
}
