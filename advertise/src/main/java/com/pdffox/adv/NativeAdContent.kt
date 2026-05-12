package com.pdffox.adv

import com.google.android.gms.ads.nativead.NativeAd

data class NativeAdContent(
	val index: Int,
	val hAd: NativeAd?,
	val mAd: NativeAd?,
	val lAd: NativeAd?,
)
